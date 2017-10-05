package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
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
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class SearchItemAdapter(private val mContext: Context, private val mActivity: Activity, private val mLayoutResourceId: Int, private val mEntities: ArrayList<PhotoMapItem>) : ArrayAdapter<PhotoMapItem>(mContext, mLayoutResourceId, mEntities) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        var holder: ViewHolder? = null

        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row!!.findViewById(R.id.text1) as TextView
            holder.textView2 = row.findViewById(R.id.text2) as TextView
            holder.textView3 = row.findViewById(R.id.text3) as TextView
            holder.imageView1 = row.findViewById(R.id.image1) as ImageView
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val imageEntity = mEntities[position]
        var bitmap: Bitmap? = null
        val fileName = FilenameUtils.getName(imageEntity.imagePath)
        bitmap = BitmapUtils.decodeFile(mActivity, Constant.WORKING_DIRECTORY + fileName + ".thumb")
        holder.textView1!!.text = imageEntity.info
        holder.textView2!!.text = imageEntity.date.toString()
        holder.textView3!!.text = imageEntity.imagePath
        holder.imageView1!!.setImageBitmap(bitmap)
        return row
    }

    internal class ViewHolder {
        var textView1: TextView? = null
        var textView2: TextView? = null
        var textView3: TextView? = null
        var imageView1: ImageView? = null
    }
}
