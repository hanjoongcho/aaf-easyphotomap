package me.blog.korn123.easyphotomap.thumbnail;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import me.blog.korn123.easyphotomap.utils.AsyncUtils;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    public String filePath;

    public BitmapWorkerTask(String imagePath, ImageView imageView) {
        // WeakReference 를 사용하는 이유는 image 처럼 메모리를 많이 차지하는 객체에 대한 가비지컬렉터를 보장하기 위해서입니다.
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.filePath = imagePath;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String filePath = params[0];
        int widthHeight = Integer.valueOf(params[1]);
        this.filePath = filePath;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
//        options.outWidth = widthHeight;
//        options.outHeight = widthHeight;
//        options.inSampleSize = 10;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true);
        return resized;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        ImageView imageView = imageViewReference.get();
        BitmapWorkerTask task = AsyncUtils.getBitmapWorkerTask(imageView);
        if(this == task && bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

}