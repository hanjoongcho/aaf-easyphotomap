package me.blog.korn123.easyphotomap.thumbnail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.file.FileExplorerActivity;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.AddressSearchActivity;
import me.blog.korn123.easyphotomap.search.PhotoEntity;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class ThumbnailExplorerActivity extends AppCompatActivity {

    private ListView mListView;
    private GridView mGridView;
    private boolean enableUpdate = false;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.thumbnail_thumbnail_explorer_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.thumbnail_explorer_compact_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        mListView = (ListView)findViewById(R.id.filelist);
        mGridView = (GridView)findViewById(R.id.filelist);
        setAdpater();
        mGridView.setColumnWidth((int) ((CommonUtils.getDefaultDisplay(this).x - CommonUtils.dpToPixel(ThumbnailExplorerActivity.this, 30, 1)) / 3));
        setOnItemClickListener();

        findViewById(R.id.startSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThumbnailThread thread = new ThumbnailThread(ThumbnailExplorerActivity.this);
                enableUpdate = true;
                thread.start();
            }
        });
        findViewById(R.id.stopSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableUpdate = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_explorer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.explorer:
                Intent intent = new Intent(ThumbnailExplorerActivity.this, FileExplorerActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
            case R.id.update:
                ThumbnailThread thread = new ThumbnailThread(this.getApplicationContext());
                thread.start();
//                progressDialog = ProgressDialog.show(ThumbnailExplorerActivity.this, getString(R.string.thumbnail_explorer_message1), "");
                break;
//            case R.id.setting:
//                Intent settingIntent = new Intent(this, EPMPreferenceActivity.class);
//                startActivity(settingIntent);
//                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class ThumbnailThread extends Thread {

        Context context;

        ThumbnailThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Message message = null;
            List<ThumbnailEntity> listOriginImage = CommonUtils.fetchAllImages(context);
            List<ThumbnailEntity> listThumbnail = CommonUtils.fetchAllThumbnail(context);
            List<String> listImageId = new ArrayList<>();
            for (ThumbnailEntity te : listThumbnail) {
                listImageId.add(te.imageId);
            }
            int index = listThumbnail.size();

            message = registerHandler.obtainMessage();
            message.obj = "visible";
            registerHandler.sendMessage(message);
            Map<String, String> map = null;
            for (ThumbnailEntity entity : listOriginImage) {
                if (!enableUpdate) break;
//                Log.i("listOriginImage", String.valueOf(listOriginImage.size()));
//                Log.i("listThumbnail", String.valueOf(listThumbnail.size()));
                if (listImageId.contains(entity.imageId)) {
                    continue;
                }
                MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), Long.parseLong(entity.imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
                message = registerHandler.obtainMessage();
                map = new HashMap<>();
                map.put("total", String.valueOf(listOriginImage.size()));
                map.put("complete", String.valueOf(++index));
                message.obj = map;
                registerHandler.sendMessage(message);
//                progresView.setText(++index + "/" + listOriginImage.size());
            }

            message = registerHandler.obtainMessage();
            map = new HashMap<>();
            map.put("total", String.valueOf(listOriginImage.size()));
            map.put("complete", String.valueOf(index));
            message.obj = map;
            registerHandler.sendMessage(message);

            message = registerHandler.obtainMessage();
            message.obj = "update completed";
            registerHandler.sendMessage(message);
        }
    }

    ThumbnailEntityAdapter mThumbnailEntityAdapter;
    public void setAdpater() {
        List<ThumbnailEntity> listPhotoEntity = CommonUtils.fetchAllThumbnail(ThumbnailExplorerActivity.this);
        mThumbnailEntityAdapter = new ThumbnailEntityAdapter(this, this, R.layout.thumbnail_thumbnail_explorer_activity_list_item, listPhotoEntity);
        mGridView.setAdapter(mThumbnailEntityAdapter);
    }

    public void setOnItemClickListener() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                ThumbnailEntity photoEntity = (ThumbnailEntity)parent.getAdapter().getItem(position);
                String imagePath = CommonUtils.getOrignImagepath(ThumbnailExplorerActivity.this, photoEntity.imageId);
                PositiveListener positiveListener = new PositiveListener(ThumbnailExplorerActivity.this, FilenameUtils.getName(imagePath) + ".origin", imagePath);
                if (imagePath == null) {
                    CommonUtils.showAlertDialog(ThumbnailExplorerActivity.this, getString(R.string.thumbnail_explorer_message4));
                } else {
                    CommonUtils.showAlertDialog(ThumbnailExplorerActivity.this, getString(R.string.file_explorer_message7), ThumbnailExplorerActivity.this, imagePath, positiveListener);
                }
            }
        });
    }

    ProgressDialog progressDialog;
    public class PositiveListener {
        String fileName;
        String path;
        Context context;

        PositiveListener(Context context, String fileName, String path) {
            this.fileName = fileName;
            this.path = path;
            this.context = context;
        }

        public void register() {
            Thread registerThread = new RegisterThread(context, fileName, path);
            registerThread.start();
            progressDialog = ProgressDialog.show(ThumbnailExplorerActivity.this, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6));
        }
    }

    class RegisterThread extends Thread {
        String fileName;
        String path;
        Context context;

        RegisterThread(Context context, String fileName, String path) {
            this.fileName = fileName;
            this.path = path;
            this.context = context;
        }

        public void run() {
            Message message = registerHandler.obtainMessage();
            try {

                File targetFile = null;
                if (CommonUtils.loadBooleanPreference(ThumbnailExplorerActivity.this, "enable_create_copy")) {
                    targetFile = new File(Constant.WORKING_DIRECTORY + fileName);
                    if (!targetFile.exists()) {
                        FileUtils.copyFile(new File(path), targetFile);
                    }
                } else {
                    targetFile = new File(path);
                    // remove .origin extension
                    fileName = FilenameUtils.getBaseName(fileName);
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
                    List<Address> listAddress = CommonUtils.getFromLocation(ThumbnailExplorerActivity.this, entity.latitude, entity.longitude, 1, 0);
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
                        message.obj = getString(R.string.file_explorer_message3);
                        registerHandler.sendMessage(message);
                    } else {
                        CommonUtils.writeDataFile(sb.toString(), Constant.PHOTO_DATA_PATH, true);
//                        if (!new File(Constant.WORKING_DIRECTORY + fileName + ".thumb").exists()) {
                            CommonUtils.createScaledBitmap(targetFile.getAbsolutePath(), Constant.WORKING_DIRECTORY + fileName + ".thumb", 200);
//                        }
                        message.obj = getString(R.string.file_explorer_message4);
                        registerHandler.sendMessage(message);
                    }
                } else {
                    message.obj = entity;
                    registerHandler.sendMessage(message);
                }
            } catch (Exception e) {
                AAFLogger.info("RegisterThread-run INFO: exception is " + e, getClass());
                String msg = null;
                if (e.getMessage() != null) {
                    msg = e.getMessage();
                } else {
                    msg = "unknown error";
                }
                message.obj = msg;
                        registerHandler.sendMessage(message);
            }
        }
    }

    private Context explorerContext = this;
    Handler registerHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof String) {
                if (StringUtils.equals((String) msg.obj, "update completed")) {
//                    progressDialog.dismiss();
                    setAdpater();
                    mThumbnailEntityAdapter.notifyDataSetChanged();
                } else if (StringUtils.equals((String) msg.obj, "visible")) {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.infoView);
                    layout.setVisibility(View.VISIBLE);
                } else {
                    progressDialog.dismiss();
                    CommonUtils.makeToast(ThumbnailExplorerActivity.this, (String) msg.obj);
                }
            } else if (msg.obj instanceof HashMap) {
                Map<String, String> infoMap = (Map)msg.obj;
                ((TextView)findViewById(R.id.progressView)).setText("total: " + infoMap.get("total"));
                ((TextView)findViewById(R.id.progressView2)).setText("completed: " + infoMap.get("complete"));
            } else if (msg.obj instanceof PhotoEntity) {
                progressDialog.dismiss();
                final Intent addressIntent = new Intent(ThumbnailExplorerActivity.this, AddressSearchActivity.class);
                final PhotoEntity lEntity = (PhotoEntity)msg.obj;
                AlertDialog.Builder builder = new AlertDialog.Builder(ThumbnailExplorerActivity.this);
                builder.setMessage(getString(R.string.file_explorer_message1)).setCancelable(false).setPositiveButton(getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addressIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                addressIntent.putExtra("imagePath", lEntity.imagePath);
                                addressIntent.putExtra("date", lEntity.date);
                                explorerContext.startActivity(addressIntent);
                                return;
                            }
                        }).setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            return true;
        }
    });

}
