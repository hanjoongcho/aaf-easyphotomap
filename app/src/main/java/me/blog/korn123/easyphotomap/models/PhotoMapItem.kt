package me.blog.korn123.easyphotomap.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by CHO HANJOONG on 2017-09-12.
 */

open class PhotoMapItem : RealmObject(), Comparable<PhotoMapItem> {

    @PrimaryKey
    var sequence = 0
    var latitude = 0.0
    var longitude = 0.0
    var info: String? = null
    var imagePath: String? = null
    var date: String? = null
    var dateWithoutTime: String? = null
    var sortFlag = 0

    override fun toString(): String {
        if (sortFlag == 1) {
            info = date
        }
        return info!!
    }

    override fun compareTo(item: PhotoMapItem): Int {
        val result = -1
        when (sortFlag) {
            0 -> info!!.compareTo(item.info!!)
            1 -> dateWithoutTime!!.compareTo(item.dateWithoutTime!!)
        }
        return result
    }

}
