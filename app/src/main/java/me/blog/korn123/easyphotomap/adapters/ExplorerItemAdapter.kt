package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.drew.lang.GeoLocation
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.models.FileItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
class ExplorerItemAdapter(private val mActivity: Activity, private val mContext: Context, private val mLayoutResourceId: Int, private val mEntities: List<FileItem>) : ArrayAdapter<FileItem>(mContext, mLayoutResourceId, mEntities) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        var holder: ViewHolder? = null

        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            // bind view
            holder.textView1 = row!!.findViewById(R.id.text1) as TextView
            holder.textView2 = row.findViewById(R.id.text2) as TextView
            holder.textView3 = row.findViewById(R.id.text3) as TextView
            holder.imageView1 = row.findViewById(R.id.image1) as ImageView
            holder.textView1!!.typeface = Typeface.DEFAULT
            holder.textView2!!.typeface = Typeface.DEFAULT
            holder.textView3!!.typeface = Typeface.DEFAULT

            // set tag
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val entity = mEntities[position]

        // init default option
        val widthHeight = (CommonUtils.getDefaultDisplay(mActivity).x / 5) as Int
        //        holder.imageView1.getLayoutParams().height = widthHeight;
        //        holder.imageView1.getLayoutParams().width = widthHeight;

        // init default value
        holder.textView1!!.text = entity.fileName
        holder.textView2!!.text = ""
        holder.textView3!!.text = ""
        holder.imageView1!!.setImageBitmap(defaultImage())

        // init async process
        val imagePath = entity.getImagePath()
        holder.position = position
        if (entity.isDirectory) {
            holder.textView2!!.visibility = View.GONE
            holder.textView3!!.visibility = View.GONE
            ThumbnailTask(mActivity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, widthHeight.toString())
        } else {
            holder.textView2!!.visibility = View.VISIBLE
            holder.textView3!!.visibility = View.VISIBLE
            ThumbnailTask(mActivity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath, widthHeight.toString())
        }
        return row
    }

    private class ThumbnailTask(private val mActivity: Activity, private val mPosition: Int, private val mHolder: ViewHolder) : AsyncTask<String, Void, Bitmap>() {
        private var isDirectory = false
        private var geoLocation: GeoLocation? = null

        override fun doInBackground(vararg params: String): Bitmap? {
            val filePath = params[0]
            val widthHeight = CommonUtils.dpToPixel(mActivity, 45f)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 20
            var resized: Bitmap? = null
            //            Log.i("doInBack", String.format("%s, %s", mHolder.position, mPosition));
            if (mHolder.position == mPosition) {
                if (filePath == null) {
                    isDirectory = true
                    var bitmap = BitmapUtils.getBitmapFromMemCache("defaultBitmap")
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(mActivity.resources, R.drawable.ic_menu_archive)
                        BitmapUtils.addBitmapToMemoryCache("defaultBitmap", bitmap!!)
                    }
                    resized = bitmap
                } else {
                    var bitmap = BitmapUtils.getBitmapFromMemCache(filePath)
                    if (bitmap == null) {
                        bitmap = BitmapUtils.decodeFile(mActivity, filePath, options)
                        BitmapUtils.addBitmapToMemoryCache(filePath, bitmap)
                    }
                    resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true)

                    val gpsDirectory = CommonUtils.getGPSDirectory(filePath)
                    if (gpsDirectory != null && gpsDirectory!!.getGeoLocation() != null) {
                        geoLocation = gpsDirectory!!.getGeoLocation()
                    }
                }
            } else {
                // mListView holder가 재활용되면 task cancel 되도록 수정 2016.11.07 Hanjoong Cho
                this.cancel(true)
            }
            return resized
        }

        override fun onPostExecute(bitmap: Bitmap) {
            if (mHolder.position == mPosition) {
                if (isDirectory) {
                    mHolder.imageView1!!.setImageBitmap(bitmap)
                } else {
                    if (geoLocation != null) {
                        mHolder.textView2!!.text = "위도: " + geoLocation!!.latitude.toString()
                        mHolder.textView3!!.text = "경도: " + geoLocation!!.longitude.toString()
                    } else {
                        mHolder.textView2!!.text = "위도: 정보없음"
                        mHolder.textView3!!.text = "경도: 정보없음"
                    }
                    val td = TransitionDrawable(arrayOf(ColorDrawable(Color.TRANSPARENT), BitmapDrawable(mActivity.resources, bitmap)))
                    mHolder.imageView1!!.setImageDrawable(td)
                    td.startTransition(1000)
                }
            }
        }
    }

    private var mDefaultBitmap: Bitmap? = null
    private fun defaultImage(): Bitmap {
        if (mDefaultBitmap == null) {
                mDefaultBitmap = BitmapFactory.decodeResource(mActivity.resources, android.R.drawable.ic_menu_gallery)
        }
        return mDefaultBitmap!!
    }

    private class ViewHolder {
        internal var textView1: TextView? = null
        internal var textView2: TextView? = null
        internal var textView3: TextView? = null
        internal var imageView1: ImageView? = null
        var position: Int = 0
    }

}
