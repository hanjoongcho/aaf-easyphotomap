package me.blog.korn123.easyphotomap.helper

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.activities.AddressSearchActivity
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.extensions.config
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.util.*

/**
 * Created by CHO HANJOONG on 2017-09-16.
 */

class RegistrationThread(private val mContext: Context, private val mActivity: Activity, private val mProgressDialog: ProgressDialog, private var mFileName: String?, private val mPath: String) : Thread() {

    private fun registerSingleFile() {
        var resultMessage: String? = null
        try {

            val targetFile: File
            if (mContext.config.enableCreateCopy) {
                targetFile = File(Constant.WORKING_DIRECTORY + mFileName)
                if (!targetFile.exists()) {
                    FileUtils.copyFile(File(mPath), targetFile)
                }
            } else {
                targetFile = File(mPath)
                // remove .origin extension
                mFileName = FilenameUtils.getBaseName(mFileName)
            }

            val metadata = JpegMetadataReader.readMetadata(targetFile)
            val item = PhotoMapItem()
            item.imagePath = targetFile.absolutePath
            val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
            val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
            if (date != null) {
                item.date = CommonUtils.dateTimePattern.format(date)
            } else {
                item.date = mContext.getString(R.string.file_explorer_message2)
            }

            val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                item.longitude = gpsDirectory.geoLocation.longitude
                item.latitude = gpsDirectory.geoLocation.latitude
                val listAddress = CommonUtils.getFromLocation(mContext, item.latitude, item.longitude, 1, 0)
                listAddress?.let {
                    if (it.isNotEmpty()) item.info = CommonUtils.fullAddress(listAddress[0])
                }

                val tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath)
                resultMessage = when (tempList.isNotEmpty()) {
                    true -> mContext.getString(R.string.file_explorer_message3)
                    false -> {
                        PhotoMapDbHelper.insertPhotoMapItem(item)
                        BitmapUtils.createScaledBitmap(targetFile.absolutePath, Constant.WORKING_DIRECTORY + mFileName + ".thumb", 200)
                        mContext.getString(R.string.file_explorer_message4)
                    }
                }
            } else {
                // does not exits gps data
                Handler(Looper.getMainLooper()).post {
                    mProgressDialog.dismiss()
                    val addressIntent = Intent(mContext, AddressSearchActivity::class.java)
                    val builder = AlertDialog.Builder(mContext)
                    builder.setMessage(mContext.getString(R.string.file_explorer_message1)).setCancelable(false).setPositiveButton(mContext.getString(R.string.confirm),
                            DialogInterface.OnClickListener { _, _ ->
                                addressIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                addressIntent.putExtra("imagePath", item.imagePath)
                                addressIntent.putExtra("date", item.date)
                                mContext.startActivity(addressIntent)
                                return@OnClickListener
                            }).setNegativeButton(mContext.getString(R.string.cancel),
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
                DialogUtils.makeSnackBar(mActivity.findViewById(android.R.id.content), it)
            }
        }
    }

    override fun run() {
        registerSingleFile()
    }

}
