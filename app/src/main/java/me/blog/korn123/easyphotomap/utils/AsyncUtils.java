package me.blog.korn123.easyphotomap.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import org.apache.commons.lang.StringUtils;

import java.net.URI;

import me.blog.korn123.easyphotomap.thumbnail.AsyncDrawable;
import me.blog.korn123.easyphotomap.thumbnail.BitmapWorkerTask;

/**
 * Created by CHO HANJOONG on 2016-08-03.
 */
public class AsyncUtils {

    public static void loadBitmap(ImageView imageView, String imagePath, int widthHeight) {
        if (cancelPotentialWork(imagePath, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(imagePath, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(task, imagePath);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imagePath, String.valueOf(widthHeight));
        }
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        BitmapWorkerTask bitmapWorkerTask = null;
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                bitmapWorkerTask = asyncDrawable.getBitmapWorkerTask();
            }
        }
        return bitmapWorkerTask;
    }

    public static boolean cancelPotentialWork(String filePath, ImageView imageView) {

        String previousFilepath = "empty";
        BitmapWorkerTask task = null;
        if (imageView.getDrawable() instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable  = (AsyncDrawable) imageView.getDrawable();
            if (asyncDrawable != null) {
                task = asyncDrawable.getBitmapWorkerTask();
                previousFilepath = task.filePath;
            }
        }

        if (StringUtils.equals(filePath, previousFilepath)) {
//                Log.i("cancelPotentialWork", "bitmapData " + bitmapData);
            // Cancel previous task
            return false;
        } else {
            if (task != null) {
                task.cancel(true);
            }
            // The same work is already in progress
            return true;
        }
    }
}
