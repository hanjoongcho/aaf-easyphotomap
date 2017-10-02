package me.blog.korn123.easyphotomap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.adapters.TimelineItemAdapter;
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;

/**
 * Created by hanjoong on 2017-02-02.
 */

public class TimelineActivity extends AppCompatActivity {

    private ArrayAdapter<PhotoMapItem> mArrayAdapter;
    private ArrayList<PhotoMapItem> mListPhotoMapItem;

    @BindView(R.id.listTimeline) ListView mListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.timeline_compat_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        parseMetadata();
        mArrayAdapter = new TimelineItemAdapter(this, this, R.layout.item_timeline, mListPhotoMapItem);
        mListView.setAdapter(mArrayAdapter);
        mListView.setSelection(mArrayAdapter.getCount() - 1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoMapItem imageEntity = (PhotoMapItem) parent.getAdapter().getItem(position);
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

    public void parseMetadata() {
        mListPhotoMapItem = PhotoMapDbHelper.selectTimeLineItemAll(getString(R.string.file_explorer_message2));
    }

}
