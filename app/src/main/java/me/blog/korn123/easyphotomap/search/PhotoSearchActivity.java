package me.blog.korn123.easyphotomap.search;

//import android.app.Fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListPopupWindow;
import android.widget.ListView;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.blog.korn123.easyphotomap.activities.MapsActivity;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
public class PhotoSearchActivity extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private ListPopupWindow mList;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_common_search_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.photo_search_message1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView)findViewById(R.id.listView);
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String query) {
                    refreshList(query, 0);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    refreshList(query, 0);
                    searchView.clearFocus();
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
            searchView.setIconified(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // Not implemented here
                return false;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        refreshList(null, 0, 0);
    }

    public void refreshList(String query, int position) {
        refreshList(query, position, 0);
    }

    PhotoEntityAdapter mAdapter;
    public void refreshList(String query, int position, int top) {
        parseMetadata(query);
        mAdapter = new PhotoEntityAdapter(this, this, R.layout.search_photo_search_activity_list_item, entities);
        listView.setAdapter(mAdapter);
        final Context context = this;
        final String fQuery = query;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoEntity imageEntity = (PhotoEntity) parent.getAdapter().getItem(position);
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("info", imageEntity.info);
                intent.putExtra("imagePath", imageEntity.imagePath);
                intent.putExtra("latitude", imageEntity.latitude);
                intent.putExtra("longitude", imageEntity.longitude);
                intent.putExtra("date", imageEntity.date);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            PhotoEntityAdapter fAdpter = mAdapter;
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long l) {
//                AAFLogger.info("PhotoSearchActivity-refreshList INFO: view top is " + view.getTop(), getClass());
                PhotoEntity imageEntity = (PhotoEntity) parent.getAdapter().getItem(position);
                String imagePath = imageEntity.imagePath;
                List<String> listData = CommonUtils.readDataFile(PHOTO_DATA_PATH);
                StringBuilder modifiedData = new StringBuilder();
                for (String data : listData) {
                    if (StringUtils.contains(data, imagePath)) continue;
                    modifiedData.append(data + "\n");
                }
                CommonUtils.writeDataFile(modifiedData.toString(), PHOTO_DATA_PATH);
                refreshList(fQuery, position, view.getTop());
                return true;
            }
        });
//        listView.setSelection(position);
//        listView.setTop(top);
        listView.setSelectionFromTop(position, top);
    }

    final String PHOTO_DATA_PATH = Constant.WORKING_DIRECTORY + "photodata.dat";
    ArrayList<PhotoEntity> entities;
    public void parseMetadata(String query) {
        if (entities == null) {
            entities = new ArrayList<>();
        } else {
            entities.clear();
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
                entity.date = infoArray[4];
//                Log.i("entity.date", entity.date);
//                Date date = CommonUtils.DATE_TIME_PATTERN.parse(entity.date);
//                Log.i("date", date.toString());
//                CommonUtils.DATE_TIME_PATTERN.setTimeZone(TimeZone.getTimeZone("GMT"));
//                Log.i("date GMT", CommonUtils.DATE_TIME_PATTERN.format(date));
//                CommonUtils.DATE_TIME_PATTERN.setTimeZone(TimeZone.getTimeZone("KST"));
//                Log.i("date KST", CommonUtils.DATE_TIME_PATTERN.format(date));
                if (query == null || StringUtils.contains(entity.info, query)) {
                    entities.add(entity);
                }
            }
            Collections.sort(entities);
        } catch (Exception e) {
            AAFLogger.info("PhotoSearchActivity-parseMetadata INFO: " + e.getMessage(), getClass());
        }
    }
}
