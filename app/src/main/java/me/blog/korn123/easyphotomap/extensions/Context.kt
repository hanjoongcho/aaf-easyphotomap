package me.blog.korn123.easyphotomap.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.EditText
import com.simplemobiletools.commons.views.MyTextView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.helper.Config
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_EXTRA_LARGE
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_LARGE
import me.blog.korn123.easyphotomap.helper.FONT_SIZE_SMALL

/**
 * Created by CHO HANJOONG on 2018-01-09.
 */

/**
 * Created by CHO HANJOONG on 2017-12-24.
 * This code based 'Simple-Commons' package
 * You can see original 'Simple-Commons' from below link.
 * https://github.com/SimpleMobileTools/Simple-Commons
 */

fun Context.getTextSize() =
        when (config.fontSize) {
            FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
            FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
            FONT_SIZE_EXTRA_LARGE -> resources.getDimension(R.dimen.extra_big_text_size)
            else -> resources.getDimension(R.dimen.bigger_text_size)
        }

val Context.config: Config get() = Config.newInstance(applicationContext)

fun Context.initTextSize(viewGroup: ViewGroup, context: Context) {
    val cnt = viewGroup.childCount
    (0 until cnt)
            .map { viewGroup.getChildAt(it) }
            .forEach {
                when (it) {
                    is MyTextView ->  {
                        when (it.id != R.id.about_copyright) {
                            true -> it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
                            false -> {}
                        }
                    }
                    is EditText ->  it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
                    is ViewGroup -> initTextSize(it, context)
                }
            }
}

private fun Context.getGPSProvider(): LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

private fun Context.getNetworkProvider(): LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

@SuppressLint("MissingPermission")
fun Context.getLocationWithGPSProvider(): Location? {
    return getGPSProvider().getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: getNetworkProvider().getLastKnownLocation(LocationManager.NETWORK_PROVIDER) 
}