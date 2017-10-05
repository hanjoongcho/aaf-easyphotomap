package me.blog.korn123.easyphotomap.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_timeline.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.TimelineItemAdapter
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import java.util.*

/**
 * Created by hanjoong on 2017-02-02.
 */

class TimelineActivity : AppCompatActivity() {

    private var mArrayAdapter: ArrayAdapter<PhotoMapItem>? = null
    private var mListPhotoMapItem: ArrayList<PhotoMapItem>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        ButterKnife.bind(this)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.timeline_compat_activity_title)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        parseMetadata()
        mArrayAdapter = TimelineItemAdapter(this, this, R.layout.item_timeline, mListPhotoMapItem!!)
        listTimeline.adapter = mArrayAdapter
        listTimeline.setSelection(mArrayAdapter!!.count - 1)
        listTimeline.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val imageEntity = parent.adapter.getItem(position) as PhotoMapItem
            val intent = Intent(this@TimelineActivity, MapsActivity::class.java)
            intent.putExtra("info", imageEntity.info)
            intent.putExtra("imagePath", imageEntity.imagePath)
            intent.putExtra("latitude", imageEntity.latitude)
            intent.putExtra("longitude", imageEntity.longitude)
            intent.putExtra("date", imageEntity.date)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun parseMetadata() {
        mListPhotoMapItem = PhotoMapDbHelper.selectTimeLineItemAll(getString(R.string.file_explorer_message2))
    }

}
