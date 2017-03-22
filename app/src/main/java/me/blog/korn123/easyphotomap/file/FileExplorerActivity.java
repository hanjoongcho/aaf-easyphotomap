package me.blog.korn123.easyphotomap.file;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.AddressSearchActivity;
import me.blog.korn123.easyphotomap.search.PhotoEntity;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-16.
 */
public class FileExplorerActivity extends AppCompatActivity {

    private Context fileExplorerContext = this;
    private String mCurrent;
    private String mRoot;
    private TextView mCurrentTxt;
    private ListView mFileList;
    private ArrayAdapter<FileEntity> mAdapter;
    private ArrayList<FileEntity> listFileEntity;
    private ArrayList<FileEntity> listDirectroryEntity;
    private ViewGroup viewGroup;
    private HorizontalScrollView mScrollView;

    Handler registerHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof String) {
                progressDialog.dismiss();
                if (StringUtils.equals((String) msg.obj, "notifyDataSetChanged")) {
                    mAdapter.notifyDataSetChanged();
                    mFileList.setSelection(0);
                } else {
                    // 탐색기 스크롤 후 사진 등록 시 기존 스크롤 위치 변경되지 않게 주석처리 2016.11.07 Hanjoong Cho
//                    mAdapter.notifyDataSetChanged();
//                    mFileList.setSelection(0);
                    CommonUtils.makeToast(FileExplorerActivity.this, (String) msg.obj);
                }
            } else if (msg.obj instanceof Map) {
                Map<String, String> infoMap = (Map)msg.obj;
                progressDialog.setMessage(infoMap.get("progressInfo"));
            } else if (msg.obj instanceof PhotoEntity) {
                progressDialog.dismiss();
                final Intent addressIntent = new Intent(FileExplorerActivity.this, AddressSearchActivity.class);
                final PhotoEntity lEntity = (PhotoEntity)msg.obj;
                AlertDialog.Builder builder = new AlertDialog.Builder(FileExplorerActivity.this);
                builder.setMessage(getString(R.string.file_explorer_message1)).setCancelable(false).setPositiveButton(getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addressIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                addressIntent.putExtra("imagePath", lEntity.imagePath);
                                addressIntent.putExtra("date", lEntity.date);
                                fileExplorerContext.startActivity(addressIntent);
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

    class RegisterThread extends Thread {
        String fileName;
        String path;
        Context context;

        RegisterThread(Context context, String fileName, String path) {
            this.fileName = fileName;
            this.path = path;
            this.context = context;
        }

        public void registerSingleFile() {
            Message message = registerHandler.obtainMessage();
            try {

                File targetFile = null;
                if (CommonUtils.loadBooleanPreference(FileExplorerActivity.this, "enable_create_copy")) {
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
                Date date = exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
                if (date != null) {
                    entity.date = CommonUtils.DATE_TIME_PATTERN.format(date);
                } else {
                    entity.date = getString(R.string.file_explorer_message2);
                }

                GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                    entity.longitude = gpsDirectory.getGeoLocation().getLongitude();
                    entity.latitude = gpsDirectory.getGeoLocation().getLatitude();
                    List<Address> listAddress = CommonUtils.getFromLocation(FileExplorerActivity.this, entity.latitude, entity.longitude, 1, 0);
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
                AAFLogger.info("FileExplorerActivity-run INFO: " + e.getMessage(), getClass());
                AAFLogger.info("RegisterThread-run INFO: exception is " + e, getClass());
                message.obj = e.getMessage();
                registerHandler.sendMessage(message);
            }
        }

        public void run() {
            registerSingleFile();
        }
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
            if (fileName != null && path != null) {
                Thread registerThread = new RegisterThread(context, fileName, path);
                registerThread.start();
                progressDialog = ProgressDialog.show(FileExplorerActivity.this, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6));
            } else {
                Intent batchIntent = new Intent(FileExplorerActivity.this, BatchPopupActivity.class);
                ArrayList<String> listImagePath = new ArrayList<>();
                for (int i = listDirectroryEntity.size(); i < listFileEntity.size(); i++) {
                    listImagePath.add(listFileEntity.get(i).getImagePath());
                }
                batchIntent.putStringArrayListExtra("listImagePath", listImagePath);
                startActivity(batchIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.file_file_explorer_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.file_explorer_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFileList = (ListView)findViewById(R.id.filelist);
        listFileEntity = new ArrayList<FileEntity>();
        listDirectroryEntity = new ArrayList<>();
        mRoot = Constant.CAMERA_DIRECTORY_INTERNAL;
        mCurrent = Constant.CAMERA_DIRECTORY;

        mAdapter = new FileEntityAdapter(this, this, R.layout.file_file_explorer_activity_list_item, this.listFileEntity);
        mFileList.setAdapter(mAdapter);
        viewGroup = (ViewGroup)findViewById(R.id.pathView);
        mScrollView  = (HorizontalScrollView)findViewById(R.id.scrollView);

        AdapterView.OnItemClickListener mItemClickListener =
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FileEntity thumbnailEntity = (FileEntity)parent.getAdapter().getItem(position);
                        String fileName = thumbnailEntity.getFileName();

                        if (fileName.startsWith("[") && fileName.endsWith("]")) {
                            fileName = fileName.substring(1, fileName.length()-1);
                        }

                        String path = mCurrent + "/" + fileName;
                        File f = new File(path);

                        if (f.isDirectory()) {
                            mCurrent = path;
                            refreshFiles();
                        } else {
                            if (!new File(Constant.WORKING_DIRECTORY).exists()) {
                                new File(Constant.WORKING_DIRECTORY).mkdirs();
                            }
                            PositiveListener positiveListener = new PositiveListener(FileExplorerActivity.this, FilenameUtils.getName(path) + ".origin", path);
                            CommonUtils.showAlertDialog(FileExplorerActivity.this, getString(R.string.file_explorer_message7), FileExplorerActivity.this, path, positiveListener);
                        }
                    }
                };
        mFileList.setOnItemClickListener(mItemClickListener);
        refreshFiles();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void downscaleBitmapUsingDensities(int sampleSize, InputStream is, OutputStream os) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inDensity=sampleSize;
        bitmapOptions.inTargetDensity=1;
        Bitmap scaledBitmap = BitmapFactory.decodeResourceStream(getResources(), null, is, null, bitmapOptions);
        scaledBitmap.setDensity(Bitmap.DENSITY_NONE);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
    }

    @OnClick({R.id.btnup})
    public void buttonClick(View view) {
        switch (view.getId()) {
//            case R.id.btnroot:
//                if (mCurrent.compareTo(mRoot) != 0) {
//                    mCurrent = mRoot;
//                    refreshFiles();
//                }
//                break;
            case R.id.btnup:
//                if (mCurrent.endsWith("/")) {
//                mCurrent = mCurrent.substring(0, mCurrent.lastIndexOf("/"));
//            }
//            String uppath = FilenameUtils.getFullPath(mCurrent);
//            if (StringUtils.equals(uppath, "/")) return;
//            mCurrent = uppath;
//            refreshFiles();

                if (listFileEntity.size() - listDirectroryEntity.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.file_explorer_message9));
                } else {
                    PositiveListener positiveListener = new PositiveListener(FileExplorerActivity.this, null, null);
                    CommonUtils.showAlertDialog(FileExplorerActivity.this, getString(R.string.file_explorer_message11) , FileExplorerActivity.this, positiveListener);
                }
                break;
//            case R.id.photoHomeExternal:
//                if (mCurrent.compareTo(Constant.CAMERA_DIRECTORY_INTERNAL) != 0) {
//                    mCurrent = mRoot;
//                    refreshFiles();
//                }
//                break;
//            case R.id.close:
//                finish();
//                break;
        }
    }

    class RefreshThread extends Thread {

        @Override
        public void run() {
            listFileEntity.clear();
            listDirectroryEntity.clear();
            File current = new File(mCurrent);
            String[] files = current.list();
            if (files != null) {
                for (int i = 0; i < files.length;i++) {
                    FileEntity thumbnailEntity = new FileEntity();
                    String path = mCurrent + "/" + files[i];
                    String name = "";
                    File f = new File(path);
                    if (f.isDirectory()) {
                        name = "[" + files[i] + "]";
                        thumbnailEntity.setImagePath(name);
                        thumbnailEntity.isDirectory = true;
                        listDirectroryEntity.add(thumbnailEntity);
                    } else {
                        name = files[i];
                        String extension = FilenameUtils.getExtension(name).toLowerCase();
                        if (!extension.matches("jpg|jpeg")) continue;
//                        if (f.length() < 1) f.delete();
//                        if (!new File(Constant.WORKING_DIRECTORY + files[i] + ".thumb").exists()) {
//                            CommonUtils.createScaledBitmap(path, Constant.WORKING_DIRECTORY + files[i] + ".thumb", 200);
//                            Message message = registerHandler.obtainMessage();
//                            Map<String, String> infoMap = new HashMap<>();
//                            infoMap.put("progressInfo" , i + "/" + files.length);
//                            message.obj = infoMap;
//                            registerHandler.sendMessage(message);
//                        }
                        thumbnailEntity.setImagePath(path);
                        listFileEntity.add(thumbnailEntity);
                    }
                }
            }

            if (CommonUtils.loadBooleanPreference(FileExplorerActivity.this, "enable_reverse_order")) {
                Collections.sort(listDirectroryEntity, Collections.reverseOrder());
                Collections.sort(listFileEntity, Collections.reverseOrder());
            } else {
                Collections.sort(listDirectroryEntity);
                Collections.sort(listFileEntity);
            }
            listFileEntity.addAll(0, listDirectroryEntity);
            Message message = registerHandler.obtainMessage();
            message.obj = "notifyDataSetChanged";
            registerHandler.sendMessage(message);
        }
    }

    void refreshFiles() {
        String[] arrayPath = StringUtils.split(mCurrent, "/");
        viewGroup.removeViews(0, viewGroup.getChildCount());
        String currentPath = "";
        for (String path : arrayPath) {
            currentPath += ("/" + path);
            final String targetPath = currentPath;
            TextView textView = new TextView(this);
            if(viewGroup.getChildCount() < 1) {
                textView.setText(path);
            } else {
                textView.setText("  >  " + path);
            }
//            textView.setTextColor(Color.parseColor("#ff000000"));
            textView.setTextColor(getResources().getColor(R.color.black1));
            if (StringUtils.equals(arrayPath[arrayPath.length - 1], path)) {
                textView.setTypeface(null, Typeface.BOLD);
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            textView.setGravity(Gravity.CENTER_VERTICAL);
//            textView.setBackgroundResource(R.color.color2);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurrent = targetPath;
                    refreshFiles();
                }
            });
            viewGroup.addView(textView);
        }
        mScrollView.postDelayed(new Runnable() {
            public void run() {
                mScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
        Thread refreshThread = new RefreshThread();
        refreshThread.start();
        progressDialog = ProgressDialog.show(FileExplorerActivity.this, "", getString(R.string.file_explorer_message10));
    }
}
