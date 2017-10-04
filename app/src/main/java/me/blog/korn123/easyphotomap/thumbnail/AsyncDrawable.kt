package me.blog.korn123.easyphotomap.thumbnail

import android.graphics.drawable.BitmapDrawable
import java.lang.ref.WeakReference

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
class AsyncDrawable(bitmapWorkerTask: BitmapWorkerTask, imagePath: String) : BitmapDrawable() {
    private val bitmapWorkerTaskReference: WeakReference<*>

    init {
        bitmapWorkerTaskReference = WeakReference(bitmapWorkerTask)
    }

    val bitmapWorkerTask: BitmapWorkerTask
        get() = bitmapWorkerTaskReference.get() as BitmapWorkerTask
}
