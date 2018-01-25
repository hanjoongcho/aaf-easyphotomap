package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import org.apache.commons.io.FilenameUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class SearchItemAdapter(private val context: Context,
                        private val activity: Activity,
                        private val listPhotoMap: ArrayList<PhotoMapItem>) : RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {

    private val layoutInflater = activity.layoutInflater
    
    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        Log.i("testRE", "$holder $position")
        val photoMapItem: PhotoMapItem = listPhotoMap[position]
        holder?.run {
            text1?.text = photoMapItem.info
            text2?.text = photoMapItem.date
            text3?.text = photoMapItem.imagePath
            var bitmap: Bitmap?
            val fileName = FilenameUtils.getName(photoMapItem.imagePath)
            bitmap = BitmapUtils.decodeFile(activity, Constant.WORKING_DIRECTORY + fileName + ".thumb")
            image1?.setImageBitmap(bitmap)
        }
    }

    override fun getItemCount(): Int = listPhotoMap.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = createViewHolder(R.layout.item_search, parent)

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): SearchItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return SearchItemAdapter.ViewHolder(view as ViewGroup)
    }

    //    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
//        var row = convertView
//        var holder: ViewHolder?
//
//        if (row == null) {
//            val inflater = (mContext as Activity).layoutInflater
//            row = inflater.inflate(mLayoutResourceId, parent, false)
//            holder = ViewHolder()
//            holder.textView1 = row.findViewById(R.id.text1) as TextView
//            holder.textView2 = row.findViewById(R.id.text2) as TextView
//            holder.textView3 = row.findViewById(R.id.text3) as TextView
//            holder.imageView1 = row.findViewById(R.id.image1) as ImageView
//            row.tag = holder
//        } else {
//            holder = row.tag as ViewHolder
//        }
//
//        val imageEntity = mEntities[position]
//        var bitmap: Bitmap?
//        val fileName = FilenameUtils.getName(imageEntity.imagePath)
//        bitmap = BitmapUtils.decodeFile(mActivity, Constant.WORKING_DIRECTORY + fileName + ".thumb")
//        holder.textView1?.text = imageEntity.info
//        holder.textView2?.text = imageEntity.date.toString()
//        holder.textView3?.text = imageEntity.imagePath
//        holder.imageView1?.setImageBitmap(bitmap)
//        return row
//    }
//
    class ViewHolder(val parent: ViewGroup?) : RecyclerView.ViewHolder(parent) {
        var text1: TextView? = null
        var text2: TextView? = null
        var text3: TextView? = null
        var image1: ImageView? = null
        init {
            parent?.let {
                text1 = parent.findViewById(R.id.text1)
                text2 = parent.findViewById(R.id.text2)
                text3 = parent.findViewById(R.id.text3)
                image1 = parent.findViewById(R.id.image1)
            }
        }
    }
}
