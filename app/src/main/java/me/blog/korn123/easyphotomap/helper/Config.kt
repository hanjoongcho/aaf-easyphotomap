package me.blog.korn123.easyphotomap.helper

import android.content.Context
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
}