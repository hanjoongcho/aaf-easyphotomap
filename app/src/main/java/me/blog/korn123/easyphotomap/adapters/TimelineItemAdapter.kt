package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class TimelineItemAdapter(private val activity: Activity,
                          private val listPhotoMapItem: ArrayList<PhotoMapItem>,
                          private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<TimelineItemAdapter.ViewHolder>() {
    private val layoutInflater = activity.layoutInflater

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): TimelineItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return TimelineItemAdapter.ViewHolder(view as ViewGroup)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TimelineItemAdapter.ViewHolder = createViewHolder(R.layout.item_timeline, parent)

    override fun onBindViewHolder(holder: TimelineItemAdapter.ViewHolder?, position: Int) {
        val photoMapItem: PhotoMapItem = listPhotoMapItem[position]
        holder?.run {
            text1?.text = "${photoMapItem.date}\n${photoMapItem.info}"
            var bitmap: Bitmap?
            val fileName = FilenameUtils.getName(photoMapItem.imagePath)
            bitmap = BitmapUtils.decodeFile(activity, Constant.WORKING_DIRECTORY + fileName + ".thumb")
            image1?.setImageBitmap(bitmap)
            if (isDateChange(position)) {
                timelineHeader?.visibility = View.VISIBLE
                text2?.text = photoMapItem.dateWithoutTime
            } else {
                timelineHeader?.visibility = View.VISIBLE
                text2?.text = ""
            }
        }

        holder?.let {
            it.itemView.setOnClickListener { item ->
                onItemClickListener.onItemClick(null, item, it.adapterPosition, it.itemId)
            }
        }
    }

    override fun getItemCount(): Int = listPhotoMapItem.size

    fun getItem(position: Int): PhotoMapItem = listPhotoMapItem[position]
    
    class ViewHolder(val parent: ViewGroup?) : RecyclerView.ViewHolder(parent) {
        var timelineHeader: View? = null
        var text1: TextView? = null
        var text2: TextView? = null
        var image1: ImageView? = null
        init {
            parent?.let {
                timelineHeader = parent.findViewById(R.id.timelineHeader) 
                text1 = parent.findViewById(R.id.address)
                text2 = parent.findViewById(R.id.timelineDate)
                image1 = parent.findViewById(R.id.thumbnail)
            }
        }
    }
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
//        var row = convertView
//        val holder: ViewHolder
//
//        if (row == null) {
//            val inflater = (mContext as Activity).layoutInflater
//            row = inflater.inflate(mLayoutResourceId, parent, false)
//            holder = ViewHolder()
//            holder.textView1 = row.findViewById(R.id.address) as TextView
//            holder.imageView1 = row.findViewById(R.id.thumbnail) as ImageView
//            row.tag = holder
//        } else {
//            holder = row.tag as ViewHolder
//        }
//
//        val photoMapItem = mListPhotoMapItem[position]
//        if (isDateChange(position)) {
//            row?.findViewById<View>(R.id.timelineHeader)?.visibility = View.VISIBLE
//            (row?.findViewById(R.id.timelineDate) as TextView).text = photoMapItem.dateWithoutTime
//        } else {
//            row?.findViewById<View>(R.id.timelineHeader)?.visibility = View.GONE
//        }
//        holder.textView1?.text = "${photoMapItem.date}\n${photoMapItem.info}"
//        val fileName = FilenameUtils.getName(photoMapItem.imagePath)
//        val bm = BitmapUtils.decodeFile(mActivity, Constant.WORKING_DIRECTORY + fileName + ".thumb")
//
//        holder.imageView1?.setImageBitmap(bm)
//
//        return row
//    }
//
    private fun isDateChange(position: Int): Boolean {
        var isChange = false
        val previousDate: String
        val currentDate: String
        if (position > 0) {
            val previous = listPhotoMapItem[position - 1]
            val current = listPhotoMapItem[position]
            previousDate = previous.dateWithoutTime
            currentDate = current.dateWithoutTime
            if (!StringUtils.equals(previousDate, currentDate)) {
                isChange = true
            }
        } else {
            isChange = true
        }
        return isChange
    }
}
