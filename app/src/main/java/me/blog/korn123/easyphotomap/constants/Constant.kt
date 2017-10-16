package me.blog.korn123.easyphotomap.constants

import android.os.Environment

/**
 * Created by CHO HANJOONG on 2016-07-23.
 */
object Constant {
    val WORKING_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/AAFactory/photo/"
    val LOG_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/AAFactory/log/"

    /* FileExplorerActivity */
    val CAMERA_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/DCIM"
    val SETTING_REVERSE_ORDER = "enable_reverse_order"

    /* MapsActivity */
    val LEGACY_PHOTO_DATA_PATH = WORKING_DIRECTORY + "photodata.dat"
    val GOOGLE_MAP_MAX_ZOOM_IN_VALUE = 18.0f
    val GOOGLE_MAP_DEFAULT_ZOOM_VALUE = 13.0f
    val GOOGLE_MAP_DEFAULT_LATITUDE = 37.3997208
    val GOOGLE_MAP_DEFAULT_LONGITUDE = 127.1000782

    /* CameraActivity */
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100
    val CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200
    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    /* IntroActivity */
    val START_MAIN_ACTIVITY = 0
}
