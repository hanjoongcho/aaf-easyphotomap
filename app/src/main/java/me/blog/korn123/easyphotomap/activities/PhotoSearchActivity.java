package me.blog.korn123.easyphotomap.activities;

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
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.adapters.SearchItemAdapter;

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
public class PhotoSearchActivity extends AppCompatActivity {

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private SearchItemAdapter searchItemAdapter;
    private ArrayList<PhotoMapItem> listPhotoMapItem = new ArrayList<>();

    @BindView(R.id.listView)
    public ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_search);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.photo_search_message1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void refreshList(String query, int position, int top) {
        parseMetadata(query);
        searchItemAdapter = new SearchItemAdapter(this, this, R.layout.item_search, listPhotoMapItem);
        mListView.setAdapter(searchItemAdapter);
        final Context context = this;
        final String fQuery = query;

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoMapItem item = (PhotoMapItem) parent.getAdapter().getItem(position);
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("info", item.info);
                intent.putExtra("imagePath", item.imagePath);
                intent.putExtra("latitude", item.latitude);
                intent.putExtra("longitude", item.longitude);
                intent.putExtra("date", item.date);
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long l) {
                PhotoMapItem item = (PhotoMapItem) parent.getAdapter().getItem(position);
                PhotoMapDbHelper.deletePhotoMapItemBy(item.sequence);
                refreshList(fQuery, position, view.getTop());
                return true;
            }
        });

        mListView.setSelectionFromTop(position, top);
    }

    public void parseMetadata(String query) {
        listPhotoMapItem.clear();
        ArrayList<PhotoMapItem> listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", query);
        listPhotoMapItem.addAll(listTemp);
    }

}
