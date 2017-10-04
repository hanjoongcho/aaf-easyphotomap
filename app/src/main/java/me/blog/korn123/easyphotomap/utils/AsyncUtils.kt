package me.blog.korn123.easyphotomap.utils

import android.widget.ImageView
import me.blog.korn123.easyphotomap.thumbnail.AsyncDrawable
import me.blog.korn123.easyphotomap.thumbnail.BitmapWorkerTask
import org.apache.commons.lang.StringUtils

/**
 * Created by CHO HANJOONG on 2016-08-03.
 */
object AsyncUtils {

    fun loadBitmap(imageView: ImageView, imagePath: String, widthHeight: Int) {
        if (cancelPotentialWork(imagePath, imageView)) {
            val task = BitmapWorkerTask(imagePath, imageView)
            val asyncDrawable = AsyncDrawable(task, imagePath)
            imageView.setImageDrawable(asyncDrawable)
            task.execute(imagePath, widthHeight.toString())
        }
    }

    fun getBitmapWorkerTask(imageView: ImageView?): BitmapWorkerTask? {
        var bitmapWorkerTask: BitmapWorkerTask? = null
        if (imageView != null) {
            val drawable = imageView.drawable
            if (drawable is AsyncDrawable) {
                bitmapWorkerTask = drawable.bitmapWorkerTask
            }
        }
        return bitmapWorkerTask
    }

    fun cancelPotentialWork(filePath: String, imageView: ImageView): Boolean {

        var previousFilepath = "empty"
        var task: BitmapWorkerTask? = null
        if (imageView.drawable is AsyncDrawable) {
            val asyncDrawable = imageView.drawable as AsyncDrawable
            if (asyncDrawable != null) {
                task = asyncDrawable.bitmapWorkerTask
                previousFilepath = task!!.filePath
            }
        }

        if (StringUtils.equals(filePath, previousFilepath)) {
            //                Log.i("cancelPotentialWork", "bitmapData " + bitmapData);
            // Cancel previous task
            return false
        } else {
            if (task != null) {
                task.cancel(true)
            }
            // The same work is already in progress
            return true
        }
    }

}
