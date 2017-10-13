package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
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
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.time.StopWatch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by CHO HANJOONG on 2016-09-11.
 */
class BatchPopupActivity : Activity() {

    private var mProgressBar: ProgressBar? = null
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
        mProgressBar = findViewById(R.id.progressBar) as ProgressBar
        infoText.typeface = Typeface.DEFAULT
        infoText2.typeface = Typeface.DEFAULT
        infoText3.typeface = Typeface.DEFAULT
        infoText4.typeface = Typeface.DEFAULT
        infoText5.typeface = Typeface.DEFAULT
        infoText6.typeface = Typeface.DEFAULT

        var listImagePath = when (intent.getStringExtra("currentPath") == null) {
            true -> intent.getStringArrayListExtra("listImagePath")
            false -> {
                val targetPath = intent.getStringExtra("currentPath")
                determinePhotoFiles(File(targetPath))
                listFilePath
            }
        }

        mTotalPhoto = listImagePath!!.size
        val thread = RegisterThread(this@BatchPopupActivity, listImagePath)
        mProgressBar!!.max = mTotalPhoto
        thread.start()
        stop.setOnClickListener({ _ -> mEnableUpdate = false })
        close.setOnClickListener({ _ -> finish() })
    }

    val listFilePath: java.util.ArrayList<String> = arrayListOf()
    private fun determinePhotoFiles(directory: File) {
        directory.listFiles().map { file ->
            when (file.isFile) {
                true -> {
                    if (file.absoluteFile.extension.toLowerCase().matches("jpg|jpeg".toRegex())) {
                        listFilePath.add(file.absolutePath)
                        println(file.absolutePath)
                    }
                }
                false -> determinePhotoFiles(file)
            }
        }
    }

    internal var progressHandler = Handler(Handler.Callback {
        infoText.text = (++mProgressStatus).toString() + " / " + mTotalPhoto
        infoText2.text = getString(R.string.batch_popup_message2) + ": " + mSuccessCount
        infoText3.text = getString(R.string.batch_popup_message3) + ": " + mReduplicationCount
        infoText4.text = getString(R.string.batch_popup_message4) + ": " + mNoGPSInfoCount
        infoText5.text = getString(R.string.batch_popup_message5) + ": " + mFailCount
        mProgressBar?.progress = mProgressStatus
        true
    })

    internal inner class RegisterThread(var context: Context, var listImagePath: ArrayList<String>) : Thread() {

        override fun run() {
            for (imagePath in listImagePath) {
                val stopWatch = StopWatch()
                stopWatch.start()
                if (!mEnableUpdate) return
                val message = progressHandler.obtainMessage()
                try {
                    var fileName = FilenameUtils.getName(imagePath) + ".origin"
                    var targetFile: File? = null
                    if (CommonUtils.loadBooleanPreference(this@BatchPopupActivity, "enable_create_copy")) {
                        targetFile = File(Constant.WORKING_DIRECTORY + fileName)
                        if (!targetFile.exists()) {
                            FileUtils.copyFile(File(imagePath), targetFile)
                        }
                    } else {
                        targetFile = File(imagePath)
                        // remove .origin extension
                        fileName = FilenameUtils.getBaseName(fileName)
                    }

                    val metadata = JpegMetadataReader.readMetadata(targetFile)
                    val item = PhotoMapItem()
                    item.imagePath = targetFile.absolutePath
                    val exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                    val date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault())
                    if (date != null) {
                        item.date = CommonUtils.dateTimePattern.format(date)
                    } else {
                        item.date = getString(R.string.file_explorer_message2)
                    }
                    Log.i("elapsed", String.format("meta %d", stopWatch.time))
                    stopWatch.reset()
                    stopWatch.start()
                    val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                    if (gpsDirectory != null && gpsDirectory.geoLocation != null) {
                        item.longitude = gpsDirectory.geoLocation.longitude
                        item.latitude = gpsDirectory.geoLocation.latitude
                        val listAddress = CommonUtils.getFromLocation(this@BatchPopupActivity, item.latitude, item.longitude, 1, 0)
                        if (listAddress!!.size > 0) {
                            item.info = CommonUtils.fullAddress(listAddress[0])
                        }
                        Log.i("elapsed", String.format("geo coding %d", stopWatch.time))
                        val tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath!!)
                        stopWatch.reset()
                        stopWatch.start()
                        if (tempList.size > 0) {
                            mReduplicationCount++
                        } else {
                            PhotoMapDbHelper.insertPhotoMapItem(item)
                            BitmapUtils.createScaledBitmap(targetFile.absolutePath, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200)
                            mSuccessCount++
                            Log.i("elapsed", String.format("create bitmap %d", stopWatch.time))
                            stopWatch.stop()
                        }
                    } else {
                        mNoGPSInfoCount++
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
