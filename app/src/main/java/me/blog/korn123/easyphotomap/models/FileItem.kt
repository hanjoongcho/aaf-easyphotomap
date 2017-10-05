package me.blog.korn123.easyphotomap.models

import org.apache.commons.io.FilenameUtils

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
class FileItem : Comparable<FileItem> {

    var imagePath: String? = null
    var fileName: String? = null
    var isDirectory: Boolean = false

    fun setImagePathAndFileName(imagePath: String) {
        this.imagePath = imagePath
        this.fileName = FilenameUtils.getName(imagePath)
    }

    override fun toString(): String {
        return fileName!!
    }

    override fun compareTo(entity: FileItem): Int {
        return fileName!!.compareTo(entity.fileName!!)
    }

}
