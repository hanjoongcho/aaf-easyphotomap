package me.blog.korn123.easyphotomap.models

import com.simplemobiletools.commons.helpers.AlphanumericComparator
import com.simplemobiletools.commons.helpers.SORT_BY_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_SIZE
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
    var length: Long = 0
    var takenDate = StringUtils.EMPTY

    fun setImagePathAndFileName(imagePath: String) {
        this.imagePath = imagePath
        this.fileName = FilenameUtils.getName(imagePath)
    }

    override fun toString(): String = fileName

    override fun compareTo(other: FileItem): Int {
        var result = when {
            sorting and SORT_BY_NAME != 0 -> AlphanumericComparator().compare(fileName.toLowerCase(), other.fileName.toLowerCase())
            sorting and SORT_BY_SIZE != 0 -> when {
                length == other.length -> 0
                length > other.length -> 1
                else -> -1
            }
            else -> takenDate.compareTo(other.takenDate)
        }
        
        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }
        
        return result
    } 
}
