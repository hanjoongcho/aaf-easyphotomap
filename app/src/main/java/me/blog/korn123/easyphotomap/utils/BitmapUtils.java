package me.blog.korn123.easyphotomap.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.LruCache;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by CHO HANJOONG on 2016-08-06.
 */
public class BitmapUtils {

    private static LruCache<String, Bitmap> mMemoryCache;

    static {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public static boolean createScaledBitmap(String srcPath, String destPath, int fixedWidthHeight) {
        boolean result = true;
        OutputStream outputStream = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 20;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float downSampleHeight = (height / (float)width) * fixedWidthHeight;
            float downSampleWidth  = (width / (float)height) * fixedWidthHeight;
            Bitmap thumbNail = null;
            if (width > height) {
                thumbNail = Bitmap.createScaledBitmap(bitmap, fixedWidthHeight, (int)downSampleHeight, false);
            } else {
                thumbNail = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, fixedWidthHeight, false);
            }
            outputStream = new FileOutputStream(destPath);
            thumbNail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return result;
    }

    public static Bitmap createScaledBitmap(String srcPath, int fixedWidth) {
        boolean result = true;
        OutputStream outputStream = null;
        Bitmap thumbNail = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 10;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float downSampleHeight = (height / (float)width) * fixedWidth;
            thumbNail = Bitmap.createScaledBitmap(bitmap, fixedWidth, (int)downSampleHeight, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return thumbNail;
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, Point point) {
        return createScaledBitmap(bitmap, point, 0.8, 0.5);
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, Point point, double scaleFactorX, double scaleFactorY) {
        Bitmap downscaledBitmap = null;
        double fixedWidth  = point.x * scaleFactorX;
        double fixedHeight = point.y * scaleFactorY;
        try {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            double downSampleWidth = (width / (float)height) * fixedHeight;
            double downSampleHeight = (height / (float)width) * fixedWidth;
            if (width > height && point.x < point.y) { // 가로이미지 & 세로보기 화면에서는 width값에 맞춰 고정함
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)fixedWidth, (int)downSampleHeight, false);
            } else if (width > height && point.x > point.y) { // 가로이미지 & 가로보기 화면에서는 height값에 맞춰 고정함
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else if (width < height) {
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else if (width == height) {
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else {
                downscaledBitmap = bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downscaledBitmap;
    }

    public static Bitmap decodeFile(Activity activity, String imagePath) {
        return decodeFile(activity, imagePath, null);
    }

    public static Bitmap decodeFile(Activity activity, String imagePath, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (imagePath != null && new File(imagePath).exists()) {
            if (options == null) {
                bitmap = BitmapFactory.decodeFile(imagePath);
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath, options);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), android.R.drawable.ic_menu_gallery);
        }
        return bitmap;
    }

    public static Bitmap border(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.parseColor("#0275d8"));
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap addFrame(Activity activity, Bitmap bmp, int borderSize, int id) {
        Bitmap bmpWithFrame = Bitmap.createBitmap(bmp.getWidth() + borderSize, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithFrame);
        Bitmap temp = BitmapFactory.decodeResource(activity.getResources(), id);
        Bitmap frame = Bitmap.createScaledBitmap(temp, bmp.getWidth() + borderSize, bmp.getHeight() + borderSize * 2, false);
        canvas.drawBitmap(frame, 0, 0, null);
        canvas.drawBitmap(bmp, borderSize / 2, borderSize, null);
        return bmpWithFrame;
    }

}
