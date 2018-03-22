package me.blog.korn123.easyphotomap.helper

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.activities.AddressSearchActivity
import me.blog.korn123.easyphotomap.extensions.config
import me.blog.korn123.easyphotomap.extensions.makeSnackBar
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File

/**
 * Created by CHO HANJOONG on 2017-09-16.
 */

class RegistrationThread(
        private val mActivity: Activity,
        private val mProgressDialog: ProgressDialog,
        private var mFileName: String?,
        private val mPath: String
) : Thread() {

    private fun registerSingleFile() {
        var resultMessage: String? = null
        try {

            val targetFile: File
            if (mActivity.config.enableCreateCopy) {
                targetFile = File(WORKING_DIRECTORY + mFileName)
                if (!targetFile.exists()) {
                    FileUtils.copyFile(File(mPath), targetFile)
                }
            } else {
                targetFile = File(mPath)
                // remove .origin extension
                mFileName = FilenameUtils.getBaseName(mFileName)
            }

            val exifInfo = CommonUtils.parseExifDescription(targetFile.absolutePath)
            val item = PhotoMapItem()
            item.imagePath = targetFile.absolutePath
            if (exifInfo.date != null) {
                item.date = CommonUtils.dateTimePattern.format(exifInfo.date)
            } else {
                item.date = mActivity.getString(R.string.file_explorer_message2)
            }

            exifInfo.geoLocation?.let {
                item.longitude = it.longitude
                item.latitude = it.latitude

                val listAddress = CommonUtils.getFromLocation(mActivity, item.latitude, item.longitude, 1, 0)
                listAddress?.let {
                    if (it.isNotEmpty()) item.info = CommonUtils.fullAddress(listAddress[0])
                }

                val tempList = PhotoMapDbHelper.selectPhotoMapItemBy(COLUMN_IMAGE_PATH, item.imagePath)
                resultMessage = when (tempList.isNotEmpty()) {
                    true -> mActivity.getString(R.string.file_explorer_message3)
                    false -> {
                        PhotoMapDbHelper.insertPhotoMapItem(item)
                        BitmapUtils.saveBitmap(targetFile.absolutePath, WORKING_DIRECTORY + mFileName + ".thumb", PHOTO_MAP_THUMBNAIL_FIXED_WIDTH_HEIGHT, exifInfo.tagOrientation)
                        mActivity.getString(R.string.file_explorer_message4)
                    }
                }
            }
            
            if (exifInfo.geoLocation == null) {
                // does not exits gps data
                Handler(Looper.getMainLooper()).post {
                    mProgressDialog.dismiss()
                    val addressIntent = Intent(mActivity, AddressSearchActivity::class.java)
                    val builder = AlertDialog.Builder(mActivity)
                    builder.setMessage(mActivity.getString(R.string.file_explorer_message1)).setCancelable(false).setPositiveButton(mActivity.getString(R.string.confirm),
                            DialogInterface.OnClickListener { _, _ ->
                                addressIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                addressIntent.putExtra(COLUMN_IMAGE_PATH, item.imagePath)
                                addressIntent.putExtra(COLUMN_DATE, item.date)
                                addressIntent.putExtra(COLUMN_TAG_ORIENTATION, exifInfo.tagOrientation)
                                mActivity.startActivity(addressIntent)
                                return@OnClickListener
                            }).setNegativeButton(mActivity.getString(R.string.cancel),
                            DialogInterface.OnClickListener { _, _ -> return@OnClickListener })
                    val alert = builder.create()
                    alert.show()
                }
            }
        } catch (e: Exception) {
            resultMessage = e.message
        }

        resultMessage?.let {
            Handler(Looper.getMainLooper()).post {
                mProgressDialog.dismiss()
                mActivity.makeSnackBar(mActivity.findViewById(android.R.id.content), it)
            }
        }
    }

    override fun run() {
        registerSingleFile()
    }
}
