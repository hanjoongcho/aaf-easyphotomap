package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import java.io.File

/**
 * Created by CHO HANJOONG on 2016-08-21.
 */
class PopupImageActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_image)
        val imageView = findViewById<PhotoView>(R.id.imageView)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = 2
        val imagePath = intent.getStringExtra("imagePath")
        val bitmap: Bitmap?
        val targetBitmap: Bitmap?
        if (File(imagePath).exists()) {
            BitmapUtils.decodeFile(this@PopupImageActivity, imagePath, options)
            val width = options.outWidth
            val height = options.outHeight
            options.inJustDecodeBounds = false
            bitmap = BitmapUtils.decodeFile(this@PopupImageActivity, imagePath, options)
            targetBitmap = when (width > height && CommonUtils.getDisplayOrientation(this) == 0 || width < height && CommonUtils.getDisplayOrientation(this) == 1) {
                true -> {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
                false -> bitmap
            }
        } else {
            bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_gallery)
            targetBitmap = bitmap
        }

        imageView.setImageBitmap(targetBitmap)
        findViewById<ImageView>(R.id.finish).run {
            setOnClickListener { finish() }
        }
    }
}
