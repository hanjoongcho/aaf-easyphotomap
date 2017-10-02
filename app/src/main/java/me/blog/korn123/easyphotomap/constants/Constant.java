package me.blog.korn123.easyphotomap.constants;

import android.os.Environment;

/**
 * Created by CHO HANJOONG on 2016-07-23.
 */
public class Constant {
    final static public String WORKING_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AAFactory/photo/";
    final static public String LOG_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AAFactory/log/";

    /* FileExplorerActivity */
    final static public String CAMERA_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM";

    /* MapsActivity */
    final static public String LEGACY_PHOTO_DATA_PATH = WORKING_DIRECTORY + "photodata.dat";
    final static public float GOOGLE_MAP_MAX_ZOOM_IN_VALUE = 18.0F;
    final static public float GOOGLE_MAP_DEFAULT_ZOOM_VALUE = 13.0F;
    final static public double GOOGLE_MAP_DEFAULT_LATITUDE = 37.3997208;
    final static public double GOOGLE_MAP_DEFAULT_LONGITUDE = 127.1000782;

    /* CameraActivity */
    final static public int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    final static public int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    final static public int MEDIA_TYPE_IMAGE = 1;
    final static public int MEDIA_TYPE_VIDEO = 2;

    /* IntroActivity */
    final static public int START_MAIN_ACTIVITY = 0;
}
