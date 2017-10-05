package me.blog.korn123.easyphotomap.activities

import android.app.SearchManager
import android.content.Context
import android.location.Address
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_address_search.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.AddressItemAdapter
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.BitmapUtils
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-22.
 */
class AddressSearchActivity : AppCompatActivity() {

    private val mListAddress = ArrayList<Address>()
    private var mAddressAdapter: AddressItemAdapter? = null
    private var mSearchView: SearchView? = null
    private var mQueryTextListener: SearchView.OnQueryTextListener? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_search)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchItem != null) {
            mSearchView = searchItem!!.actionView as SearchView
        }
        if (mSearchView != null) {
            mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            mQueryTextListener = object : SearchView.OnQueryTextListener {
                public override fun onQueryTextChange(query: String): Boolean {
                    return true
                }

                public override fun onQueryTextSubmit(query: String): Boolean {
                    refreshList(query, 50)
                    mSearchView!!.clearFocus()
                    return true
                }
            }
            mSearchView!!.setOnQueryTextListener(mQueryTextListener)
            mSearchView!!.isIconified = false
        }
        return true
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> return false
            android.R.id.home -> finish()
            else -> {
            }
        }
        mSearchView!!.setOnQueryTextListener(mQueryTextListener)
        return super.onOptionsItemSelected(item)
    }

    @JvmOverloads
    fun refreshList(query: String? = null, maxResults: Int = 50) {
        if (StringUtils.length(query) < 1) return
        mListAddress.clear()
        try {
            val listAddress = CommonUtils.getFromLocationName(this@AddressSearchActivity, query!!, maxResults, 0)
            this.mListAddress.addAll(listAddress!!)
            if (mAddressAdapter == null) {
                mAddressAdapter = AddressItemAdapter(this, android.R.layout.simple_list_item_2, listAddress!!)
                listView.adapter = mAddressAdapter
                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, _ ->
                    val address = parent.adapter.getItem(position) as Address
                    val intent = intent
                    var resultMessage: String? = null
                    if (intent.hasExtra("imagePath")) {
                        val fileName = FilenameUtils.getName(intent.getStringExtra("imagePath"))
                        val item = PhotoMapItem()
                        item.imagePath = intent.getStringExtra("imagePath")
                        item.info = CommonUtils.fullAddress(address)
                        item.latitude = address.getLatitude()
                        item.longitude = address.getLongitude()
                        item.date = intent.getStringExtra("date")

                        val tempList = PhotoMapDbHelper.selectPhotoMapItemBy("imagePath", item.imagePath!!)
                        resultMessage = when(tempList.size > 0) {
                            true -> getString(R.string.file_explorer_message3)
                            false -> {
                                PhotoMapDbHelper.insertPhotoMapItem(item)
                                BitmapUtils.createScaledBitmap(item.imagePath!!, Constant.WORKING_DIRECTORY + fileName + ".thumb", 200)
                                getString(R.string.file_explorer_message4)
                            }
                        }
                    }
                    DialogUtils.makeSnackBar(view, resultMessage!!)
                    Thread(Runnable {
                        try {
                            Thread.sleep(1000)
                            finish()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }).start()
                }
            } else {
                mAddressAdapter!!.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
