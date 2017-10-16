package me.blog.korn123.easyphotomap.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.beardedhen.androidbootstrap.TypefaceProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.activity_maps.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.*
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.io.File
import java.util.*
import java.util.regex.Pattern

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private val mListLatLng = arrayListOf<LatLng>()
    private val mListPhotoEntity = arrayListOf<PhotoMapItem>()
    private val mListRecommendationOrigin = arrayListOf<Recommendation>()
    private val mListRecommendation = arrayListOf<Recommendation>()
    private val mListMarkerOptions = arrayListOf<MarkerOptions>()
    private val mRecommendMap: HashMap<String, Int> = hashMapOf()
    private var mMap: GoogleMap? = null
    private var mProgressDialog: ProgressDialog? = null
    private var mPopupWindow: PopupWindow? = null
    private var mClusterManager: ClusterManager<MyItem>? = null
    private var mListPhotoMapItem: ArrayList<PhotoMapItem>? = null
    private var mEnableDateFilter: Boolean = false
    private var mAdapter: ArrayAdapter<*>? = null
    private var mListView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        TypefaceProvider.registerDefaultIconSets()
        setContentView(R.layout.activity_maps)
        CommonUtils.initWorkingDirectory()

        // Obtain the SupportMapFragment and get notified when the mRecommendMap is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        floatingMenu.setClosedOnTouchOutside(true)
        floatingMenu.setOnMenuButtonClickListener { floatingMenu.toggle(true) }
        camera.setOnClickListener(mMenuClickListener)
        explorer.setOnClickListener(mMenuClickListener)
        folder.setOnClickListener(mMenuClickListener)
        overlay.setOnClickListener(mMenuClickListener)
        find.setOnClickListener(mMenuClickListener)
        timeline.setOnClickListener(mMenuClickListener)
        setting.setOnClickListener(mMenuClickListener)
    }

    /**
     * Manipulates the mRecommendMap once available.
     * This callback is triggered when the mRecommendMap is ready to be used.
     * This is where we can add markers or lines, add listeners or move the activity_camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.let { it ->
            it.setOnCameraChangeListener { cameraPosition ->
                if (cameraPosition.zoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                    it.animateCamera(CameraUpdateFactory.zoomTo(Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE))
                }
            }
        }

        if (intent.hasExtra("info")) {
            mMap?.let { it ->
                val info = intent.getStringExtra("info")
                val imagePath = intent.getStringExtra("imagePath")
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val date = intent.getStringExtra("date")
                val fOptions = MarkerOptions()
                val latLng = LatLng(latitude, longitude)
                fOptions.position(latLng)
                fOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                it.setInfoWindowAdapter(InfoWindow(info, imagePath, latitude, longitude, date))
                it.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15.0f), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        val marker = mMap!!.addMarker(fOptions)
                        it.setOnInfoWindowClickListener {
                            val imageViewIntent = Intent(this@MapsActivity, PopupImageActivity::class.java)
                            imageViewIntent.putExtra("imagePath", imagePath)
                            startActivity(imageViewIntent)
                        }
                        marker.showInfoWindow()
                    }

                    override fun onCancel() {}
                })
            }
        } else {
            val location = GPSUtils.getLocationWithGPSProvider(this)
            if (location == null) {
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(Constant.GOOGLE_MAP_DEFAULT_LATITUDE, Constant.GOOGLE_MAP_DEFAULT_LONGITUDE),
                        Constant.GOOGLE_MAP_DEFAULT_ZOOM_VALUE)
                )
            } else {
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), Constant.GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
            }
        }

        migrateLegacyData()
    }

    override fun onResume() {
        super.onResume()
        parseMetadata()
    }

    private fun migrateLegacyData() {
        val legacyFile = File(Constant.LEGACY_PHOTO_DATA_PATH)
        if (legacyFile.exists()) {
            val listPhotoMapData = CommonUtils.readDataFile(Constant.LEGACY_PHOTO_DATA_PATH)
            listPhotoMapData?.map { data ->
                val temps = StringUtils.split(data, "|")
                val item = PhotoMapItem().apply {
                    imagePath = temps[0]
                    info = temps[1]
                    latitude = java.lang.Double.valueOf(temps[2])
                    longitude = java.lang.Double.valueOf(temps[3])
                    date = temps[4]
                }
                PhotoMapDbHelper.insertPhotoMapItem(item)
            }
            legacyFile.renameTo(File(Constant.LEGACY_PHOTO_DATA_PATH + "_BAK"))
            parseMetadata()
        }
    }

    private fun parseMetadata() {
        mRecommendMap.clear()
        mListPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll()
        mListPhotoMapItem?.let {
            Collections.sort(it)
        }
    }

    private var mMenuClickListener: View.OnClickListener = View.OnClickListener { view ->
        floatingMenu!!.close(false)
        when (view.id) {
            R.id.camera -> if (CommonUtils.loadBooleanPreference(this@MapsActivity, "disable_info_popup")) {
                val camera = Intent(view.context, CameraActivity::class.java)
                startActivity(camera)
            } else {
                val infoView = layoutInflater.inflate(R.layout.popup_window_camera, null)
                val textView2 = infoView.findViewById(R.id.textView2) as TextView
                val textView3 = infoView.findViewById(R.id.textView3) as TextView
                mPopupWindow = PopupWindow(infoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                CommonUtils.bindButtonEffect(textView2)
                CommonUtils.bindButtonEffect(textView3)
                textView2.setOnClickListener { v ->
                    mPopupWindow!!.dismiss()
                    val camera = Intent(v.context, CameraActivity::class.java)
                    startActivity(camera)
                }
                textView3.setOnClickListener { v ->
                    CommonUtils.saveBooleanPreference(this@MapsActivity, "disable_info_popup", true)
                    mPopupWindow!!.dismiss()
                    val camera = Intent(v.context, CameraActivity::class.java)
                    startActivity(camera)
                }
                mPopupWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
            }
            R.id.explorer -> {
                val intent = Intent(view.context, ThumbnailExplorerActivity::class.java)
                startActivity(intent)
            }
            R.id.overlay -> {
                mEnableDateFilter = CommonUtils.loadBooleanPreference(this@MapsActivity, "date_filter_setting")
                mMap?.clear()
                parseMetadata()
                mListPhotoMapItem?.size
                if (mListPhotoMapItem?.size == null) {
                    DialogUtils.showAlertDialog(this, getString(R.string.maps_activity_message2))
                } else {
                    val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val customView = inflater.inflate(R.layout.popup_window_recommendation, null)
                    mListView = customView.findViewById(R.id.listView) as ListView
                    FontUtils.setChildViewTypeface(customView as ViewGroup)
                    val listOfSortEntry: List<Map.Entry<String, Int>>?
                    if (mEnableDateFilter) {
                        mListPhotoMapItem?.map { item ->
                            val date: String? = when (item.date?.contains("(")) {
                                true -> item.date?.substring(0, item.date!!.lastIndexOf("("))
                                false -> item.date
                                null -> null
                            }
                            date?.let {
                                if (mRecommendMap.containsKey(it)) {
                                    mRecommendMap.put(it, mRecommendMap[it]!!.plus(1))
                                } else {
                                    mRecommendMap.put(it, 1)
                                }
                            }
                        }
                        listOfSortEntry = CommonUtils.entriesSortedByKeys(mRecommendMap)
                    } else {
                        mListPhotoMapItem!!.map { it ->
                            val arr = StringUtils.split(it.info, " ")
                            arr.map { str ->
                                if (Pattern.matches("^(([0-9]{1,9})-([0-9]{1,9}))|(([0-9]{1,9}))$", str) || str.length < 2) {
                                } else if (mRecommendMap.containsKey(str)) {
                                    mRecommendMap.put(str, mRecommendMap[str]!!.plus(1))
                                } else {
                                    mRecommendMap.put(str, 1)
                                }
                            }
                        }
                        listOfSortEntry = CommonUtils.entriesSortedByValues(mRecommendMap)
                    }

                    mListRecommendationOrigin.clear()
                    mListRecommendation.clear()
                    for ((key, value) in listOfSortEntry) {
                        mListRecommendationOrigin.add(Recommendation(key, value))
                    }

                    mListRecommendation.addAll(mListRecommendationOrigin)
                    mAdapter = ArrayAdapter(this, R.layout.item_recommendation, mListRecommendation)
                    mListView!!.adapter = mAdapter
                    mListView!!.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, id ->
                        val recommendation = parent.adapter.getItem(position) as Recommendation
                        mPopupWindow!!.dismiss()
                        overlayIcons(recommendation.keyWord, mEnableDateFilter)
                    }
                    val point = CommonUtils.getDefaultDisplay(this)
                    customView.findViewById(R.id.viewWorld).setOnClickListener {
                        mPopupWindow!!.dismiss()
                        overlayIcons("", false)
                    }
                    customView.findViewById(R.id.close).setOnClickListener { mPopupWindow!!.dismiss() }

                    val searchView = customView.findViewById(R.id.searchKey) as SearchView
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean = false

                        override fun onQueryTextChange(newText: String): Boolean {
                            mListRecommendation.clear()
                            mListRecommendationOrigin.map{ it -> if (StringUtils.contains(it.keyWord, newText)) {
                                mListRecommendation.add(it)
                            }}
                            mAdapter!!.notifyDataSetChanged()
                            return false
                        }
                    })
                    val contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).top
                    mPopupWindow = PopupWindow(customView, point.x, point.y - contentViewTop, true)
                    mPopupWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
                }
            }
            R.id.find -> {
                if (mListPhotoMapItem!!.size < 1) {
                    DialogUtils.showAlertDialog(this, getString(R.string.maps_activity_message2))
                } else {
                    val photoSearchIntent = Intent(this, PhotoSearchActivity::class.java)
                    startActivity(photoSearchIntent)
                }
            }
            R.id.setting -> {
                val settingIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingIntent)
            }
            R.id.folder -> {
                val fileExplorerIntent = Intent(this@MapsActivity, FileExplorerActivity::class.java)
                startActivity(fileExplorerIntent)
            }
            R.id.timeline -> {
                if (mListPhotoMapItem!!.size < 1) {
                    DialogUtils.showAlertDialog(this, getString(R.string.maps_activity_message2))
                } else {
                    val timelineIntent = Intent(this@MapsActivity, TimelineActivity::class.java)
                    startActivity(timelineIntent)
                }
            }
            else -> {
            }
        }
    }

    private fun setUpCluster() {
        if (mClusterManager == null) {
            mClusterManager = ClusterManager(this, mMap)
        } else {
            mClusterManager?.clearItems()
        }
    }

    private fun overlayIcons(keyword: String, applyFilter: Boolean) {
        val overlayThread = OverlayThread(keyword, applyFilter)
        overlayThread.start()
        mProgressDialog = ProgressDialog(this@MapsActivity).apply {
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setMessage("find $keyword...")
            max = when(applyFilter) {
                true -> PhotoMapDbHelper.containsPhotoMapItemBy("dateWithoutTime", keyword).size
                false -> PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword).size
            }
        }
        mProgressDialog?.show()
    }

    inner class OverlayThread(private val keyword: String, private val applyFilter: Boolean) : Thread() {

        override fun run() {
            super.run()
            val listTemp = when(applyFilter) {
                true -> {
                    PhotoMapDbHelper.containsPhotoMapItemBy("dateWithoutTime", keyword)
                }
                false -> {
                    PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword)
                }
            }
            mListLatLng.clear()
            mListMarkerOptions.clear()
            mListPhotoEntity.clear()

            for ((index, item) in listTemp.withIndex()) {
                val info = item.info
                Handler(Looper.getMainLooper()).post {
                    mProgressDialog?.progress = index + 1
                    mProgressDialog?.setMessage(info)
                    val textView = mProgressDialog?.findViewById(android.R.id.message) as TextView?
                    textView?.ellipsize = TextUtils.TruncateAt.MIDDLE
                    textView?.maxLines = 1
                }

                val options = MarkerOptions()
                val latLng = LatLng(item.latitude, item.longitude)
                options.position(latLng)
                val fileName = FilenameUtils.getName(item.imagePath)
                val bm: Bitmap = BitmapUtils.decodeFile(this@MapsActivity, Constant.WORKING_DIRECTORY + fileName + ".thumb")!!
                val image = when (CommonUtils.loadStringPreference(this@MapsActivity, "photo_marker_setting", "basicFrame")) {
                    "filmFrame" -> {
                        val point = Point(bm.width, bm.height)
                        val fixedWidthHeight = java.lang.Double.parseDouble(CommonUtils.loadStringPreference(this@MapsActivity, "photo_size_setting", "0.6"))
                        val bm2 = BitmapUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight)!!
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.addFrame(this@MapsActivity, bm2, CommonUtils.dpToPixel(this@MapsActivity, 6f), R.drawable.frame_03))
                    }
                    "basicFrame" -> {
                        val point = Point(bm.width, bm.height)
                        val fixedWidthHeight = java.lang.Double.parseDouble(CommonUtils.loadStringPreference(this@MapsActivity, "photo_size_setting", "0.6"))
                        val bm2 = BitmapUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight)!!
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.border(bm2, CommonUtils.dpToPixel(this@MapsActivity, 1.5f)))
                    }
                    "flowerFrame" -> {
                        val point = Point(bm.width, bm.height)
                        val fixedWidthHeight = java.lang.Double.parseDouble(CommonUtils.loadStringPreference(this@MapsActivity, "photo_size_setting", "0.6"))
                        val bm2 = BitmapUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight)!!
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.addFrame(this@MapsActivity, bm2, CommonUtils.dpToPixel(this@MapsActivity, 6f), R.drawable.frame_02))
                    }
                    else -> {
                        var px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
                        px = (px * java.lang.Double.parseDouble(CommonUtils.loadStringPreference(this@MapsActivity, "photo_size_setting", "0.6"))).toInt()
                        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(mDotMarkerBitmap)
                        val shape = resources.getDrawable(R.drawable.circle)
                        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
                        shape.draw(canvas)
                        BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)
                    }
                }
                options.icon(image)
                mListMarkerOptions.add(options)
                mListPhotoEntity.add(item)
            }

            Handler(Looper.getMainLooper()).post {
                mProgressDialog?.dismiss()
                setUpCluster()
                val listTemp = when(applyFilter) {
                    true -> {
                        PhotoMapDbHelper.containsPhotoMapItemBy("dateWithoutTime", keyword)
                    }
                    false -> {
                        PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword)
                    }
                }
                for (i in mListMarkerOptions.indices) {
                    val item = MyItem(mListMarkerOptions[i], listTemp[i])
                    mClusterManager?.addItem(item)
                    mListLatLng.add(mListMarkerOptions[i].position)
                }

                mMap?.let { map ->
                    mClusterManager?.let { clusterManager ->
                        map.setOnMarkerClickListener(clusterManager)
                        map.setOnCameraChangeListener(clusterManager)

                        val clusterRenderer = MyClusterRenderer(this@MapsActivity, map, clusterManager)
                        clusterManager.setRenderer(clusterRenderer)

                        clusterManager.setOnClusterClickListener {
                            map.setInfoWindowAdapter(null)
                            false
                        }

                        clusterManager.setOnClusterItemClickListener { item ->
                            map.setInfoWindowAdapter(InfoWindow(
                                    item.photoEntity.info!!,
                                    item.photoEntity.imagePath!!,
                                    item.photoEntity.latitude,
                                    item.photoEntity.longitude,
                                    item.photoEntity.date!!
                            ))
                            val fImagePath = item.photoEntity.imagePath
                            map.setOnInfoWindowClickListener {
                                val imageViewIntent = Intent(this@MapsActivity, PopupImageActivity::class.java)
                                imageViewIntent.putExtra("imagePath", fImagePath)
                                startActivity(imageViewIntent)
                            }
                            false
                        }

                        val builder = LatLngBounds.Builder()
                        for (latLng in mListLatLng) {
                            builder.include(latLng)
                        }
                        val bounds = builder.build()
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
                    }
                }
            }
        }
    }

    inner class MyItem(val markerOptions: MarkerOptions, val photoEntity: PhotoMapItem) : ClusterItem {
        override fun getPosition(): LatLng = markerOptions.position
    }

    inner class MyClusterRenderer(context: Context, map: GoogleMap,
                                           clusterManager: ClusterManager<MyItem>) : DefaultClusterRenderer<MyItem>(context, map, clusterManager), GoogleMap.OnCameraChangeListener {
        private var mClusterIconGenerator: IconGenerator? = null
        private var mapZoom: Float = 0f

        init {
            mClusterIconGenerator = IconGenerator(applicationContext)
        }

        override fun onBeforeClusterItemRendered(item: MyItem?,
                                                 markerOptions: MarkerOptions?) {
            super.onBeforeClusterItemRendered(item, markerOptions)
            markerOptions!!.icon(item!!.markerOptions.icon)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
            return if (mapZoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE - 1) {
                false
            } else {
                cluster.size > Integer.valueOf(CommonUtils.loadStringPreference(this@MapsActivity, "photo_marker_enable_minimum_cluster", "10"))
            }

        }

        override fun onCameraChange(cameraPosition: CameraPosition) {
            if (cameraPosition.zoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE))
            }
            mapZoom = cameraPosition.zoom
        }
    }

    inner class InfoWindow(var info: String, var imagePath: String, var latitude: Double, var longitude: Double, var date: String) : GoogleMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker): View? = null

        override fun getInfoContents(marker: Marker): View {

            val cw = ContextThemeWrapper(applicationContext, R.style.Transparent)
            val inflater = cw.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.popup_window_photo_map_info, null)
            val imageView = view.findViewById(R.id.info1) as ImageView
            //            imageView.setImageResource(imageId);
            val imgFile = File(imagePath)

            if (imgFile.exists()) {
                var orientation = 0
                val matrix = Matrix()
                try {
                    val exif = ExifInterface(imagePath)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                    when (orientation) {
                        6 -> matrix.postRotate(90f)
                        3 -> matrix.postRotate(180f)
                        8 -> matrix.postRotate(270f)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                options.inSampleSize = 5
                var bitmap = BitmapUtils.decodeFile(this@MapsActivity, imgFile.absolutePath, options)!!
                if (orientation > 1) {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) // rotating bitmap
                }

                val height = bitmap.height
                val width = bitmap.width
                val resized = when (orientation > 1) {
                    true -> {
                        val downSampleHeight = height / width.toFloat() * 200
                        Bitmap.createScaledBitmap(bitmap, 200, downSampleHeight.toInt(), true)
                    }
                    false -> {
                        val downSampleWidth = width / height.toFloat() * 200
                        Bitmap.createScaledBitmap(bitmap, downSampleWidth.toInt(), 200, true)
                    }
                }
                imageView.setImageBitmap(resized)
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_gallery))
            }
            val lat = StringUtils.substring(latitude.toString(), 0, 6)
            val lon = StringUtils.substring(longitude.toString(), 0, 6)
            (view.findViewById(R.id.info2) as TextView).text = "lat: $lat lon: $lon \n $info"
            (view.findViewById(R.id.info3) as TextView).text = date
            return view
        }
    }

    private inner class Recommendation(internal var keyWord: String, internal var count: Int) {
        override fun toString(): String {
            val unit = getString(R.string.photo_map_item_count_unit)
            return "$keyWord [$count$unit]"
        }
    }

}
