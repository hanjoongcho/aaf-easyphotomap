package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_thumbnail_explorer.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.ThumbnailItemAdapter
import me.blog.korn123.easyphotomap.helper.RegistrationThread
import me.blog.korn123.easyphotomap.models.ThumbnailItem
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import org.apache.commons.io.FilenameUtils
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
class ThumbnailExplorerActivity : AppCompatActivity() {
    private var mThumbnailEntityAdapter: ThumbnailItemAdapter? = null
    private var mEnableUpdate = false
    private var mProgressDialog: ProgressDialog? = null
    private var mThumbnailTotal = 0
    private var mCompleted = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thumbnail_explorer)
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.thumbnail_explorer_compact_activity_title)
            setDisplayHomeAsUpEnabled(true)
        }
        setAdapter()
        thumbnailGrid.columnWidth = ((CommonUtils.getDefaultDisplay(this).x - CommonUtils.dpToPixel(this@ThumbnailExplorerActivity, 30f, 1)) / 3)
        setOnItemClickListener()

        findViewById<View>(R.id.startSync).setOnClickListener {
            val thread = ThumbnailCreatorThread(this@ThumbnailExplorerActivity)
            mEnableUpdate = true
            thread.start()
        }
        findViewById<View>(R.id.stopSync).setOnClickListener { mEnableUpdate = false }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.thumbnail_explorer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.explorer -> {
                val intent = Intent(this@ThumbnailExplorerActivity, FileExplorerActivity::class.java)
                startActivity(intent)
            }
            android.R.id.home -> finish()
            R.id.update -> {
                val thread = ThumbnailCreatorThread(this.applicationContext)
                thread.start()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setAdapter() {
        val listPhotoEntity = CommonUtils.fetchAllThumbnail(this@ThumbnailExplorerActivity)
        mThumbnailEntityAdapter = ThumbnailItemAdapter(this, this, R.layout.item_thumbnail, listPhotoEntity)
        thumbnailGrid.adapter = mThumbnailEntityAdapter
    }

    private fun setOnItemClickListener() {
        thumbnailGrid.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val photoEntity = parent.adapter.getItem(position) as ThumbnailItem
            val imagePath = CommonUtils.getOriginImagePath(this@ThumbnailExplorerActivity, photoEntity.imageId)
            val positiveListener = PositiveListener(this@ThumbnailExplorerActivity, this@ThumbnailExplorerActivity, FilenameUtils.getName(imagePath) + ".origin", imagePath)
            if (imagePath == null) {
                DialogUtils.showAlertDialog(this@ThumbnailExplorerActivity, getString(R.string.thumbnail_explorer_message4))
            } else {
                DialogUtils.showAlertDialog(this@ThumbnailExplorerActivity, getString(R.string.file_explorer_message7), this@ThumbnailExplorerActivity, imagePath, positiveListener)
            }
        }
    }

    inner class PositiveListener internal constructor(internal var context: Context, internal var activity: Activity, internal var fileName: String?, private var path: String?) {
        fun register() {
            if (fileName != null && path != null) {
                mProgressDialog = ProgressDialog.show(this@ThumbnailExplorerActivity, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6))
                val registerThread = RegistrationThread(context, activity, mProgressDialog!!, fileName, path!!)
                registerThread.start()
            }
        }
    }

    inner class ThumbnailCreatorThread(var context: Context) : Thread() {
        override fun run() {
            val listOriginImage = CommonUtils.fetchAllImages(context)
            val listThumbnail = CommonUtils.fetchAllThumbnail(context)
            val listImageId = ArrayList<String>()
            listThumbnail.map { thumbnailItem -> listImageId.add(thumbnailItem.imageId) }
            mCompleted = listThumbnail.size

            Handler(Looper.getMainLooper()).post {
                val layout = findViewById<LinearLayout>(R.id.infoView)
                layout.visibility = View.VISIBLE
            }

            for (entity in listOriginImage) {
                if (!mEnableUpdate) break
                if (listImageId.contains(entity.imageId)) {
                    continue
                }
                MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, java.lang.Long.parseLong(entity.imageId), MediaStore.Images.Thumbnails.MINI_KIND, null)
                Handler(Looper.getMainLooper()).post {
                    (findViewById<TextView>(R.id.progressView)).text = "Total: $mThumbnailTotal"
                    (findViewById<TextView>(R.id.progressView2)).text = "Completed: ${++mCompleted}"
                }
            }

            mThumbnailTotal = listOriginImage.size
            Handler(Looper.getMainLooper()).post {
                progressView.text = "Total: $mThumbnailTotal"
                progressView2.text = "Completed: $mCompleted"
                setAdapter()
                mThumbnailEntityAdapter?.notifyDataSetChanged()
            }
        }
    }
}
