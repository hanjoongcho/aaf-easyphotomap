package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_file_explorer.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.ExplorerItemAdapter
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.helper.RegistrationThread
import me.blog.korn123.easyphotomap.models.FileItem
import me.blog.korn123.easyphotomap.utils.CommonUtils
import me.blog.korn123.easyphotomap.utils.DialogUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.io.File
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-16.
 */
class FileExplorerActivity : AppCompatActivity() {

    private var mCurrent: String? = null
    private var mListFile: ArrayList<FileItem>? = null
    private var mListDirectory: ArrayList<FileItem>? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mAdapter: ArrayAdapter<FileItem>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)
        supportActionBar?.title = getString(R.string.file_explorer_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mListFile = ArrayList()
        mListDirectory = ArrayList()
        mCurrent = Constant.CAMERA_DIRECTORY
        registerDirectory.typeface = Typeface.DEFAULT

        mAdapter = ExplorerItemAdapter(this, this, R.layout.item_file_explorer, this.mListFile!!)
        fileList.adapter = mAdapter

        // binding listener
        registerDirectory.setOnClickListener({
            when(mListFile!!.size - mListDirectory!!.size < 1) {
                true -> {
                    DialogUtils.showAlertDialog(this, getString(R.string.file_explorer_message9))
                }
                false -> {
                    val positiveListener = PositiveListener(this@FileExplorerActivity, this@FileExplorerActivity, null, null)
                    DialogUtils.showAlertDialog(this@FileExplorerActivity, getString(R.string.file_explorer_message11), this@FileExplorerActivity, positiveListener)
                }
            }
        })

        fileList.setOnItemClickListener { parent, _, position, _ ->
            val thumbnailEntity = parent.adapter.getItem(position) as FileItem
            var fileName = thumbnailEntity.fileName!!

            if (fileName.startsWith("[") && fileName.endsWith("]")) {
                fileName = fileName.substring(1, fileName.length - 1)
            }

            val path = mCurrent + "/" + fileName
            val f = File(path)

            if (f.isDirectory) {
                mCurrent = path
                refreshFiles()
            } else {
                if (!File(Constant.WORKING_DIRECTORY).exists()) {
                    File(Constant.WORKING_DIRECTORY).mkdirs()
                }
                val positiveListener = PositiveListener(this@FileExplorerActivity, this@FileExplorerActivity, FilenameUtils.getName(path) + ".origin", path)
                DialogUtils.showAlertDialog(this@FileExplorerActivity, getString(R.string.file_explorer_message7), this@FileExplorerActivity, path, positiveListener)
            }
        }

        refreshFiles()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshFiles() {
        val arrayPath = StringUtils.split(mCurrent, "/")
        pathView!!.removeViews(0, pathView!!.childCount)
        var currentPath = ""
        var index = 0
        arrayPath.map { path ->
            currentPath += "/" + path
            val targetPath = currentPath
            val textView = TextView(this)
            if (index < arrayPath.size - 1) {
                textView.text = path + "  >  "
            } else {
                textView.text = path
            }

            if (StringUtils.equals(arrayPath[arrayPath.size - 1], path)) {
                textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.colorPrimary))
            } else {
                textView.typeface = Typeface.DEFAULT
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.defaultFont))
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setOnClickListener {
                mCurrent = targetPath
                refreshFiles()
            }
            pathView!!.addView(textView)
            index++
        }
//        scrollView.postDelayed({ scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT) }, 100L)
        RefreshThread().start()
    }

    override fun onBackPressed() {
        DialogUtils.showAlertDialog(this@FileExplorerActivity, getString(R.string.file_explorer_message12), DialogInterface.OnClickListener { dialogInterface, i -> finish() }, DialogInterface.OnClickListener { dialogInterface, i -> })
    }

    inner class PositiveListener internal constructor(internal var context: Context, internal var activity: Activity, internal var fileName: String?, internal var path: String?) {

        fun register() {
            if (fileName != null && path != null) {
                mProgressDialog = ProgressDialog.show(this@FileExplorerActivity, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6))
                val registerThread = RegistrationThread(context, activity, mProgressDialog!!, fileName, path!!)
                registerThread.start()
            } else {
                val batchIntent = Intent(this@FileExplorerActivity, BatchPopupActivity::class.java)
                val listImagePath = ArrayList<String>()
                for (i in mListDirectory!!.size..mListFile!!.size - 1) {
                    listImagePath.add(mListFile!![i].imagePath!!)
                }

                batchIntent.putStringArrayListExtra("listImagePath", listImagePath)
                startActivity(batchIntent)
            }
        }
    }

    internal inner class RefreshThread : Thread() {
        override fun run() {
            Handler(Looper.getMainLooper()).post {
                mListFile!!.clear()
                mListDirectory!!.clear()
                val current = File(this@FileExplorerActivity.mCurrent!!)
                val files = current.list()
                if (files != null) {
                    for (i in files.indices) {
                        val thumbnailEntity = FileItem()
                        val path = this@FileExplorerActivity.mCurrent + "/" + files[i]
                        var name = ""
                        val f = File(path)
                        if (f.isDirectory) {
                            name = "[" + files[i] + "]"
                            thumbnailEntity.setImagePathAndFileName(name)
                            thumbnailEntity.isDirectory = true
                            mListDirectory!!.add(thumbnailEntity)
                        } else {
                            name = files[i]
                            val extension = FilenameUtils.getExtension(name).toLowerCase()
                            if (!extension.matches("jpg|jpeg".toRegex())) continue
                            thumbnailEntity.setImagePathAndFileName(path)
                            mListFile!!.add(thumbnailEntity)
                        }
                    }
                }

                if (CommonUtils.loadBooleanPreference(this@FileExplorerActivity, "enable_reverse_order")) {
                    Collections.sort(mListDirectory!!, Collections.reverseOrder<Any>())
                    Collections.sort(mListFile!!, Collections.reverseOrder<Any>())
                } else {
                    Collections.sort(mListDirectory!!)
                    Collections.sort(mListFile!!)
                }
                mListFile!!.addAll(0, mListDirectory!!)
                mAdapter!!.notifyDataSetChanged()
                fileList.setSelection(0)
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
            }
        }
    }

}
