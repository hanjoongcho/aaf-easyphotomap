package me.blog.korn123.easyphotomap.activities

//import android.app.Fragment;

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_photo_search.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.SearchItemAdapter
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
class PhotoSearchActivity : AppCompatActivity() {

    private var mSearchView: SearchView? = null
    private var mQueryTextListener: SearchView.OnQueryTextListener? = null
    private var mSearchItemAdapter: SearchItemAdapter? = null
    private val mListPhotoMapItem = ArrayList<PhotoMapItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_search)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.photo_search_message1)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
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
        if (mSearchView != null) {
            mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            mQueryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String): Boolean {
                    refreshList(query, 0)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    refreshList(query, 0)
                    mSearchView!!.clearFocus()
                    return true
                }
            }
            mSearchView!!.setOnQueryTextListener(mQueryTextListener)
            mSearchView!!.isIconified = false
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
        mSearchView!!.setOnQueryTextListener(mQueryTextListener)
        return super.onOptionsItemSelected(item)
    }

    @JvmOverloads
    fun refreshList(query: String? = "", position: Int = 0, top: Int = 0) {
        parseMetadata(query)
        mSearchItemAdapter = SearchItemAdapter(this, this, R.layout.item_search, mListPhotoMapItem)
        listView.adapter = mSearchItemAdapter
        val context = this

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent.adapter.getItem(position) as PhotoMapItem
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra("info", item.info)
            intent.putExtra("imagePath", item.imagePath)
            intent.putExtra("latitude", item.latitude)
            intent.putExtra("longitude", item.longitude)
            intent.putExtra("date", item.date)
            startActivity(intent)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, l ->
            val item = parent.adapter.getItem(position) as PhotoMapItem
            PhotoMapDbHelper.deletePhotoMapItemBy(item.sequence)
            refreshList(query, position, view.top)
            true
        }

        listView.setSelectionFromTop(position, top)
    }

    private fun parseMetadata(query: String?) {
        mListPhotoMapItem.clear()
        val listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", query!!)
        mListPhotoMapItem.addAll(listTemp)
    }

}
