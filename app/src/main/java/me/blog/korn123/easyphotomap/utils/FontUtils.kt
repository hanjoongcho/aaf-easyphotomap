package me.blog.korn123.easyphotomap.utils

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object FontUtils {

    fun setChildViewTypeface(viewGroup: ViewGroup) {
        repeat(viewGroup.childCount) { i ->
            if (viewGroup.getChildAt(i) is ViewGroup) {
                setChildViewTypeface(viewGroup.getChildAt(i) as ViewGroup)
            } else {
                if (viewGroup.getChildAt(i) is TextView) {
                    val tv = viewGroup.getChildAt(i) as TextView
                    tv.typeface = Typeface.DEFAULT
                }
            }
        }
    }
}
