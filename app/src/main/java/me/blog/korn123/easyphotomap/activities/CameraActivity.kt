package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.extensions.config
import me.blog.korn123.easyphotomap.extensions.showConfirmDialogWithFinish
import me.blog.korn123.easyphotomap.helper.*
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-08-20.
 */
class CameraActivity : SimpleActivity() {
    private var mFileUri: Uri? = null
    private var mMediaStorageDir: File? = null
    private var mCurrentFileName: String? = null
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        mFileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE) // create a file to save the image
        when (mFileUri != null) {
            true -> {
                // create Intent to take a picture and return control to the calling application
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri) // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
            false -> showConfirmDialogWithFinish("Please try again later.")
        }
    }

    /** Create a file Uri for saving an image or video  */
//    private fun getOutputMediaFileUri(type: Int): Uri = Uri.fromFile(getOutputMediaFile(type))
    private fun getOutputMediaFileUri(type: Int): Uri? {
        val targetFile = getOutputMediaFile(type)
        var targetUri: Uri? = null
        if (targetFile != null) {
            targetUri = FileProvider.getUriForFile(this@CameraActivity,  "$packageName.provider", targetFile) 
        }
        return targetUri
    } 

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        mMediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "aaf-easyphotomap")
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mMediaStorageDir?.let {
            if (!it.exists()) {
                if (!it.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        return when(type) {
            MEDIA_TYPE_IMAGE -> {
                mCurrentFileName = mMediaStorageDir?.path + File.separator + "IMG_" + timeStamp + ".jpg"
                File(mCurrentFileName)
            }
            MEDIA_TYPE_VIDEO -> {
                File(mMediaStorageDir?.path + File.separator + "VID_" + timeStamp + ".mp4")
            }
            else -> { null }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // Image captured and saved to mFileUri specified in the Intent
                    //                Toast.makeText(this, "Image saved to:\n" +
                    //                        mFileUri.toString(), Toast.LENGTH_LONG).show();
//                    val srcFilepath = StringUtils.substring(mFileUri?.toString(), 6)
                    val fileName: String
                    try {
                        val targetFile: File
                        if (config.enableCreateCopy) {
                            fileName = FilenameUtils.getName(mCurrentFileName) + ".origin"
                            targetFile = File(WORKING_DIRECTORY + fileName)
                            if (!targetFile.exists()) {
                                FileUtils.copyFile(File(mCurrentFileName), targetFile)
                            }
                        } else {
                            targetFile = File(mCurrentFileName)
                            fileName = FilenameUtils.getName(mCurrentFileName)
                        }
                        val exifInfo = CommonUtils.parseExifDescription(targetFile.absolutePath)
                        val item = PhotoMapItem()
                        item.imagePath = targetFile.absolutePath
                        if (exifInfo.date != null) {
                            item.date = CommonUtils.dateTimePattern.format(exifInfo.date)
                        } else {
                            item.date = getString(R.string.file_explorer_message2)
                        }

                        exifInfo.geoLocation?.let {
                            item.longitude = it.longitude
                            item.latitude = it.latitude
                            val listAddress = CommonUtils.getFromLocation(this@CameraActivity, item.latitude, item.longitude, 1, 0)
                            listAddress?.let { it ->
                                item.info = CommonUtils.fullAddress(it[0])
                            }

                            PhotoMapDbHelper.insertPhotoMapItem(item)
                            BitmapUtils.saveBitmap(targetFile.absolutePath, WORKING_DIRECTORY + fileName + ".thumb",PHOTO_MAP_THUMBNAIL_FIXED_WIDTH_HEIGHT, exifInfo.tagOrientation)
                            val intent = Intent(this@CameraActivity, MapsActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra(COLUMN_INFO, item.info)
                            intent.putExtra(COLUMN_IMAGE_PATH, item.imagePath)
                            intent.putExtra(COLUMN_LATITUDE, item.latitude)
                            intent.putExtra(COLUMN_LONGITUDE, item.longitude)
                            intent.putExtra(COLUMN_DATE, item.date)
                            startActivity(intent)
                            finish()
                        } ?: showConfirmDialogWithFinish(getString(R.string.camera_activity_message1))
                    } catch (e: Exception) {
                        showConfirmDialogWithFinish(e.message ?: "Please try again later.")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    finish()
                }
                else -> {}
            }
        }
    }
}
