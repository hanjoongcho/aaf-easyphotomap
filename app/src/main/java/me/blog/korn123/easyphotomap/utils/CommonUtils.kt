package me.blog.korn123.easyphotomap.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.drew.imaging.jpeg.JpegMetadataReader
import com.drew.imaging.jpeg.JpegProcessingException
import com.drew.metadata.exif.GpsDirectory
import me.blog.korn123.easyphotomap.models.ThumbnailItem
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-21.
 */
class CommonUtils {

    companion object {

        @JvmStatic @Volatile private var mGeoCoder: Geocoder? = null

        @JvmStatic val dateTimePattern = SimpleDateFormat("yyyy-MM-dd(EEE) HH:mm", Locale.getDefault())

        private val MAX_RETRY = 5

        fun getGeoCoderInstance(context: Context): Geocoder {
            if (mGeoCoder == null) {
                mGeoCoder = Geocoder(context, Locale.getDefault())
            }
            return mGeoCoder!!;
        }

        @JvmStatic
        @Throws(Exception::class)
        fun getFromLocation(context: Context, latitude: Double, longitude: Double, maxResults: Int, retryCount: Int): List<Address>? {
            var latitude = latitude
            var longitude = longitude
            var retryCount = retryCount
            latitude = java.lang.Double.parseDouble(String.format("%.6f", latitude))
            longitude = java.lang.Double.parseDouble(String.format("%.7f", longitude))
            var listAddress: List<Address>? = null
            try {
                listAddress = getGeoCoderInstance(context).getFromLocation(latitude, longitude, maxResults)
            } catch (e: Exception) {
                if (retryCount < MAX_RETRY) {
                    return getFromLocation(context, latitude, longitude, maxResults, ++retryCount)
                }
                throw Exception(e.message)
            }

            return listAddress
        }

        @JvmStatic
        @Throws(Exception::class)
        fun getFromLocationName(context: Context, locationName: String, maxResults: Int, retryCount: Int): List<Address>? {
            var retryCount = retryCount
            val geoCoder = Geocoder(context, Locale.getDefault())
            var listAddress: List<Address>? = null
            try {
                listAddress = geoCoder.getFromLocationName(locationName, maxResults)
            } catch (e: Exception) {
                if (retryCount < MAX_RETRY) {
                    return getFromLocationName(context, locationName, maxResults, ++retryCount)
                }
                throw Exception(e.message)
            }

            return listAddress
        }

        fun <K, V : Comparable<V>> entriesSortedByValues(map: Map<K, V>): List<Map.Entry<K, V>> {
            val sortedEntries = ArrayList(map.entries)
            Collections.sort(sortedEntries) { e1, e2 -> e2.value.compareTo(e1.value) }
            return sortedEntries
        }

        fun <K, V : Comparable<V>> entriesSortedByKeys(map: Map<K, V>): List<Map.Entry<K, V>> {
            val sortedEntries = ArrayList(map.entries)
            Collections.sort(sortedEntries) { e1, e2 -> e2.key.toString().compareTo(e1.key.toString()) }
            return sortedEntries
        }

        fun bindButtonEffect(targetView: View) {
            val onTouchListener = View.OnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(0x5fef1014)
                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    view.setBackgroundColor(0x00ffffff)
                }
                false
            }
            targetView.setOnTouchListener(onTouchListener)
        }

        @JvmStatic
        fun loadBooleanPreference(context: Context, key: String): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(key, false)
        }

        @JvmStatic
        fun saveBooleanPreference(context: Context, key: String, isEnable: Boolean) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = preferences.edit()
            edit.putBoolean(key, isEnable)
            edit.commit()
        }

        fun loadIntPreference(context: Context, key: String, defaultValue: Int): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(key, defaultValue)
        }

        fun loadStringPreference(context: Context, key: String, defaultValue: String): String {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(key, defaultValue)
        }

        @JvmStatic
        fun saveStringPreference(context: Context, key: String, value: String) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val edit = preferences.edit()
            edit.putString(key, value)
            edit.commit()
        }

        fun loadFontScaleFactor(activity: Activity): Float {
            val mPreferences = activity.getPreferences(Activity.MODE_PRIVATE)
            return mPreferences.getFloat("fontScaleFactor", 1.0f)
        }

        fun saveFontScaleFactor(activity: Activity, scaleFactor: Float) {
            val mPreferences = activity.getPreferences(Activity.MODE_PRIVATE)
            val edit = mPreferences.edit()
            edit.putFloat("fontScaleFactor", scaleFactor)
            edit.commit()
        }

        fun fetchThumbnailBy(context: Context, imageId: String): ThumbnailItem? {
            var photo: ThumbnailItem? = null
            val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
            val mSelectionArgs = arrayOf(imageId)
            val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID + " = ?",
                    mSelectionArgs,
                    MediaStore.Images.Thumbnails.DATA + " desc")
            val dataColumnIndex = imageCursor.getColumnIndex(projection[0])
            val idColumnIndex = imageCursor.getColumnIndex(projection[1])

            imageCursor.let {
                if (imageCursor.moveToFirst()) {
                    photo = ThumbnailItem(imageCursor.getString(idColumnIndex), "", imageCursor.getString(dataColumnIndex))
                }
                imageCursor.close()
            }

            return photo
        }

        @JvmStatic
        fun fetchAllThumbnail(context: Context): List<ThumbnailItem> {
            val projection = arrayOf(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID)
            val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection, null, null,
                    MediaStore.Images.Thumbnails.DATA + " desc")
            val result = ArrayList<ThumbnailItem>()

            imageCursor.let {
                when (imageCursor.moveToFirst()) {
                    true -> {
                        val dataColumnIndex = imageCursor.getColumnIndex(projection[0])
                        val idColumnIndex = imageCursor.getColumnIndex(projection[1])
                        do {
                            val filePath = imageCursor.getString(dataColumnIndex)
                            val imageId = imageCursor.getString(idColumnIndex)

                            //                Uri thumbnailUri = uriToThumbnail(context, imageId);
                            //                Uri imageUri = Uri.parse(filePath);
                            //                Log.i("fetchAllImages", imageUri.toString());
                            // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                            val photo = ThumbnailItem(imageId, "", filePath)
                            result.add(photo)
                        } while (imageCursor.moveToNext())
                        imageCursor.close()
                    }
                    false -> {
                        // imageCursor is empty
                    }
                }
            }
            return result
        }

        @JvmStatic
        fun fetchAllImages(context: Context): List<ThumbnailItem> {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            val projection = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID)
            val imageCursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                    projection, null, null,
                    MediaStore.Images.Media.DATA + " asc")        // DATA, _ID를 출력
            val result = ArrayList<ThumbnailItem>(imageCursor!!.count)

            imageCursor.let {
                when(imageCursor.moveToFirst()) {
                    true -> {
                        val dataColumnIndex = imageCursor.getColumnIndex(projection[0])
                        val idColumnIndex = imageCursor.getColumnIndex(projection[1])
                        do {
                            val filePath = imageCursor.getString(dataColumnIndex)
                            val imageId = imageCursor.getString(idColumnIndex)

                            //                Uri thumbnailUri = uriToThumbnail(context, imageId);
                            //                Uri imageUri = Uri.parse(filePath);
                            //                Log.i("fetchAllImages", imageUri.toString());
                            // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                            val photo = ThumbnailItem(imageId, filePath, "")
                            result.add(photo)
                        } while (imageCursor.moveToNext())
                        imageCursor.close()
                    }
                    false -> {
                        // imageCursor is empty
                    }
                }
            }
            return result
        }

        @JvmStatic
        fun getOriginImagePath(context: Context, imageId: String): String? {
            // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val contentResolver = context.contentResolver

            // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
            val cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Media._ID + "=?",
                    arrayOf(imageId), null)
            if (cursor == null) {
                // Error 발생
                // 적절하게 handling 해주세요
            } else if (cursor.moveToFirst()) {
                val thumbnailColumnIndex = cursor.getColumnIndex(projection[0])
                val path = cursor.getString(thumbnailColumnIndex)
                cursor.close()
                return path
            }
            return null
        }

        fun getGPSDirectory(filePath: String): GpsDirectory? {
            var gpsDirectory: GpsDirectory? = null
            try {
                val metadata = JpegMetadataReader.readMetadata(File(filePath))
                gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            } catch (e: JpegProcessingException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return gpsDirectory
        }

//    fun generateThumbnail(context: Context, imageId: String): Int {
//        var count = 0
//        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA)
//        val contentResolver = context.contentResolver
//        val thumbnailCursor = contentResolver.query(
//                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
//                projection, // DATA를 출력
//                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
//                arrayOf(imageId), null)
//        if (thumbnailCursor == null) {
//        } else if (thumbnailCursor.moveToFirst()) {
//        } else {
//            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, java.lang.Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null)
//            thumbnailCursor.close()
//            count = 1
//        }
//        return count
//    }

//    fun uriToThumbnail(context: Context, imageId: String): String? {
//        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
//        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA)
//        val contentResolver = context.contentResolver
//
//        // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
//        val thumbnailCursor = contentResolver.query(
//                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
//                projection, // DATA를 출력
//                MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
//                arrayOf(imageId), null)
//
//        if (thumbnailCursor == null) {
//            // Error 발생
//            // 적절하게 handling 해주세요
//        } else if (thumbnailCursor.moveToFirst()) {
//            val thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0])
//
//            val thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex)
//            thumbnailCursor.close()
//            return thumbnailPath
//        } else {
//            // thumbnailCursor가 비었습니다.
//            // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
//            // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
//            // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다
//            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, java.lang.Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null)
//            thumbnailCursor.close()
//            return uriToThumbnail(context, imageId)
//        }
//        return null
//    }

//    fun isMatchLine(dataPath: String, lineString: String): Boolean {
//        var isMatch = false
//        try {
//            val `is` = FileInputStream(File(dataPath))
//            val listLine = IOUtils.readLines(`is`, "UTF-8")
//            for (line in listLine) {
//                //                Log.i("isMatchLine", lineString.length() + "," + lineString);
//                //                Log.i("isMatchLine", line.length() + "," + line);
//                if (StringUtils.equals(line.trim { it <= ' ' }, lineString.trim { it <= ' ' })) {
//                    isMatch = true
//                    break
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return isMatch
//    }

        @JvmStatic
        fun fullAddress(address: Address): String {
            val sb = StringBuilder()
            if (address.countryName != null) sb.append(address.countryName).append(" ")
            if (address.adminArea != null) sb.append(address.adminArea).append(" ")
            if (address.locality != null) sb.append(address.locality).append(" ")
            if (address.subLocality != null) sb.append(address.subLocality).append(" ")
            if (address.thoroughfare != null) sb.append(address.thoroughfare).append(" ")
            if (address.featureName != null) sb.append(address.featureName).append(" ")
            return sb.toString()
        }

//    fun writeDataFile(data: String, targetPath: String, append: Boolean) {
//        if (append) {
//            appendDataFile(data, targetPath)
//        } else {
//            writeDataFile(data, targetPath)
//        }
//    }

//    private fun appendDataFile(data: String, targetPath: String) {
//        var writer: Writer? = null
//        var bufferedWriter: BufferedWriter? = null
//        try {
//            writer = FileWriterWithEncoding(File(targetPath), "UTF-8", true)
//            bufferedWriter = BufferedWriter(writer)
//            bufferedWriter.write(data)
//            bufferedWriter.flush()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            IOUtils.closeQuietly(bufferedWriter)
//            IOUtils.closeQuietly(writer)
//        }
//    }

        fun readDataFile(targetPath: String): List<String>? {
            var inputStream: InputStream? = null
            var listData: List<String>? = null
            try {
                inputStream = FileUtils.openInputStream(File(targetPath))
                listData = IOUtils.readLines(inputStream, "UTF-8")
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return listData
        }

//    private fun writeDataFile(data: String, targetPath: String) {
//        var outputStream: OutputStream? = null
//        try {
//            outputStream = FileOutputStream(File(targetPath))
//            IOUtils.write(data, outputStream, "UTF-8")
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            IOUtils.closeQuietly(outputStream)
//        }
//    }

        @JvmStatic
        fun dpToPixel(context: Context, dp: Float, policy: Int = 0): Int {
            val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
            var pixel = 0
            when (policy) {
                0 -> pixel = px.toInt()
                1 -> pixel = Math.round(px)
            }
            return pixel
        }

        @JvmStatic
        fun dpToPixel(context: Context, dp: Float): Int {
            return dpToPixel(context, dp, 0)
        }

        @JvmStatic
        fun getDisplayOrientation(activity: Activity): Int {
            val display = activity.windowManager.defaultDisplay
            return display.orientation
        }

        @JvmStatic
        fun getDefaultDisplay(activity: Activity): Point {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }

//    fun openPopupWindow(activity: Activity, anchorView: View): PopupWindow {
//        val popupWindow: PopupWindow
//        val popupView = activity.layoutInflater.inflate(R.layout.activity_photo_search, null)
//        popupWindow = PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        popupWindow.height = 100
//        popupWindow.showAsDropDown(anchorView)
//        return popupWindow
//    }
    }

}
