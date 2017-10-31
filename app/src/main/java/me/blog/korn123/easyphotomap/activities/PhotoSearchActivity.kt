package me.blog.korn123.easyphotomap.activities

//import android.app.Fragment;

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_photo_search.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.SearchItemAdapter
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
class PhotoSearchActivity : AppCompatActivity() {

    private val mListPhotoMapItem = arrayListOf<PhotoMapItem>()
    private var mSearchView: SearchView? = null
    private var mQueryTextListener: SearchView.OnQueryTextListener? = null
    private var mSearchItemAdapter: SearchItemAdapter? = null
    private var mCurrentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_search)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            setTitle(R.string.photo_search_message1)
            setDisplayHomeAsUpEnabled(true)
        }
        delete.setOnClickListener({ _ ->
            val message = when (mCurrentQuery.isEmpty()) {
                true -> getString(R.string.delete_all_confirm_message)
                false -> getString(R.string.delete_contain_keyword_confirm_message, mCurrentQuery)
            }
            AlertDialog.Builder(this@PhotoSearchActivity).apply {
                setMessage(message)
                setPositiveButton(getString(R.string.confirm), { _, _ ->
                    PhotoMapDbHelper.deletePhotoMapItemBy(mCurrentQuery)
                    refreshList(mCurrentQuery)
                })
            }.show()
        })
        refreshList()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchItem != null) {
            mSearchView = searchItem.actionView as SearchView
        }
        mSearchView?.let { searchView ->
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            mQueryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String): Boolean {
                    mCurrentQuery = query
                    refreshList(query, 0)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    mCurrentQuery = query
                    refreshList(query, 0)
                    searchView.clearFocus()
                    return true
                }
            }
            searchView.setOnQueryTextListener(mQueryTextListener)
            searchView.isIconified = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return false
            android.R.id.home -> finish()
            else -> {
            }
        }
        mSearchView?.setOnQueryTextListener(mQueryTextListener)
        return super.onOptionsItemSelected(item)
    }

    @JvmOverloads
    fun refreshList(query: String? = "", position: Int = 0, top: Int = 0) {
        parseMetadata(query)
        if (mSearchItemAdapter == null) {
            mSearchItemAdapter = SearchItemAdapter(this, this, R.layout.item_search, mListPhotoMapItem)
            listView.adapter = mSearchItemAdapter
            val context = this

            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, clickPosition, _ ->
                val item = parent.adapter.getItem(clickPosition) as PhotoMapItem
                val intent = Intent(context, MapsActivity::class.java).apply {
                    putExtra("info", item.info)
                    putExtra("imagePath", item.imagePath)
                    putExtra("latitude", item.latitude)
                    putExtra("longitude", item.longitude)
                    putExtra("date", item.date)
                }
                startActivity(intent)
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, longClickPosition, _ ->
                val item = parent.adapter.getItem(longClickPosition) as PhotoMapItem
                PhotoMapDbHelper.deletePhotoMapItemBy(item.sequence)
                refreshList(query, longClickPosition, view.top)
                true
            }
        } else {
            mSearchItemAdapter?.notifyDataSetChanged()
        }
        listView.setSelectionFromTop(position, top)
    }

    private fun parseMetadata(query: String?) {
        query?.let {
            mListPhotoMapItem.clear()
            val listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", it)
            mListPhotoMapItem.addAll(listTemp)
        }
    }

}
