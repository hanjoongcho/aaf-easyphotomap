package me.blog.korn123.easyphotomap.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constants.Constant;
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.utils.BitmapUtils;
import me.blog.korn123.easyphotomap.utils.CommonUtils;
import me.blog.korn123.easyphotomap.utils.DialogUtils;

/**
 * Created by CHO HANJOONG on 2016-09-11.
 */
public class BatchPopupActivity extends Activity {

    ProgressBar mProgressBar;
    private boolean mEnableUpdate = true;

    int mTotalPhoto = 0;
    int mSuccessCount = 0;
    int mFailCount = 0;
    int mNoGPSInfoCount = 0;
    int mReduplicationCount = 0;
    private int mProgressStatus = 0;

    TextView mInfoText;
    TextView mInfoText2;
    TextView mInfoText3;
    TextView mInfoText4;
    TextView mInfoText5;
    TextView mInfoText6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_batch_popup);
        ButterKnife.bind(this);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mInfoText = (TextView)findViewById(R.id.infoText);
        mInfoText2 = (TextView)findViewById(R.id.infoText2);
        mInfoText3 = (TextView)findViewById(R.id.infoText3);
        mInfoText4 = (TextView)findViewById(R.id.infoText4);
        mInfoText5 = (TextView)findViewById(R.id.infoText5);
        mInfoText6 = (TextView)findViewById(R.id.infoText6);
        mInfoText.setTypeface(Typeface.DEFAULT);
        mInfoText2.setTypeface(Typeface.DEFAULT);
        mInfoText3.setTypeface(Typeface.DEFAULT);
        mInfoText4.setTypeface(Typeface.DEFAULT);
        mInfoText5.setTypeface(Typeface.DEFAULT);
        mInfoText6.setTypeface(Typeface.DEFAULT);

        ArrayList<String> listImagePath = getIntent().getStringArrayListExtra("listImagePath");
        mTotalPhoto = listImagePath.size();
        Thread thread = new RegisterThread(BatchPopupActivity.this, listImagePath);
        mProgressBar.setMax(mTotalPhoto);
        thread.start();
    }

    @OnClick({R.id.stop, R.id.close})
    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.stop:
                mEnableUpdate = false;
                break;
            case R.id.close:
                finish();
                break;
        }
    }

    Handler progressHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            mInfoText.setText(++mProgressStatus + " / " + mTotalPhoto);
            mInfoText2.setText(getString(R.string.batch_popup_message2) + ": " + mSuccessCount);
            mInfoText3.setText(getString(R.string.batch_popup_message3) + ": " + mReduplicationCount);
            mInfoText4.setText(getString(R.string.batch_popup_message4) + ": " + mNoGPSInfoCount);
            mInfoText5.setText(getString(R.string.batch_popup_message5) + ": " + mFailCount);
            mProgressBar.setProgress(mProgressStatus);
            return true;
        }
    });

    class RegisterThread extends Thread {
        ArrayList<String> listImagePath;
        Context context;

        RegisterThread(Context context, ArrayList<String> listImagePath) {
            this.listImagePath = listImagePath;
            this.context = context;
        }

        public void run() {
            for (String imagePath : listImagePath) {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                if (!mEnableUpdate) return;
                Message message = progressHandler.obtainMessage();
                try {
                    String fileName = FilenameUtils.getName(imagePath) + ".origin";
                    File targetFile = null;
                    if (CommonUtils.loadBooleanPreference(BatchPopupActivity.this, "enable_create_copy")) {
                        targetFile = new File(Constant.WORKING_DIRECTORY + fileName);
                        if (!targetFile.exists()) {
                            FileUtils.copyFile(new File(imagePath), targetFile);
                        }
                    } else {
                        targetFile = new File(imagePath);
                        // remove .origin extension
                        fileName = FilenameUtils.getBaseName(fileName);
                    }

                    Metadata metadata = JpegMetadataReader.readMetadata(targetFile);
                    PhotoMapItem item = new PhotoMapItem();
                    item.imagePath = targetFile.getAbsolutePath();
                    ExifSubIFDDirectory exifSubIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                    Date date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
                    if (date != null) {
                        item.date = CommonUtils.DATE_TIME_PATTERN.format(date);
                    } else {
                        item.date = getString(R.string.file_explorer_message2);
                    }
                    Log.i("elapsed", String.format("meta %d", stopWatch.getTime()));
                    stopWatch.reset();
                    stopWatch.start();
                    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                    if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                        item.longitude = gpsDirectory.getGeoLocation().getLongitude();
                        item.latitude = gpsDirectory.getGeoLocation().getLatitude();
                        List<Address> listAddress = CommonUtils.getFromLocation(BatchPopupActivity.this, item.latitude, item.longitude, 1, 0);
                        if (listAddress.size() > 0) {
                            item.info = CommonUtils.fullAddress(listAddress.get(0));
                        }
                        Log.i("elapsed", String.format("geo coding %d", stopWatch.getTime()));
                        ArrayList<PhotoMapItem> tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath);
                        stopWatch.reset();
                        stopWatch.start();
                        if (tempList.size() > 0) {
                            mReduplicationCount++;
                        } else {
                            PhotoMapDbHelper.insertPhotoMapItem(item);
                            BitmapUtils.createScaledBitmap(targetFile.getAbsolutePath(), Constant.WORKING_DIRECTORY + fileName + ".thumb", 200);
                            mSuccessCount++;
                            Log.i("elapsed", String.format("create bitmap %d", stopWatch.getTime()));
                            stopWatch.stop();
                        }
                    } else {
                        mNoGPSInfoCount++;
                    }

                } catch (Exception e) {
                    mFailCount++;
                }
                progressHandler.sendMessage(message);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DialogUtils.showAlertDialog(BatchPopupActivity.this, getString(R.string.batch_popup_message6));
    }

}
