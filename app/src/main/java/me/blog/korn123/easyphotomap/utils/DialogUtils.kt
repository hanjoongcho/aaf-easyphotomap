package me.blog.korn123.easyphotomap.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.activities.FileExplorerActivity
import me.blog.korn123.easyphotomap.activities.ThumbnailExplorerActivity

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object DialogUtils {

    fun makeToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun makeSnackBar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
    }

    fun showAlertDialog(context: Context, message: String) {
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { _, _ -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context, message: String, activity: Activity, imagePath: String, listener: ThumbnailExplorerActivity.PositiveListener) {
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            listener.register()
            return@OnClickListener
        }
        val negativeListener = DialogInterface.OnClickListener { _, _ -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        builder.setNegativeButton(context.getString(R.string.cancel), negativeListener)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = BitmapUtils.getSampleSize(options, imagePath, 2000000)
        options.inJustDecodeBounds = false
        val originBitmap = BitmapUtils.decodeFile(activity, imagePath, options)
        val scaledBitmap = BitmapUtils.createScaledBitmap(originBitmap, CommonUtils.getDefaultDisplay(activity))
        val thumbView = ImageView(activity)
        thumbView.setImageBitmap(scaledBitmap)
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(thumbView)
        builder.setView(layout)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context, message: String, activity: Activity, imagePath: String, listener: FileExplorerActivity.PositiveListener) {
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            listener.register()
            return@OnClickListener
        }
        val negativeListener = DialogInterface.OnClickListener { _, _ -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        builder.setNegativeButton(context.getString(R.string.cancel), negativeListener)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inSampleSize = BitmapUtils.getSampleSize(options, imagePath, 2000000)
        options.inJustDecodeBounds = false
        val originBitmap = BitmapUtils.decodeFile(activity, imagePath, options)
        val scaledBitmap = BitmapUtils.createScaledBitmap(originBitmap, CommonUtils.getDefaultDisplay(activity))
        val thumbView = ImageView(activity)
        thumbView.setImageBitmap(scaledBitmap)
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(thumbView)
        builder.setView(layout)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener,
                        negativeListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setNegativeButton("취소", negativeListener)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }
}
