package me.blog.korn123.easyphotomap.models

import android.net.Uri

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
class ThumbnailItem(imageId: String, imagePath: String, thumbnailPath: String) {

    var imageId: String? = null
    var imagePath: String? = null
    var thumbnailPath: String? = null

    init {
        this.imageId = imageId
        this.imagePath = imagePath
        this.thumbnailPath = thumbnailPath
    }

}
