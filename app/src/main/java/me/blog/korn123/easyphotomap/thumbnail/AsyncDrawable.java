package me.blog.korn123.easyphotomap.thumbnail;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference bitmapWorkerTaskReference;

    public AsyncDrawable(BitmapWorkerTask bitmapWorkerTask, String imagePath) {
//        super(imagePath);
        bitmapWorkerTaskReference = new WeakReference(bitmapWorkerTask);
    }

    public BitmapWorkerTask getBitmapWorkerTask() {
        return (BitmapWorkerTask)bitmapWorkerTaskReference.get();
    }
}
