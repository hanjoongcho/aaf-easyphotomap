package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
class TimelineItemAdapter(private val mContext: Context, private val mActivity: Activity, private val mLayoutResourceId: Int, private val mListPhotoMapItem: ArrayList<PhotoMapItem>) : ArrayAdapter<PhotoMapItem>(mContext, mLayoutResourceId, mListPhotoMapItem) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder

        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.address) as TextView
            holder.imageView1 = row.findViewById(R.id.thumbnail) as ImageView
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val photoMapItem = mListPhotoMapItem[position]
        if (isDateChange(position)) {
            row?.findViewById(R.id.timelineHeader)?.visibility = View.VISIBLE
            (row?.findViewById(R.id.timelineDate) as TextView).text = photoMapItem.dateWithoutTime
        } else {
            row?.findViewById(R.id.timelineHeader)?.visibility = View.GONE
        }
        holder.textView1?.text = "${photoMapItem.date}\n${photoMapItem.info}"
        val fileName = FilenameUtils.getName(photoMapItem.imagePath)
        val bm = BitmapUtils.decodeFile(mActivity, Constant.WORKING_DIRECTORY + fileName + ".thumb")

        holder.imageView1?.setImageBitmap(bm)

        return row
    }

    private fun isDateChange(position: Int): Boolean {
        var isChange = false
        val previousDate: String
        val currentDate: String
        if (position > 0) {
            val previous = mListPhotoMapItem[position - 1]
            val current = mListPhotoMapItem[position]
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

    internal class ViewHolder {
        var textView1: TextView? = null
        var textView2: TextView? = null
        var imageView1: ImageView? = null
    }

}
