package me.blog.korn123.easyphotomap.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.blog.korn123.easyphotomap.activities.MapsActivity;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.PhotoEntity;

/**
 * Created by hanjoong on 2017-02-02.
 */

public class TimelineActivity extends AppCompatActivity {

    @Bind(R.id.listTimeline) ListView mListView;
    ArrayAdapter<PhotoEntity> mArrayAdapter;
    ArrayList<PhotoEntity> mListPhotoEntity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_timeline_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.timeline_compat_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parseMetadata();
        mArrayAdapter = new TimelineEntityAdapter(this, this, R.layout.timeline_timeline_activity_list_item, mListPhotoEntity);
        mListView.setAdapter(mArrayAdapter);
        mListView.setSelection(mArrayAdapter.getCount() - 1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoEntity imageEntity = (PhotoEntity) parent.getAdapter().getItem(position);
                Intent intent = new Intent(TimelineActivity.this, MapsActivity.class);
                intent.putExtra("info", imageEntity.info);
                intent.putExtra("imagePath", imageEntity.imagePath);
                intent.putExtra("latitude", imageEntity.latitude);
                intent.putExtra("longitude", imageEntity.longitude);
                intent.putExtra("date", imageEntity.date);
                startActivity(intent);
            }
        });
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

    final String PHOTO_DATA_PATH = Constant.WORKING_DIRECTORY + "photodata.dat";
    public void parseMetadata() {
        if (mListPhotoEntity == null) {
            mListPhotoEntity = new ArrayList<>();
        } else {
            mListPhotoEntity.clear();
        }

        List<String> infoLines = new ArrayList();
        try {
            InputStream is = new FileInputStream(new File(PHOTO_DATA_PATH));
            infoLines = IOUtils.readLines(is, "UTF-8");
            for (String infoLine : infoLines) {
                String[] infoArray = StringUtils.split(infoLine, "|");
                PhotoEntity entity = new PhotoEntity();
                entity.longitude = Double.parseDouble(infoArray[3]);
                entity.latitude = Double.parseDouble(infoArray[2]);
                entity.imagePath = infoArray[0];
                entity.info = infoArray[1];
                entity.originDate = infoArray[4];
                entity.date = getSimpleDate(infoArray[4]);
                entity.sortFlag = 1;
                if (StringUtils.equals(entity.date , getString(R.string.file_explorer_message2))) continue;
                mListPhotoEntity.add(entity);
            }
            Collections.sort(mListPhotoEntity);
        } catch (Exception e) {
            AAFLogger.info("TimelineActivity-parseMetadata INFO: " + e.getMessage(), getClass());
        }
    }

    public String getSimpleDate(String date) {
        String simpleDate = null;
        if (date.contains("(")) {
            simpleDate = date.substring(0, date.lastIndexOf("("));
        } else {
            simpleDate = date;
        }
        return simpleDate;
    }
}
