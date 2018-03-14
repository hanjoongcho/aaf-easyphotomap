package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_popup_image.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.utils.BitmapUtils
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
        options.inJustDecodeBounds = false
        options.inSampleSize = 2
        val imagePath = intent.getStringExtra("imagePath")
        val bitmap: Bitmap = if (File(imagePath).exists()) BitmapUtils.decodeFile(this@PopupImageActivity, imagePath, options) else BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_gallery) 
        imageView.setImageBitmap(bitmap)
        finish.setOnClickListener { finish() }
        rotateLeft.setOnClickListener { imageView.setRotationBy(-90F) }
        rotateRight.setOnClickListener { imageView.setRotationBy(90F) }
    }
}
