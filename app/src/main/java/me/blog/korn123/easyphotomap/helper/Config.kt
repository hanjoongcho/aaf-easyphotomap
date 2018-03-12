package me.blog.korn123.easyphotomap.helper

import android.content.Context
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_MODIFIED
import com.simplemobiletools.commons.helpers.SORT_BY_NAME
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import io.github.hanjoongcho.commons.helpers.BaseConfig

/**
 * Created by CHO HANJOONG on 2017-12-24.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, FONT_SIZE_MEDIUM)
        set(size) = prefs.edit().putInt(FONT_SIZE, size).apply()
    
    var enableDateFilter: Boolean
        get() = legacyPrefs.getBoolean(DATE_FILTER, false)
        set(isEnable) = legacyPrefs.edit().putBoolean(DATE_FILTER, isEnable).apply()
    
    var disableCameraInformation: Boolean
        get() = legacyPrefs.getBoolean(DISABLE_INFO_POPUP, false)
        set(isDisable) = legacyPrefs.edit().putBoolean(DISABLE_INFO_POPUP, isDisable).apply()

    var enableCreateCopy: Boolean
        get() = legacyPrefs.getBoolean(ENABLE_CREARE_COPY, false)
        set(isEnable) = legacyPrefs.edit().putBoolean(ENABLE_CREARE_COPY, isEnable).apply()

    var photoMarkerIcon: Int
        get() = prefs.getInt(PHOTO_MARKER_ICON, BASIC)
        set(photoMarkerIcon) = prefs.edit().putInt(PHOTO_MARKER_ICON, photoMarkerIcon).apply()

    var photoMarkerScale: Int
        get() = prefs.getInt(PHOTO_MARKER_SCALE, SCALE_DEFAULT)
        set(photoMarkerScale) = prefs.edit().putInt(PHOTO_MARKER_SCALE, photoMarkerScale).apply()
    
    var photoMarkerMinimumCluster: Int
        get() = legacyPrefs.getInt(PHOTO_MARKER_MINIMUN_CLUSTER, CLUSTER_L2)
        set(photoMarkerMinimumCluster) = legacyPrefs.edit().putInt(PHOTO_MARKER_MINIMUN_CLUSTER, photoMarkerMinimumCluster).apply()

    var fileSorting: Int
        get() = prefs.getInt(SORT_ORDER, SORT_BY_DATE_MODIFIED or SORT_DESCENDING)
        set(order) = prefs.edit().putInt(SORT_ORDER, order).apply()
    
    var directorySorting: Int
        get() = prefs.getInt(DIRECTORY_SORT_ORDER, SORT_BY_NAME or SORT_DESCENDING)
        set(order) = prefs.edit().putInt(DIRECTORY_SORT_ORDER, order).apply()
}