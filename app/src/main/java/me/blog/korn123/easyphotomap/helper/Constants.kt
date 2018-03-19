package me.blog.korn123.easyphotomap.helper

import android.os.Environment

/**
 * Created by CHO HANJOONG on 2017-12-24.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

var WORKING_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/AAFactory/photo/"
var CAMERA_DIRECTORY = Environment.getExternalStorageDirectory().absolutePath + "/DCIM"
var LEGACY_PHOTO_DATA_PATH = WORKING_DIRECTORY + "photodata.dat"

// shared preferences
const val SORT_ORDER = "sort_order"
const val DIRECTORY_SORT_ORDER = "directory_sort_order"
const val FONT_SIZE = "font_size"
const val DATE_FILTER ="date_filter_setting"
const val DISABLE_INFO_POPUP = "disable_info_popup"
const val ENABLE_CREARE_COPY = "enable_create_copy"
const val PHOTO_MARKER_ICON = "photo_marker_icon"
const val PHOTO_MARKER_MINIMUN_CLUSTER = "photo_marker_minimum_cluster"
const val PHOTO_MARKER_SCALE = "photo_marker_scale"

// photo marker cluster
const val CLUSTER_L1 = 1
const val CLUSTER_L2 = 2
const val CLUSTER_L3 = 3
const val CLUSTER_L4 = 4
const val CLUSTER_L5 = 5
const val CLUSTER_L6 = 6
const val CLUSTER_L7 = 7

// photo marker scale
const val SCALE_M4 = -4
const val SCALE_M3 = -3
const val SCALE_M2 = -2
const val SCALE_M1 = -1
const val SCALE_DEFAULT = 0
const val SCALE_P1 = 1
const val SCALE_P2 = 2 

// photo marker frame
const val BASIC = 0
const val FILM = 1
const val CIRCLE = 2
const val FLOWER = 3

// font sizes
const val FONT_SIZE_SMALL = 0
const val FONT_SIZE_MEDIUM = 1
const val FONT_SIZE_LARGE = 2
const val FONT_SIZE_EXTRA_LARGE = 3

const val COLUMN_IMAGE_PATH = "imagePath"
const val COLUMN_DATE = "date"
const val COLUMN_LATITUDE = "latitude"
const val COLUMN_LONGITUDE = "longitude"
const val COLUMN_INFO = "info"
const val COLUMN_TAG_ORIENTATION = "tagOrientation"

const val PHOTO_MAP_THUMBNAIL_FIXED_WIDTH_HEIGHT = 200
const val GOOGLE_MAP_MAX_ZOOM_IN_VALUE = 18.0f
const val GOOGLE_MAP_DEFAULT_ZOOM_VALUE = 13.0f
const val GOOGLE_MAP_DEFAULT_LATITUDE = 37.3997208
const val GOOGLE_MAP_DEFAULT_LONGITUDE = 127.1000782

const val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100
const val CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200
const val MEDIA_TYPE_IMAGE = 1
const val MEDIA_TYPE_VIDEO = 2
const val START_MAIN_ACTIVITY = 0
