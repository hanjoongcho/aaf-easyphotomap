package me.blog.korn123.easyphotomap.constant;

import android.os.Environment;

/**
 * Created by CHO HANJOONG on 2016-07-23.
 */
public class Constant {
    final static public String WORKING_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AAFactory/photo/";
    final static public String LOG_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AAFactory/log/";
    final static public String CAMERA_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM";
    final static public String CAMERA_DIRECTORY_INTERNAL = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
    final static public String THUMBNAIL_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/.thumbnails";
    final static public String PHOTO_DATA_PATH = WORKING_DIRECTORY + "photodata.dat";

    final static public float GOOGLE_MAP_MAX_ZOOM_IN_VALUE = 18.0F;
    final static public float GOOGLE_MAP_DEFAULT_ZOOM_VALUE = 13.0F;
    final static public double GOOGLE_MAP_DEFAULT_LATITUDE = 36.4372046;
    final static public double GOOGLE_MAP_DEFAULT_LONGITUDE = 127.8896971;
}
