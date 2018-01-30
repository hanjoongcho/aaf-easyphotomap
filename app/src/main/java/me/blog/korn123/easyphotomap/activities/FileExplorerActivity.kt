package me.blog.korn123.easyphotomap.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.*
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_file_explorer.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.ExplorerItemAdapter
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.dialogs.ChangeSortingDialog
import me.blog.korn123.easyphotomap.extensions.config
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
class FileExplorerActivity : SimpleActivity() {

    private val mListFile: ArrayList<FileItem> = arrayListOf()
    private val mListDirectory: ArrayList<FileItem> = arrayListOf()
    private var mCurrent: String? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mAdapter: ArrayAdapter<FileItem>? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        setSupportActionBar(toolbar)
        supportActionBar?.run {
            title = getString(R.string.file_explorer_activity_title)
            setDisplayHomeAsUpEnabled(true)
        }

        mCurrent = Constant.CAMERA_DIRECTORY
        mAdapter = ExplorerItemAdapter(this, this, R.layout.item_file_explorer, this.mListFile)
        fileList.adapter = mAdapter

        fileList.setOnItemClickListener { parent, _, position, _ ->
            val thumbnailEntity = parent.adapter.getItem(position) as FileItem
            var fileName = thumbnailEntity.fileName

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
                DialogUtils.showAlertDialog(
                        this@FileExplorerActivity,
                        getString(R.string.file_explorer_message7),
                        this@FileExplorerActivity,
                        path,
                        positiveListener
                )
            }
        }

        refreshFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.file_explorer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.sort -> showSortingDialog()
            R.id.batchModeA -> {
                val positiveListener = DialogInterface.OnClickListener { _, _ ->
                    val batchIntent = Intent(this@FileExplorerActivity, BatchPopupActivity::class.java)
                    val listImagePath = arrayListOf<String>()
                    File(mCurrent).listFiles().map { file ->
                        if (file.absoluteFile.extension.toLowerCase().matches("jpg|jpeg".toRegex())) {
                            listImagePath.add(file.absolutePath)
                        }
                    }
                    batchIntent.putStringArrayListExtra("listImagePath", listImagePath)
                    startActivity(batchIntent)
                    return@OnClickListener
                }

                AlertDialog.Builder(this@FileExplorerActivity).apply {
                    setMessage(getString(R.string.file_explorer_message11))
                    setPositiveButton(getString(R.string.confirm), positiveListener)
                    setNegativeButton(getString(R.string.cancel), null)
                }.create().show()
            }
            R.id.batchModeB -> {
                val positiveListener = DialogInterface.OnClickListener { _, _ ->
                    val batchIntent = Intent(this@FileExplorerActivity, BatchPopupActivity::class.java)
                    batchIntent.putExtra("currentPath", mCurrent)
                    startActivity(batchIntent)
                    return@OnClickListener
                }

                AlertDialog.Builder(this@FileExplorerActivity).apply {
                    setMessage(getString(R.string.file_explorer_message13))
                    setPositiveButton(getString(R.string.confirm), positiveListener)
                    setNegativeButton(getString(R.string.cancel), null)
                }.create().show()
            }
        }
        return true
    }

    override fun onBackPressed() {
        DialogUtils.showAlertDialog(
                this@FileExplorerActivity,
                getString(R.string.file_explorer_message12),
                DialogInterface.OnClickListener { _, _ -> finish() },
                DialogInterface.OnClickListener { _, _ -> }
        )
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this, true, false) {
//            if (config.directorySorting and SORT_BY_DATE_MODIFIED > 0 || config.directorySorting and SORT_BY_DATE_TAKEN > 0) {
//                getDirectories()
//            } else {
//                gotDirectories(mDirs, true)
//            }
            refreshFiles()
        }
    }

    private fun refreshFiles() {
        FileItem.sorting = config.directorySorting
        val arrayPath = StringUtils.split(mCurrent, "/")
        pathView.removeViews(0, pathView.childCount)
        var currentPath = ""
        var index = 0
        arrayPath.map { path ->
            currentPath += "/" + path
            val targetPath = currentPath
            val textView = TextView(this)
            if (index < arrayPath.size - 1) {
                textView.text = "$path  >  "
            } else {
                textView.text = path
            }

            if (StringUtils.equals(arrayPath[arrayPath.size - 1], path)) {
                textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.colorPrimary))
            } else {
                textView.typeface = Typeface.DEFAULT
                textView.setTextColor(ContextCompat.getColor(this@FileExplorerActivity, R.color.default_text_color))
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.setOnClickListener {
                mCurrent = targetPath
                refreshFiles()
            }
            pathView.addView(textView)
            index++
        }
        progressDialog.visibility = View.VISIBLE
        RefreshThread().start()
    }

    inner class PositiveListener internal constructor(val context: Context, val activity: Activity, val fileName: String, private val path: String) {
        fun register() {
            mProgressDialog = ProgressDialog.show(this@FileExplorerActivity, getString(R.string.file_explorer_message5), getString(R.string.file_explorer_message6))
            mProgressDialog?.let { it ->
                val registerThread = RegistrationThread(context, activity, it, fileName, path)
                registerThread.start()
            }
        }
    }

    inner class RefreshThread : Thread() {
        override fun run() {
            mListFile.clear()
            mListDirectory.clear()
            val current = File(mCurrent)
            val files = current.list()
            if (files != null) {
                for (i in files.indices) {
                    val fileItem = FileItem()
                    val path = "$mCurrent/${files[i]}"
                    val name: String
                    val f = File(path)
                    if (f.isDirectory) {
                        name = "[${files[i]}]"
                        fileItem.setImagePathAndFileName(name)
                        fileItem.isDirectory = true
                        mListDirectory.add(fileItem)
                    } else {
                        name = files[i]
                        val extension = FilenameUtils.getExtension(name).toLowerCase()
                        if (!extension.matches("jpg|jpeg".toRegex())) continue
                        fileItem.setImagePathAndFileName(path)
                        try {
                            fileItem.length = f.length()
                            fileItem.takenDate = CommonUtils.getDateFromJpegMetaData(f)
                        } catch (e: Exception){
                            e.printStackTrace()
                        }

                        mListFile.add(fileItem)
                    }
                }
            }

//                if (config.enableReverseOrder) {
//                    Collections.sort(mListDirectory, Collections.reverseOrder<Any>())
//                    Collections.sort(mListFile, Collections.reverseOrder<Any>())
//                } else {
            Collections.sort(mListDirectory)
            Collections.sort(mListFile)
//                }
            mListFile.addAll(0, mListDirectory)
            
            Handler(Looper.getMainLooper()).post {
                mAdapter?.notifyDataSetChanged()
                fileList.setSelection(0)
                scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                progressDialog.visibility = View.GONE
            }
        }
    }
}
