package me.blog.korn123.easyphotomap.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.helper.RegistrationThread;
import me.blog.korn123.easyphotomap.models.ThumbnailItem;
import me.blog.korn123.easyphotomap.adapters.ThumbnailItemAdapter;
import me.blog.korn123.easyphotomap.utils.CommonUtils;
import me.blog.korn123.easyphotomap.utils.DialogUtils;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class ThumbnailExplorerActivity extends AppCompatActivity {

    private ThumbnailItemAdapter mThumbnailEntityAdapter;
    private GridView mGridView;
    private boolean mEnableUpdate = false;
    private ProgressDialog mProgressDialog;
    private int mThumbnailTotal = 0;
    private int mCompleted = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_thumbnail_explorer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.thumbnail_explorer_compact_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGridView = (GridView)findViewById(R.id.filelist);
        setAdapter();
        mGridView.setColumnWidth((int) ((CommonUtils.getDefaultDisplay(this).x - CommonUtils.dpToPixel(ThumbnailExplorerActivity.this, 30, 1)) / 3));
        setOnItemClickListener();

        findViewById(R.id.startSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThumbnailCreatorThread thread = new ThumbnailCreatorThread(ThumbnailExplorerActivity.this);
                mEnableUpdate = true;
                thread.start();
            }
        });
        findViewById(R.id.stopSync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableUpdate = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.thumbnail_explorer_menu, menu);
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
                ThumbnailCreatorThread thread = new ThumbnailCreatorThread(this.getApplicationContext());
                thread.start();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAdapter() {
        List<ThumbnailItem> listPhotoEntity = CommonUtils.fetchAllThumbnail(ThumbnailExplorerActivity.this);
        mThumbnailEntityAdapter = new ThumbnailItemAdapter(this, this, R.layout.item_thumbnail, listPhotoEntity);
        mGridView.setAdapter(mThumbnailEntityAdapter);
    }

    public void setOnItemClickListener() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                ThumbnailItem photoEntity = (ThumbnailItem)parent.getAdapter().getItem(position);
                String imagePath = CommonUtils.getOriginImagePath(ThumbnailExplorerActivity.this, photoEntity.getImageId());
                PositiveListener positiveListener = new PositiveListener(ThumbnailExplorerActivity.this, ThumbnailExplorerActivity.this, FilenameUtils.getName(imagePath) + ".origin", imagePath);
                if (imagePath == null) {
                    DialogUtils.INSTANCE.showAlertDialog(ThumbnailExplorerActivity.this, getString(R.string.thumbnail_explorer_message4));
                } else {
                    DialogUtils.INSTANCE.showAlertDialog(ThumbnailExplorerActivity.this, getString(R.string.file_explorer_message7), ThumbnailExplorerActivity.this, imagePath, positiveListener);
                }
            }
        });
    }

    public class PositiveListener {
        Context context;
        Activity activity;
        String fileName;
        String path;

        PositiveListener(Context context, Activity activity, String fileName, String path) {
            this.context = context;
            this.activity = activity;
            this.fileName = fileName;
            this.path = path;
        }

        public void register() {
            if (fileName != null && path != null) {
                mProgressDialog = ProgressDialog.show(ThumbnailExplorerActivity.this, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6));
                Thread registerThread = new RegistrationThread(context, activity, mProgressDialog, fileName, path);
                registerThread.start();
            }
        }
    }

    class ThumbnailCreatorThread extends Thread {

        Context context;

        ThumbnailCreatorThread(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            List<ThumbnailItem> listOriginImage = CommonUtils.fetchAllImages(context);
            List<ThumbnailItem> listThumbnail = CommonUtils.fetchAllThumbnail(context);
            List<String> listImageId = new ArrayList<>();
            for (ThumbnailItem te : listThumbnail) {
                listImageId.add(te.getImageId());
            }
            mCompleted = listThumbnail.size();

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    LinearLayout layout = (LinearLayout)findViewById(R.id.infoView);
                    layout.setVisibility(View.VISIBLE);
                }
            });

            for (ThumbnailItem entity : listOriginImage) {
                if (!mEnableUpdate) break;
                if (listImageId.contains(entity.getImageId())) {
                    continue;
                }
                MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), Long.parseLong(entity.getImageId()), MediaStore.Images.Thumbnails.MINI_KIND, null);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.progressView)).setText("total: " + mThumbnailTotal);
                        ((TextView)findViewById(R.id.progressView2)).setText("mCompleted: " + (++mCompleted));
                    }
                });
            }

            mThumbnailTotal = listOriginImage.size();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.progressView)).setText("total: " + mThumbnailTotal);
                    ((TextView)findViewById(R.id.progressView2)).setText("mCompleted: " + mCompleted);
                    setAdapter();
                    mThumbnailEntityAdapter.notifyDataSetChanged();
                }
            });
        }
    }

}
