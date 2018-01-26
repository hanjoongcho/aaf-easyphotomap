package me.blog.korn123.easyphotomap.activities

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import com.simplemobiletools.commons.extensions.onGlobalLayout
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
    private var mCurrentQuery: String = ""
    private val mSearchItemAdapter: SearchItemAdapter? by lazy {
        SearchItemAdapter(
                this,
                mListPhotoMapItem,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    mSearchItemAdapter?.getItem(position)?.let { item ->
                        val intent = Intent(this@PhotoSearchActivity, MapsActivity::class.java).apply {
                            putExtra("info", item.info)
                            putExtra("imagePath", item.imagePath)
                            putExtra("latitude", item.latitude)
                            putExtra("longitude", item.longitude)
                            putExtra("date", item.date)
                        }
                        startActivity(intent)
                    }
                },
                AdapterView.OnItemLongClickListener { _, view, longClickPosition, _ ->
                    mSearchItemAdapter?.getItem(longClickPosition)?.let { item ->
                        PhotoMapDbHelper.deletePhotoMapItemBy(item.sequence)
                        refreshList(mCurrentQuery, longClickPosition, view.top)
                    }
                    true
                }
        )
    }

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

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(this, R.drawable.divider_simple)?.let {
            dividerItemDecoration.setDrawable(it)
            search_items.adapter = mSearchItemAdapter
            search_items.addItemDecoration(dividerItemDecoration)
        }
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

    override fun onResume() {
        super.onResume()
        refreshList()
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

    fun refreshList(query: String? = "", position: Int = 0, top: Int = 0) {
        parseMetadata(query)
        mSearchItemAdapter?.notifyDataSetChanged()
//        listView.setSelectionFromTop(position, top)
        items_fastscroller.setViews(search_items, null) {
            val item = mListPhotoMapItem.getOrNull(it)
            items_fastscroller.updateBubbleText(item?.getBubbleText() ?: "")
        }
        search_items.onGlobalLayout {
            items_fastscroller.setScrollTo(search_items.computeVerticalScrollOffset())
        }
    }

    private fun parseMetadata(query: String?) {
        query?.let {
            mListPhotoMapItem.clear()
            val listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", it)
            mListPhotoMapItem.addAll(listTemp)
        }
    }
}
