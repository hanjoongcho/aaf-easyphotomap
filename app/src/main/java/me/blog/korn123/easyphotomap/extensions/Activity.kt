package me.blog.korn123.easyphotomap.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import me.blog.korn123.easyphotomap.R

/**
 * Created by CHO HANJOONG on 2018-02-25.
 */

fun Activity.showConfirmDialog(message: String) {
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.confirm), null)
    val alert = builder.create()
    alert.show()
}

fun Activity.showConfirmDialogWithFinish(message: String) {
    val positiveListener = DialogInterface.OnClickListener { _, _ ->  finish() }
    val builder = AlertDialog.Builder(this)
    builder.setMessage(message)
    builder.setCancelable(false)
    builder.setPositiveButton(getString(R.string.confirm), positiveListener)
    val alert = builder.create()
    alert.show()
}