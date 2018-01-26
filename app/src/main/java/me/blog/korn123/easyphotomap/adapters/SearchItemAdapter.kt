package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.AdapterView
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
                        private val listPhotoMap: ArrayList<PhotoMapItem>,
                        private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {
    private val layoutInflater = activity.layoutInflater

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): SearchItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return SearchItemAdapter.ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
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

        holder?.let {
            it.itemView.setOnClickListener { item ->
                onItemClickListener.onItemClick(null, item, it.adapterPosition, it.itemId)
            }
        }
    }

    override fun getItemCount(): Int = listPhotoMap.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = createViewHolder(R.layout.item_search, parent)

    fun getItem(position: Int): PhotoMapItem = listPhotoMap[position]

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
