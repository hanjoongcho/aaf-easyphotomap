package me.blog.korn123.easyphotomap.utils

import android.content.res.AssetManager
import android.graphics.Typeface
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

object FontUtils {

    private var sTypeface: Typeface? = null

    fun setTypefaceDefault(view: TextView) {
        view.typeface = Typeface.DEFAULT
    }

    fun setTypeface(assetManager: AssetManager, view: TextView) {
        view.typeface = getTypeface(assetManager)
    }

    fun getTypeface(assetManager: AssetManager): Typeface {
        if (sTypeface == null) {
            sTypeface = Typeface.createFromAsset(assetManager, "fonts/NanumPen.ttf")
        }
        return sTypeface!!
    }

    fun setToolbarTypeface(toolbar: Toolbar, assetManager: AssetManager) {
        repeat(toolbar.childCount) { i ->
            val view = toolbar.getChildAt(i)
            if (view is TextView) {
                FontUtils.setTypeface(assetManager, view)
                //                ((TextView) view).setTypeface(Typeface.DEFAULT);
            }
        }
    }

    fun setToolbarTypeface(toolbar: Toolbar, typeface: Typeface) {
        repeat(toolbar.childCount) { i ->
            val view = toolbar.getChildAt(i)
            if (view is TextView) {
                view.typeface = typeface
            }
        }
    }

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
