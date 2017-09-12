package me.blog.korn123.easyphotomap.camera;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.blog.korn123.easyphotomap.activities.MapsActivity;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.PhotoEntity;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-08-20.
 */
public class CameraActivity extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri fileUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_camera_activity);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
//                Toast.makeText(this, "Image saved to:\n" +
//                        fileUri.toString(), Toast.LENGTH_LONG).show();
                String srcFilepath = StringUtils.substring(fileUri.toString(), 6);
                String fileName = null;
                try {
                    File targetFile = null;
                    if (CommonUtils.loadBooleanPreference(CameraActivity.this, "enable_create_copy")) {
                        fileName = FilenameUtils.getName(srcFilepath) + ".origin";
                        targetFile = new File(Constant.WORKING_DIRECTORY + fileName);
                        if (!targetFile.exists()) {
                            FileUtils.copyFile(new File(srcFilepath), targetFile);
                        }
                    } else {
                        targetFile = new File(srcFilepath);
                        fileName = FilenameUtils.getName(srcFilepath);
                    }
                    Metadata metadata = JpegMetadataReader.readMetadata(targetFile);
                    PhotoEntity entity = new PhotoEntity();
                    entity.imagePath = targetFile.getAbsolutePath();
                    ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    if (exifSubIFDDirectory != null) {
                        Date date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
                        if (date != null) {
                            entity.date = CommonUtils.DATE_TIME_PATTERN.format(date);
                        } else {
                            entity.date = getString(R.string.file_explorer_message2);
                        }
                    } else {
                        entity.date = getString(R.string.file_explorer_message2);
                    }

                    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                        entity.longitude = gpsDirectory.getGeoLocation().getLongitude();
                        entity.latitude = gpsDirectory.getGeoLocation().getLatitude();
                        List<Address> listAddress = CommonUtils.getFromLocation(CameraActivity.this, entity.latitude, entity.longitude, 1, 0);
                        if (listAddress.size() > 0) {
                            entity.info = CommonUtils.fullAddress(listAddress.get(0));
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(entity.imagePath + "|");
                        sb.append(entity.info + "|");
                        sb.append(entity.latitude + "|");
                        sb.append(entity.longitude + "|");
                        sb.append(entity.date + "\n");
                        if (CommonUtils.isMatchLine(Constant.PHOTO_DATA_PATH, sb.toString())) {
                        } else {
                            CommonUtils.writeDataFile(sb.toString(), Constant.PHOTO_DATA_PATH, true);
                            CommonUtils.createScaledBitmap(targetFile.getAbsolutePath(), Constant.WORKING_DIRECTORY + fileName + ".thumb", 200);
                        }
                        Intent intent = new Intent(CameraActivity.this, MapsActivity.class);
                        intent.putExtra("info", entity.info);
                        intent.putExtra("imagePath", entity.imagePath);
                        intent.putExtra("latitude", entity.latitude);
                        intent.putExtra("longitude", entity.longitude);
                        intent.putExtra("date", entity.date);
                        startActivity(intent);
                    } else {
                        CommonUtils.makeToast(this, getString(R.string.camera_activity_message1));
                    }
                } catch (Exception e) {
                    AAFLogger.info("CameraActivity-onActivityResult INFO: exception is " + e, getClass());
                }

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
            }
            finish();
        }

//        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                // Video captured and saved to fileUri specified in the Intent
//                Toast.makeText(this, "Video saved to:\n" +
//                        data.getData(), Toast.LENGTH_LONG).show();
//            } else if (resultCode == RESULT_CANCELED) {
//                // User cancelled the video capture
//            } else {
//                // Video capture failed, advise user
//            }
//        }

    }
}
