package me.blog.korn123.easyphotomap.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_timeline.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.TimelineItemAdapter
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import java.util.*
import android.support.v7.widget.LinearLayoutManager
import com.simplemobiletools.commons.extensions.onGlobalLayout


/**
 * Created by hanjoong on 2017-02-02.
 */

class TimelineActivity : AppCompatActivity() {
    private var mListPhotoMapItem: ArrayList<PhotoMapItem> = arrayListOf<PhotoMapItem>()
    private val mTimeLineItemAdapter: TimelineItemAdapter? by lazy {
        TimelineItemAdapter(
                this,
                mListPhotoMapItem,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    mTimeLineItemAdapter?.getItem(position)?.let { item ->
                        val intent = Intent(this@TimelineActivity, MapsActivity::class.java).apply {
                            putExtra("info", item.info)
                            putExtra("imagePath", item.imagePath)
                            putExtra("latitude", item.latitude)
                            putExtra("longitude", item.longitude)
                            putExtra("date", item.date)
                        }
                        startActivity(intent)
                    }
                }
        )
    }
    
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.timeline_compat_activity_title)
            setDisplayHomeAsUpEnabled(true)
        }

        parseMetadata()
//        val layoutManager = LinearLayoutManager(this)
//        layoutManager.reverseLayout = true
//        layoutManager.stackFromEnd = true
//        timeline_items.layoutManager = layoutManager
        timeline_items.adapter = mTimeLineItemAdapter
        items_fastscroller.setViews(timeline_items, null) {
            val item = mListPhotoMapItem.getOrNull(it)
            items_fastscroller.updateBubbleText(item?.getBubbleText() ?: "")
        }
        timeline_items.onGlobalLayout {
            items_fastscroller.setScrollTo(timeline_items.computeVerticalScrollOffset())
        }
        if (mListPhotoMapItem.size > 0) {
            timeline_items.scrollToPosition(mListPhotoMapItem.size - 1);    
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
