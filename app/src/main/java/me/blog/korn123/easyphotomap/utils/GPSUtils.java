package me.blog.korn123.easyphotomap.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by hanjoong on 2017-06-01.
 */

public class GPSUtils {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3;

    private static LocationManager mLocationManagerWithGPS;
    private static LocationManager mLocationManagerWithNetwork;

    private static LocationManager getGPSProvider(Context context) {
        if (mLocationManagerWithGPS == null) {
            mLocationManagerWithGPS = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            if (mLocationManagerWithGPS.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManagerWithGPS.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {

                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        });
            }
        }
        return mLocationManagerWithGPS;
    }

    private static LocationManager getNetworkProvider(Context context) {
        if (mLocationManagerWithNetwork == null) {
            mLocationManagerWithNetwork = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            if (mLocationManagerWithNetwork.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManagerWithNetwork.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {

                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {

                            }
                        });
            }
        }
        return mLocationManagerWithNetwork;
    }

    public static Location getLocationWithGPSProvider(Context context) {
        Location location = getGPSProvider(context).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            //Toast.makeText(context, "gps provider unknown", Toast.LENGTH_SHORT);
            location = getNetworkProvider(context).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

}
