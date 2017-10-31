package me.blog.korn123.easyphotomap.thumbnail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import me.blog.korn123.easyphotomap.utils.AsyncUtils
import java.lang.ref.WeakReference

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
class BitmapWorkerTask(var filePath: String, imageView: ImageView, private var imageViewReference: WeakReference<ImageView>?) : AsyncTask<String, Void, Bitmap>() {

    init {
        // WeakReference 를 사용하는 이유는 image 처럼 메모리를 많이 차지하는 객체에 대한 가비지컬렉터를 보장하기 위해서입니다.
        imageViewReference = WeakReference(imageView)
    }

    override fun doInBackground(vararg params: String): Bitmap {
        val filePath = params[0]
        val widthHeight = Integer.valueOf(params[1])
        this.filePath = filePath
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        //        options.outWidth = widthHeight;
        //        options.outHeight = widthHeight;
        //        options.inSampleSize = 10;
        val bitmap = BitmapFactory.decodeFile(filePath, options)
        return Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true)
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        var resultBitmap = bitmap
        if (isCancelled) {
            resultBitmap = null
        }
        val imageView = imageViewReference?.get()
        val task = AsyncUtils.getBitmapWorkerTask(imageView)
        if (this === task && resultBitmap != null) {
            imageView?.setImageBitmap(resultBitmap)
        }
    }

}