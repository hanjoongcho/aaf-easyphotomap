package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.support.v4.graphics.ColorUtils
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.github.hanjoongcho.commons.extensions.baseConfig
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
    val layoutParamsA: FrameLayout.LayoutParams by lazy {
        FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, 0)
        }
    }
    val layoutParamsB: FrameLayout.LayoutParams by lazy {
        FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(120, 0, 0, 0)
        }
    }

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): TimelineItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return TimelineItemAdapter.ViewHolder(view as ViewGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TimelineItemAdapter.ViewHolder = createViewHolder(R.layout.item_timeline, parent)

    override fun onBindViewHolder(holder: TimelineItemAdapter.ViewHolder?, position: Int) {
        val photoMapItem: PhotoMapItem = listPhotoMapItem[position]
        holder?.run {
            text1?.text = photoMapItem.info
            val fileName = FilenameUtils.getName(photoMapItem.imagePath)
            TimelineItemAdapter.ThumbnailTask(activity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.WORKING_DIRECTORY + fileName + ".thumb")
            if (isDateChange(position)) {
                image2?.visibility = View.VISIBLE
                timelineHeader?.let {
                    it.layoutParams = layoutParamsA
                    it.setBackgroundColor(ColorUtils.setAlphaComponent(activity.resources.getColor(R.color.colorPrimary), 255))
                }
                text2?.run {
                    setTextColor(activity.resources.getColor(android.R.color.white))
                    text = photoMapItem.date
                }
            } else {
                image2?.visibility = View.GONE
                timelineHeader?.let {
                    it.layoutParams = layoutParamsB
                    it.setBackgroundColor(ColorUtils.setAlphaComponent(activity.resources.getColor(R.color.colorPrimary), 0))
                }
                text2?.run {
                    setTextColor(activity.resources.getColor(R.color.default_text_color))
                    text = photoMapItem.date
                }
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
        var image2: ImageView? = null
        init {
            parent?.let {
                timelineHeader = parent.findViewById(R.id.timelineHeader)
                text1 = parent.findViewById(R.id.address)
                text2 = parent.findViewById(R.id.timelineDate)
                image1 = parent.findViewById(R.id.thumbnail)
                image2 = parent.findViewById(R.id.headerIcon)
            }
        }
    }

    private class ThumbnailTask(val activity: Activity, val position: Int, val holder: TimelineItemAdapter.ViewHolder) : AsyncTask<String, Void, Bitmap>() {
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
