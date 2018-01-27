package me.blog.korn123.easyphotomap.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.LruCache
import me.blog.korn123.easyphotomap.R
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.time.StopWatch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Created by CHO HANJOONG on 2016-08-06.
 */
object BitmapUtils {

    private var mMemoryCache: LruCache<String, Bitmap>? = null

    init {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
        }
    }

    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache!!.put(key, bitmap)
        }
    }

    fun getBitmapFromMemCache(key: String): Bitmap? = mMemoryCache?.get(key)

    fun createScaledBitmap(srcPath: String, destPath: String, fixedWidthHeight: Int): Boolean {
        val stopWatch: StopWatch = StopWatch()
        stopWatch.start()
        var result = true
        var outputStream: OutputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 20
            val bitmap = BitmapFactory.decodeFile(srcPath, options)
            val height = bitmap.height
            val width = bitmap.width
            val downSampleHeight = height / width.toFloat() * fixedWidthHeight
            val downSampleWidth = width / height.toFloat() * fixedWidthHeight
            val thumbNail = when (width > height) {
                true -> Bitmap.createScaledBitmap(bitmap, fixedWidthHeight, downSampleHeight.toInt(), false)
                false -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedWidthHeight, false)
            }
            outputStream = FileOutputStream(destPath)
            thumbNail!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        } finally {
            IOUtils.closeQuietly(outputStream)
        }

        Log.i("stopwatch", "createScaledBitmap ${stopWatch.time} $srcPath")
        return result
    }

    fun createScaledBitmap(srcPath: String, fixedWidth: Int): Bitmap? {
        val outputStream: OutputStream? = null
        var thumbNail: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inSampleSize = 10
            val bitmap = BitmapFactory.decodeFile(srcPath, options)
            val height = bitmap.height
            val width = bitmap.width
            val downSampleHeight = height / width.toFloat() * fixedWidth
            thumbNail = Bitmap.createScaledBitmap(bitmap, fixedWidth, downSampleHeight.toInt(), false)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return thumbNail
    }

    @JvmOverloads
    fun createScaledBitmap(bitmap: Bitmap, point: Point, scaleFactorX: Double = 0.8, scaleFactorY: Double = 0.5): Bitmap {
        val fixedWidth = point.x * scaleFactorX
        val fixedHeight = point.y * scaleFactorY
        val height = bitmap.height
        val width = bitmap.width
        val downSampleWidth = width / height.toFloat() * fixedHeight
        val downSampleHeight = height / width.toFloat() * fixedWidth
        return when {
            // 가로이미지 & 세로보기 화면에서는 width값에 맞춰 고정함
            width > height && point.x < point.y -> Bitmap.createScaledBitmap(bitmap, fixedWidth.toInt(), downSampleHeight.toInt(), false)
            // 가로이미지 & 가로보기 화면에서는 height값에 맞춰 고정함
            width > height && point.x > point.y -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
            width < height -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
            width == height -> Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), fixedHeight.toInt(), false)
            else -> bitmap
        }
    }

    fun decodeFile(activity: Activity, imagePath: String?, options: BitmapFactory.Options? = null): Bitmap = when (imagePath != null && File(imagePath).exists()) {
        true -> {
            options?.let { BitmapFactory.decodeFile(imagePath, options) } ?: BitmapFactory.decodeFile(imagePath)
        }
        false -> {
            BitmapFactory.decodeResource(activity.resources, android.R.drawable.ic_menu_gallery)
        }
    }

    fun border(context: Context, bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(bmp.width + borderSize * 2, bmp.height + borderSize * 2, bmp.config)
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(ContextCompat.getColor(context, R.color.colorPrimary))
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    fun addFrame(activity: Activity, bmp: Bitmap, borderSize: Int, id: Int): Bitmap {
        val bmpWithFrame = Bitmap.createBitmap(bmp.width + borderSize, bmp.height + borderSize * 2, bmp.config)
        val canvas = Canvas(bmpWithFrame)
        val temp = BitmapFactory.decodeResource(activity.resources, id)
        val frame = Bitmap.createScaledBitmap(temp, bmp.width + borderSize, bmp.height + borderSize * 2, false)
        canvas.drawBitmap(frame, 0f, 0f, null)
        canvas.drawBitmap(bmp, (borderSize / 2).toFloat(), borderSize.toFloat(), null)
        return bmpWithFrame
    }

}
