package me.blog.korn123.easyphotomap.models

import com.drew.lang.GeoLocation
import java.util.*

/**
 * Created by Administrator on 2018-03-19.
 */

data class ExifModel(val imagePath: String) {
    var tagOrientation: Int = 1
    var date: Date? = null
    var geoLocation: GeoLocation? = null
}