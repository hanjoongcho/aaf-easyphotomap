package me.blog.korn123.easyphotomap.utils

import android.content.Context
import android.location.Location
import android.location.LocationManager

/**
 * Created by hanjoong on 2017-06-01.
 */

object GPSUtils {

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 3
    private val MIN_TIME_BW_UPDATES = (1000 * 3).toLong()

    private var mLocationManagerWithGPS: LocationManager? = null
    private var mLocationManagerWithNetwork: LocationManager? = null

    private fun getGPSProvider(context: Context): LocationManager {
        if (mLocationManagerWithGPS == null) {
            mLocationManagerWithGPS = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return mLocationManagerWithGPS!!
    }

    private fun getNetworkProvider(context: Context): LocationManager {
        if (mLocationManagerWithNetwork == null) {
            mLocationManagerWithNetwork = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return mLocationManagerWithNetwork!!
    }

    fun getLocationWithGPSProvider(context: Context): Location? {
        var location: Location? = getGPSProvider(context).getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location == null) {
            //Toast.makeText(context, "gps provider unknown", Toast.LENGTH_SHORT);
            location = getNetworkProvider(context).getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        return location
    }

    fun getLocationWithNetworkProvider(context: Context): Location {
        var location: Location? = getNetworkProvider(context).getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location == null) {
            location = getGPSProvider(context).getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        return location!!
    }

}
