package me.blog.korn123.easyphotomap.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.beardedhen.androidbootstrap.TypefaceProvider
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import kotlinx.android.synthetic.main.activity_batch_popup.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import me.blog.korn123.easyphotomap.extensions.config
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.time.StopWatch
import java.io.File
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-09-11.
 */
class BatchPopupActivity : SimpleActivity() {

    private var mEnableUpdate = true
    private var mTotalPhoto = 0
    private var mSuccessCount = 0
    private var mFailCount = 0
    private var mNoGPSInfoCount = 0
    private var mReduplicationCount = 0
    private var mProgressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TypefaceProvider.registerDefaultIconSets()
        setContentView(R.layout.activity_batch_popup)
        infoText.typeface = Typeface.DEFAULT
        infoText2.typeface = Typeface.DEFAULT
        infoText3.typeface = Typeface.DEFAULT
        infoText4.typeface = Typeface.DEFAULT
        infoText5.typeface = Typeface.DEFAULT
        infoText6.typeface = Typeface.DEFAULT

        val listImagePath = when (intent.getStringExtra("currentPath") == null) {
            true -> intent.getStringArrayListExtra("listImagePath")
            false -> {
                val targetPath = intent.getStringExtra("currentPath")
                determinePhotoFiles(File(targetPath))
                listFilePath
            }
        }

        mTotalPhoto = listImagePath.size
        when (mTotalPhoto > 0) {
            true -> {
                if (!File(Constant.WORKING_DIRECTORY).exists()) {
                    File(Constant.WORKING_DIRECTORY).mkdirs()
                }
                val thread = RegisterThread(this@BatchPopupActivity, listImagePath)
                progressBar.max = mTotalPhoto
                thread.start()
                stop.setOnClickListener({ _ -> mEnableUpdate = false })
                close.setOnClickListener({ _ -> finish() })
            }
            false -> {
                val builder = AlertDialog.Builder(this@BatchPopupActivity)
                val positiveListener = DialogInterface.OnClickListener { _, _ ->
                    finish()
                    return@OnClickListener
                }
                builder.setMessage(getString(R.string.file_explorer_message9))
                builder.setPositiveButton(getString(R.string.confirm), positiveListener)
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
    }

    private val listFilePath: java.util.ArrayList<String> = arrayListOf()
    private fun determinePhotoFiles(directory: File) {
        directory.listFiles()?.map { file ->
            when (file.isFile) {
                true -> {
                    if (file.absoluteFile.extension.toLowerCase().matches("jpg|jpeg".toRegex())) {
                        listFilePath.add(file.absolutePath)
//                        println(file.absolutePath)
                    }
                }
                false -> determinePhotoFiles(file)
            }
        }
    }

    internal var progressHandler = Handler(Handler.Callback {
        infoText.text = "${++mProgressStatus} / $mTotalPhoto"
        infoText2.text = "${getString(R.string.batch_popup_message2)}: $mSuccessCount"
        infoText3.text = "${getString(R.string.batch_popup_message3)}: $mReduplicationCount"
        infoText4.text = "${getString(R.string.batch_popup_message4)}: $mNoGPSInfoCount"
        infoText5.text = "${getString(R.string.batch_popup_message5)}: $mFailCount"
        progressBar.progress = mProgressStatus
        true
    })

    internal inner class RegisterThread(var context: Context, private var listImagePath: ArrayList<String>) : Thread() {

        override fun run() {
            val stopWatch: StopWatch = StopWatch() 
            for (imagePath in listImagePath) {
                
                if (!mEnableUpdate) return
                val message = progressHandler.obtainMessage()
                try {
                    var fileName = FilenameUtils.getName(imagePath) + ".origin"
                    var targetFile: File?
                    if (config.enableCreateCopy) {
                        targetFile = File(Constant.WORKING_DIRECTORY + fileName)
                        if (!targetFile.exists()) {
                            FileUtils.copyFile(File(imagePath), targetFile)
                        }
                    } else {
                        targetFile = File(imagePath)
                        // remove .origin extension
                        fileName = FilenameUtils.getBaseName(fileName)
                    }


                    if (PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", targetFile.absolutePath).size > 0) {
                        mReduplicationCount++
                    } else {
                        stopWatch.start()
                        val metadata = JpegMetadataReader.readMetadata(targetFile)
                        Log.i("stopwatch", "readMetadata ${stopWatch.time} $targetFile")
                        stopWatch.reset()
                        val item = PhotoMapItem()
                        item.imagePath = targetFile.absolutePath
                        val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                        val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
                        if (date != null) {
                            item.date = CommonUtils.dateTimePattern.format(date)
                        } else {
                            item.date = getString(R.string.file_explorer_message2)
                        }
                        val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                        if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                            item.longitude = gpsDirectory.geoLocation.longitude
                            item.latitude = gpsDirectory.geoLocation.latitude
                            stopWatch.start()
                            val listAddress = CommonUtils.getFromLocation(this@BatchPopupActivity, item.latitude, item.longitude, 1, 0)
                            Log.i("stopwatch", "getFromLocation ${stopWatch.time} $targetFile")
                            stopWatch.reset()
                            listAddress?.let {
                                if (it.isNotEmpty()) item.info = CommonUtils.fullAddress(it[0])
                            }
                            PhotoMapDbHelper.insertPhotoMapItem(item)
                            val srcPath = targetFile.absolutePath
                            Thread(Runnable {
                                BitmapUtils.createScaledBitmap(srcPath, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200)
                            }).start()
                            mSuccessCount++
                        } else {
                            mNoGPSInfoCount++
                        }
                    }
                } catch (e: Exception) {
                    mFailCount++
                }
                progressHandler.sendMessage(message)
            }
        }
    }

    override fun onBackPressed() {
        DialogUtils.showAlertDialog(this@BatchPopupActivity, getString(R.string.batch_popup_message6))
    }

}
