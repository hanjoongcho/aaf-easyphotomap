package me.blog.korn123.easyphotomap.models

import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
class FileItem : Comparable<FileItem> {

    companion object {
        var sorting: Int = 0
    }
    var imagePath: String = StringUtils.EMPTY
    var fileName: String = StringUtils.EMPTY
    var isDirectory: Boolean = false

    fun setImagePathAndFileName(imagePath: String) {
        this.imagePath = imagePath
        this.fileName = FilenameUtils.getName(imagePath)
    }

    override fun toString(): String = fileName

    override fun compareTo(other: FileItem): Int {
        var result = fileName.compareTo(other.fileName)
        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }
        return result
    } 
}
