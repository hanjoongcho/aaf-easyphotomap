package me.blog.korn123.easyphotomap.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.support.design.widget.Snackbar
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.activities.FileExplorerActivity
import me.blog.korn123.easyphotomap.activities.ThumbnailExplorerActivity
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils

/**
 * Created by CHO HANJOONG on 2018-02-25.
 */

fun Activity.showConfirmDialog(message: String) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.ok), null)
    val alert = builder.create()
    alert.show()
}

fun Activity.showConfirmDialogWithFinish(message: String) {
    val positiveListener = DialogInterface.OnClickListener { _, _ ->  finish() }
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Activity.showAlertDialog(
        message: String,
        positiveListener: DialogInterface.OnClickListener,
        negativeListener: DialogInterface.OnClickListener
) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setNegativeButton(R.string.cancel, negativeListener)
    builder.setPositiveButton(R.string.ok, positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Activity.makeToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.makeSnackBar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAction("Action", null).show()
}

fun Activity.showAlertDialog(message: String) {
    val builder = AlertDialog.Builder(this)
    val positiveListener = DialogInterface.OnClickListener { _, _ -> return@OnClickListener }
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    val alert = builder.create()
    alert.show()
}

fun Activity.showAlertDialog(
        message: String,
        imagePath: String,
        listener: ThumbnailExplorerActivity.PositiveListener
) {
    val positiveListener = DialogInterface.OnClickListener { _, _ ->
        listener.register()
        return@OnClickListener
    }
    launchDialog(message, imagePath, positiveListener)
}

fun Activity.showAlertDialog(
        message: String,
        imagePath: String,
        listener: FileExplorerActivity.PositiveListener
) {
    val positiveListener = DialogInterface.OnClickListener { _, _ ->
        listener.register()
        return@OnClickListener
    }
    launchDialog(message, imagePath, positiveListener)
}

fun Activity.launchDialog(message: String, imagePath: String, positiveListener: DialogInterface.OnClickListener) {
    val builder = AlertDialog.Builder(this)
    val negativeListener = DialogInterface.OnClickListener { _, _ -> return@OnClickListener }
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setPositiveButton(getString(R.string.ok), positiveListener)
    builder.setNegativeButton(getString(R.string.cancel), negativeListener)

    val displayMetrics = resources.displayMetrics
    val size = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)
    val bitmap = BitmapUtils.decodeFileMaxWidthHeight(imagePath, size / 2)
    val thumbnail = ImageView(this)
    thumbnail.setImageBitmap(bitmap)
    thumbnail.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
    builder.setView(thumbnail)
    val alert = builder.create()
    alert.show()
}