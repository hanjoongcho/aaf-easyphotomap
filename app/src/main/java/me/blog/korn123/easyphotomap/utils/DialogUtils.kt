package me.blog.korn123.easyphotomap.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
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
        val positiveListener = DialogInterface.OnClickListener { dialog, which -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context, message: String, activity: Activity, imagePath: String) {
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { dialog, which -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inSampleSize = 5
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

    fun showAlertDialog(context: Context, message: String, activity: Activity, imagePath: String, listener: ThumbnailExplorerActivity.PositiveListener) {
        val start = System.currentTimeMillis()
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { dialog, which ->
            listener.register()
            return@OnClickListener
        }
        val NegativeListener = DialogInterface.OnClickListener { dialog, which -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inSampleSize = 5
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

    fun showAlertDialog(context: Context, message: String, activity: Activity, listener: FileExplorerActivity.PositiveListener) {
        val start = System.currentTimeMillis()
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { dialog, which ->
            listener.register()
            return@OnClickListener
        }
        val NegativeListener = DialogInterface.OnClickListener { dialog, which -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener)
        val options = BitmapFactory.Options()
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context, message: String, activity: Activity, imagePath: String, listener: FileExplorerActivity.PositiveListener) {
        val start = System.currentTimeMillis()
        val builder = AlertDialog.Builder(context)
        val positiveListener = DialogInterface.OnClickListener { dialog, which ->
            listener.register()
            return@OnClickListener
        }
        val NegativeListener = DialogInterface.OnClickListener { dialog, which -> return@OnClickListener }
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener)
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        options.inSampleSize = 5
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

    fun showAlertDialog(context: Context,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }

    fun showAlertDialog(context: Context,
                        title: String,
                        message: String,
                        positiveListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        //        builder.setIcon(R.drawable.book);
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("확인", positiveListener)
        val alert = builder.create()
        alert.show()
    }

}
