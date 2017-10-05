package me.blog.korn123.easyphotomap.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Handler;
import android.os.Looper;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constants.Constant;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.activities.AddressSearchActivity;
import me.blog.korn123.easyphotomap.utils.BitmapUtils;
import me.blog.korn123.easyphotomap.utils.CommonUtils;
import me.blog.korn123.easyphotomap.utils.DialogUtils;

/**
 * Created by CHO HANJOONG on 2017-09-16.
 */

public class RegistrationThread extends Thread {
    private Context mContext;
    private Activity mActivity;
    private ProgressDialog mProgressDialog;
    private String mFileName;
    private String mPath;

    public RegistrationThread(Context context, Activity activity, ProgressDialog progressDialog, String fileName, String path) {
        this.mContext = context;
        this.mActivity = activity;
        this.mProgressDialog = progressDialog;
        this.mFileName = fileName;
        this.mPath = path;
    }

    public void registerSingleFile() {
        String resultMessage = "";
        try {

            File targetFile = null;
            if (CommonUtils.loadBooleanPreference(mContext, "enable_create_copy")) {
                targetFile = new File(Constant.WORKING_DIRECTORY + mFileName);
                if (!targetFile.exists()) {
                    FileUtils.copyFile(new File(mPath), targetFile);
                }
            } else {
                targetFile = new File(mPath);
                // remove .origin extension
                mFileName = FilenameUtils.getBaseName(mFileName);
            }

            Metadata metadata = JpegMetadataReader.readMetadata(targetFile);
            PhotoMapItem item = new PhotoMapItem();
            item.imagePath = targetFile.getAbsolutePath();
            ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
            if (date != null) {
                item.date = CommonUtils.getDateTimePattern().format(date);
            } else {
                item.date = mContext.getString(R.string.file_explorer_message2);
            }

            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                item.longitude = gpsDirectory.getGeoLocation().getLongitude();
                item.latitude = gpsDirectory.getGeoLocation().getLatitude();
                List<Address> listAddress = CommonUtils.getFromLocation(mContext, item.latitude, item.longitude, 1, 0);
                if (listAddress.size() > 0) {
                    item.info = CommonUtils.fullAddress(listAddress.get(0));
                }

                ArrayList<PhotoMapItem> tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath);
                if (tempList.size() > 0) {
                    resultMessage = mContext.getString(R.string.file_explorer_message3);
                } else {
                    PhotoMapDbHelper.insertPhotoMapItem(item);
                    BitmapUtils.INSTANCE.createScaledBitmap(targetFile.getAbsolutePath(), Constant.WORKING_DIRECTORY + mFileName + ".thumb", 200);
                    resultMessage = mContext.getString(R.string.file_explorer_message4);
                }
            } else {
                // does not exits gps data
                final PhotoMapItem temp = item;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                        final Intent addressIntent = new Intent(mContext, AddressSearchActivity.class);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(mContext.getString(R.string.file_explorer_message1)).setCancelable(false).setPositiveButton(mContext.getString(R.string.confirm),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        addressIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        addressIntent.putExtra("imagePath", temp.imagePath);
                                        addressIntent.putExtra("date", temp.date);
                                        mContext.startActivity(addressIntent);
                                        return;
                                    }
                                }).setNegativeButton(mContext.getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        } catch (Exception e) {
            resultMessage = e.getMessage();
        }
        final String message = resultMessage;
        if (StringUtils.isNotEmpty(message)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                    DialogUtils.INSTANCE.makeSnackBar(mActivity.findViewById(android.R.id.content), message);
                }
            });
        }
    }

    public void run() {
        registerSingleFile();
    }
}
