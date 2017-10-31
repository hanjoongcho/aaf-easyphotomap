package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-08-20.
 */
class CameraActivity : Activity() {


    private var mFileUri: Uri? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        mFileUri = getOutputMediaFileUri(Constant.MEDIA_TYPE_IMAGE) // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri) // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, Constant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
    }

    /** Create a file Uri for saving an image or video  */
    private fun getOutputMediaFileUri(type: Int): Uri = Uri.fromFile(getOutputMediaFile(type))

    /** Create a File for saving an image or video  */
    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp")
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        return when(type) {
            Constant.MEDIA_TYPE_IMAGE -> {
                File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")
            }
            Constant.MEDIA_TYPE_VIDEO -> {
                File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
            }
            else -> {null}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // Image captured and saved to mFileUri specified in the Intent
                    //                Toast.makeText(this, "Image saved to:\n" +
                    //                        mFileUri.toString(), Toast.LENGTH_LONG).show();
                    val srcFilepath = StringUtils.substring(mFileUri?.toString(), 6)
                    val fileName: String
                    try {
                        val targetFile: File
                        if (CommonUtils.loadBooleanPreference(this@CameraActivity, "enable_create_copy")) {
                            fileName = FilenameUtils.getName(srcFilepath) + ".origin"
                            targetFile = File(Constant.WORKING_DIRECTORY + fileName)
                            if (!targetFile.exists()) {
                                FileUtils.copyFile(File(srcFilepath), targetFile)
                            }
                        } else {
                            targetFile = File(srcFilepath)
                            fileName = FilenameUtils.getName(srcFilepath)
                        }
                        val metadata = JpegMetadataReader.readMetadata(targetFile)
                        val entity = PhotoMapItem()
                        entity.imagePath = targetFile.absolutePath
                        val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                        if (exifSubIFDDirectory != null) {
                            val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
                            if (date != null) {
                                entity.date = CommonUtils.dateTimePattern.format(date)
                            } else {
                                entity.date = getString(R.string.file_explorer_message2)
                            }
                        } else {
                            entity.date = getString(R.string.file_explorer_message2)
                        }

                        val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                        if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                            entity.longitude = gpsDirectory.geoLocation.longitude
                            entity.latitude = gpsDirectory.geoLocation.latitude
                            val listAddress = CommonUtils.getFromLocation(this@CameraActivity, entity.latitude, entity.longitude, 1, 0)
                            listAddress?.let { it ->
                                entity.info = CommonUtils.fullAddress(it[0])
                            }

                            PhotoMapDbHelper.insertPhotoMapItem(entity)
                            BitmapUtils.createScaledBitmap(targetFile.absolutePath, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200)
                            val intent = Intent(this@CameraActivity, MapsActivity::class.java)
                            intent.putExtra("info", entity.info)
                            intent.putExtra("imagePath", entity.imagePath)
                            intent.putExtra("latitude", entity.latitude)
                            intent.putExtra("longitude", entity.longitude)
                            intent.putExtra("date", entity.date)
                            startActivity(intent)
                        } else {
                            DialogUtils.makeToast(this, getString(R.string.camera_activity_message1))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Activity.RESULT_CANCELED -> {}
                else -> {}
            }
            finish()
        }
    }

}
