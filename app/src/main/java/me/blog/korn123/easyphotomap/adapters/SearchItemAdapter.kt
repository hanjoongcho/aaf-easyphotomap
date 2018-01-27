package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import org.apache.commons.io.FilenameUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class SearchItemAdapter(private val activity: Activity,
                        private val listPhotoMapItem: ArrayList<PhotoMapItem>,
                        private val onItemClickListener: AdapterView.OnItemClickListener,
                        private val onItemLongClickListener: AdapterView.OnItemLongClickListener
) : RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {
    private val layoutInflater = activity.layoutInflater

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): SearchItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return SearchItemAdapter.ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val photoMapItem: PhotoMapItem = listPhotoMapItem[position]
        holder?.run {
            text1?.text = photoMapItem.info
            text2?.text = photoMapItem.date
            text3?.text = photoMapItem.imagePath
            val fileName = FilenameUtils.getName(photoMapItem.imagePath)
            SearchItemAdapter.ThumbnailTask(activity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.WORKING_DIRECTORY + fileName + ".thumb")
        }

        holder?.let {
            it.itemView.setOnClickListener { item ->
                onItemClickListener.onItemClick(null, item, it.adapterPosition, it.itemId)
            }
            it.itemView.setOnLongClickListener { item ->
                onItemLongClickListener.onItemLongClick(null, item, it.adapterPosition, it.itemId)
            }
        }
    }

    override fun getItemCount(): Int = listPhotoMapItem.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = createViewHolder(R.layout.item_search, parent)

    fun getItem(position: Int): PhotoMapItem = listPhotoMapItem[position]

    private class ThumbnailTask(val activity: Activity, val position: Int, val holder: SearchItemAdapter.ViewHolder) : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            val filePath = params[0]
//            val widthHeight = CommonUtils.dpToPixel(activity, 45f)
            var resized: Bitmap? = null
            if (holder.position == position) {
                var bitmap = BitmapUtils.decodeFile(activity, filePath)
//                resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true)
                resized = bitmap
            } else {
                this.cancel(true)
            }
            return resized
        }

        override fun onPostExecute(bitmap: Bitmap) {
            if (holder.position == position) {
                val td = TransitionDrawable(arrayOf(ColorDrawable(Color.TRANSPARENT), BitmapDrawable(activity.resources, bitmap)))
                holder.image1?.setImageDrawable(td)
                td.startTransition(1000)
            }
        }
    }

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
