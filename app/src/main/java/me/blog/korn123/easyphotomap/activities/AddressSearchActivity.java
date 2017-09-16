package me.blog.korn123.easyphotomap.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.adapters.AddressItemAdapter;
import me.blog.korn123.easyphotomap.constants.Constant;
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.utils.CommonUtils;
import me.blog.korn123.easyphotomap.utils.DialogUtils;

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
public class AddressSearchActivity extends AppCompatActivity {

    private List<Address> listAddress = new ArrayList<>();
    private AddressItemAdapter addressAdapter;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    @BindView(R.id.listView)
    public ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query) {
                    refreshList(query, 50);
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
        refreshList(null, 50);
    }

    public void refreshList(String query, int maxResults) {
        if (StringUtils.length(query) < 1) return;
        listAddress.clear();
        try {
            List<Address> listAddress = CommonUtils.getFromLocationName(AddressSearchActivity.this, query, maxResults, 0);
            this.listAddress.addAll(listAddress);
            if (addressAdapter == null) {
                addressAdapter = new AddressItemAdapter(this, android.R.layout.simple_list_item_2, listAddress);
                listView.setAdapter(addressAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Address address = (Address) parent.getAdapter().getItem(position);
                        Intent intent = getIntent();
                        String resultMessage = null;
                        if (intent.hasExtra("imagePath")) {
                            String fileName = FilenameUtils.getName(intent.getStringExtra("imagePath"));
                            PhotoMapItem item = new PhotoMapItem();
                            item.imagePath = intent.getStringExtra("imagePath");
                            item.info = CommonUtils.fullAddress(address);
                            item.latitude = address.getLatitude();
                            item.longitude = address.getLongitude();
                            item.date = intent.getStringExtra("date");

                            ArrayList<PhotoMapItem> tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath);
                            if (tempList.size() > 0) {
                                resultMessage = getString(R.string.file_explorer_message3);
                            } else {
                                PhotoMapDbHelper.insertPhotoMapItem(item);
                                CommonUtils.createScaledBitmap(item.imagePath, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200);
                                resultMessage = getString(R.string.file_explorer_message4);
                            }
                        }
                        DialogUtils.makeSnackBar(view, resultMessage);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    finish();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
            } else {
                addressAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
