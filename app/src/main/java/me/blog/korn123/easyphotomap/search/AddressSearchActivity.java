package me.blog.korn123.easyphotomap.search;

//import android.app.Fragment;

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
import android.widget.ListPopupWindow;
import android.widget.ListView;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
public class AddressSearchActivity extends AppCompatActivity {

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
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView)findViewById(R.id.listView);
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
//                    refreshList(query, 50);
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
        refreshList(null, 50);
    }

    private AddressEntityAdapter addressAdapter;
    private List<Address> mListAddress = new ArrayList<>();
    public void refreshList(String query, int maxResults) {
        if (StringUtils.length(query) < 1) return;
        mListAddress.clear();
        try {
            List<Address> listAddress = CommonUtils.getFromLocationName(AddressSearchActivity.this, query, maxResults, 0);
            mListAddress.addAll(listAddress);
            if (addressAdapter == null) {
                addressAdapter = new AddressEntityAdapter(this, android.R.layout.simple_list_item_2, mListAddress);
                listView.setAdapter(addressAdapter);
                final Context context = this;
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Address address = (Address) parent.getAdapter().getItem(position);
                        Intent intent = getIntent();
                        if (intent.hasExtra("imagePath")) {
                            String fileName = FilenameUtils.getName(intent.getStringExtra("imagePath"));
                            StringBuilder sb = new StringBuilder();
                            sb.append(intent.getStringExtra("imagePath") + "|");
                            sb.append(CommonUtils.fullAddress(address) + "|");
                            sb.append(address.getLatitude() + "|");
                            sb.append(address.getLongitude() + "|");
                            sb.append(intent.getStringExtra("date") + "\n");
                            if (CommonUtils.isMatchLine(Constant.PHOTO_DATA_PATH, sb.toString())) {
                                CommonUtils.makeToast(getApplicationContext(), getString(R.string.address_search_message1));
                            } else {
                                CommonUtils.writeDataFile(sb.toString(), Constant.PHOTO_DATA_PATH, true);
                                if (!new File(Constant.WORKING_DIRECTORY + fileName + ".thumb").exists()) {
                                    CommonUtils.createScaledBitmap(intent.getStringExtra("imagePath"), Constant.WORKING_DIRECTORY + fileName + ".thumb", 200);
                                }
                                CommonUtils.makeToast(getApplicationContext(), getString(R.string.address_search_message2));
                            }
                        }
                        finish();
                    }
                });
            } else {
                addressAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            AAFLogger.info("AddressSearchActivity-refreshList INFO: exception is " + e, getClass());
        }
    }

}
