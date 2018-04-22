package me.blog.korn123.easyphotomap.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.widget.*
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
import com.simplemobiletools.commons.extensions.onGlobalLayout
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PERMISSION_CAMERA
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.views.FastScroller
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_COARSE_LOCATION
import io.github.aafactory.commons.helpers.PERMISSION_ACCESS_FINE_LOCATION
import io.github.aafactory.commons.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_maps.*
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.adapters.RecommendationItemAdapter
import me.blog.korn123.easyphotomap.extensions.config
import me.blog.korn123.easyphotomap.extensions.getLocationWithGPSProvider
import me.blog.korn123.easyphotomap.extensions.showAlertDialog
import me.blog.korn123.easyphotomap.helper.*
import me.blog.korn123.easyphotomap.models.PhotoMapItem
import me.blog.korn123.easyphotomap.utils.EasyPhotoMapUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import java.io.File
import java.util.*
import java.util.regex.Pattern

class MapsActivity : SimpleActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val mListLatLng = arrayListOf<LatLng>()
    private val mListPhotoEntity = arrayListOf<PhotoMapItem>()
    private val mListRecommendationOrigin = arrayListOf<Recommendation>()
    private val mListRecommendation = arrayListOf<Recommendation>()
    private val mListMarkerOptions = arrayListOf<MarkerOptions>()
    private val mRecommendMap: HashMap<String, Int> = hashMapOf()
    private var mProgressDialog: ProgressDialog? = null
    private var mPopupWindow: PopupWindow? = null
    private var mClusterManager: ClusterManager<MyItem>? = null
    private var mListPhotoMapItem: ArrayList<PhotoMapItem>? = null
    private var mEnableDateFilter: Boolean = false
    private var mHaveCameraPosition = false
    private var mSavedCameraLatitude: Double = 0.0
    private var mSavedCameraLongitude: Double = 0.0
    private var mSavedCameraZoom: Float = 0F
    private var isMapReady = false

    private val mRecommendationAdapter: RecommendationItemAdapter by lazy {
        RecommendationItemAdapter(
                this,
                mListRecommendation,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    mPopupWindow?.dismiss()
                    overlayIcons(mRecommendationAdapter.getItem(position).keyWord, mEnableDateFilter)
                }
        )
    }
    
    private var mMenuClickListener: View.OnClickListener = View.OnClickListener { view ->
        floatingMenu?.close(false)
        when (view.id) {
            R.id.camera -> {
                handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        handlePermission(PERMISSION_WRITE_STORAGE) {
                            if (it) {
                                handlePermission(PERMISSION_CAMERA) {
                                    if (it) {
                                        if (config.disableCameraInformation) {
                                            val camera = Intent(view.context, CameraActivity::class.java)
                                            startActivity(camera)
                                        } else {
                                            val infoView = layoutInflater.inflate(R.layout.popup_window_camera, null)
                                            val textView2 = infoView.findViewById<TextView>(R.id.textView2)
                                            val textView3 = infoView.findViewById<TextView>(R.id.textView3)
                                            mPopupWindow = PopupWindow(infoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                            textView2.setOnClickListener { v ->
                                                mPopupWindow?.dismiss()
                                                val camera = Intent(v.context, CameraActivity::class.java)
                                                startActivity(camera)
                                            }
                                            textView3.setOnClickListener { v ->
                                                config.disableCameraInformation = true
                                                mPopupWindow?.dismiss()
                                                val camera = Intent(v.context, CameraActivity::class.java)
                                                startActivity(camera)
                                            }
                                            mPopupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
                                        }
                                    } else {
                                        toast(R.string.no_camera_permissions)
                                    }
                                }
                            } else {
                                toast(R.string.no_storage_permissions)
                            }
                        }
                    } else {
                        toast(R.string.no_storage_permissions)
                    }
                }
            }
            R.id.thumbnailViewer -> {
                handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        handlePermission(PERMISSION_WRITE_STORAGE) {
                            if (it) {
                                val intent = Intent(view.context, ThumbnailExplorerActivity::class.java)
                                startActivity(intent)
                            } else {
                                toast(R.string.no_storage_permissions)
                            }
                        }
                    } else {
                        toast(R.string.no_storage_permissions)
                    }
                }
            }
            R.id.fileManager -> {
                handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        handlePermission(PERMISSION_WRITE_STORAGE) {
                            if (it) {
                                val fileExplorerIntent = Intent(this@MapsActivity, FileExplorerActivity::class.java)
                                startActivity(fileExplorerIntent)
                            } else {
                                toast(R.string.no_storage_permissions)
                            }
                        }
                    } else {
                        toast(R.string.no_storage_permissions)
                    }
                }
            }
            R.id.overlay -> {
                var recyclerView: RecyclerView
                mEnableDateFilter = config.enableDateFilter
                mMap.clear()
                parseMetadata()
                mListPhotoMapItem?.let { listPhotoMapItem ->
                    if (listPhotoMapItem.size == 0) {
                        showAlertDialog(getString(R.string.maps_activity_message2))
                    } else {
                        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val customView = inflater.inflate(R.layout.popup_window_recommendation, null)
                        recyclerView = customView.findViewById<RecyclerView>(R.id.recommendation_items)
                        EasyPhotoMapUtils.setChildViewTypeface(customView as ViewGroup)
                        val listOfSortEntry: List<Map.Entry<String, Int>>?
                        if (mEnableDateFilter) {
                            listPhotoMapItem.map { item ->
                                val date: String? = when (item.date.contains("(")) {
                                    true -> item.date.substring(0, item.date.lastIndexOf("("))
                                    false -> item.date
                                }

                                date?.let {
                                    val count = mRecommendMap[it]?.plus(1) ?: 1
                                    mRecommendMap.put(it, count)
                                }
                            }
                            listOfSortEntry = EasyPhotoMapUtils.entriesSortedByKeys(mRecommendMap)
                        } else {
                            listPhotoMapItem.map { it ->
                                val pattern = "[0-9]{1,9}"
                                val regexString = "^($pattern-$pattern)|$pattern$"
                                StringUtils.split(it.info, " ")?.map { str ->
                                    if (!Pattern.matches(regexString, str) && str.length > 1) {
                                        val count = mRecommendMap[str]?.plus(1) ?: 1
                                        mRecommendMap.put(str, count)
                                    }
                                }
                            }
                            listOfSortEntry = EasyPhotoMapUtils.entriesSortedByValues(mRecommendMap)
                        }

                        mListRecommendationOrigin.clear()
                        mListRecommendation.clear()
                        listOfSortEntry.map { mListRecommendationOrigin.add(Recommendation(it.key, it.value)) }

                        mListRecommendation.addAll(mListRecommendationOrigin)

                        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
                        AppCompatResources.getDrawable(this, R.drawable.divider_default)?.let {
                            dividerItemDecoration.setDrawable(it)
                            recyclerView.adapter = mRecommendationAdapter
                            recyclerView.addItemDecoration(dividerItemDecoration)
                        }
                        val fastScroller = customView.findViewById<FastScroller>(R.id.items_fastscroller)
                        fastScroller.setViews(recyclerView, null) {
                            val item = mListRecommendation.getOrNull(it)
                            fastScroller.updateBubbleText(item?.getBubbleText() ?: "")
                        }
                        recyclerView.onGlobalLayout {
                            fastScroller.setScrollTo(recyclerView.computeVerticalScrollOffset())
                        }

                        customView.findViewById<TextView>(R.id.viewWorld).setOnClickListener {
                            mPopupWindow?.dismiss()
                            overlayIcons("", false)
                        }
                        customView.findViewById<View>(R.id.close).setOnClickListener { mPopupWindow?.dismiss() }

                        val searchView = customView.findViewById<EditText>(R.id.searchKey)
                        searchView.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                                mListRecommendation.clear()
                                mListRecommendationOrigin.map{ it -> if (StringUtils.contains(it.keyWord, charSequence.toString())) {
                                    mListRecommendation.add(it)
                                }}
                                mRecommendationAdapter.notifyDataSetChanged()
                            }

                            override fun afterTextChanged(editable: Editable) {}
                        })

                        val rootView = window.findViewById<View>(android.R.id.content)
                        mPopupWindow = PopupWindow(customView, rootView.width, rootView.height, true)
                        mPopupWindow?.showAtLocation(view, Gravity.CENTER, 0, 0)
                    }
                }
            }
            R.id.find -> {
                mListPhotoMapItem?.let { listPhotoMapItem ->
                    if (listPhotoMapItem.size == 0) {
                        showAlertDialog(getString(R.string.maps_activity_message2))
                    } else {
                        val photoSearchIntent = Intent(this, PhotoSearchActivity::class.java)
                        startActivity(photoSearchIntent)
                    }
                }

            }
            R.id.settings -> {
                val settingIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingIntent)
            }
            R.id.timeline -> {
                mListPhotoMapItem?.let {
                    if (it.size == 0) {
                        showAlertDialog(getString(R.string.maps_activity_message2))
                    } else {
                        val timelineIntent = Intent(this@MapsActivity, TimelineActivity::class.java)
                        startActivity(timelineIntent)
                    }
                }
            }
            else -> {
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        EasyPhotoMapUtils.initWorkingDirectory()

        savedInstanceState?.let { 
            when (it.getBoolean(HAVE_CAMERA_POSITION, false)) {
                true -> {
                    mHaveCameraPosition = true
                    mSavedCameraZoom = it.getFloat(SAVED_CAMERA_ZOOM, 0F)
                    mSavedCameraLatitude = it.getDouble(SAVED_CAMERA_LATITUDE, 0.0)
                    mSavedCameraLongitude = it.getDouble(SAVED_CAMERA_LONGITUDE, 0.0)
                }
            }
        }
        
        // Obtain the SupportMapFragment and get notified when the mRecommendMap is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        floatingMenu?.setClosedOnTouchOutside(true)
        floatingMenu?.setOnMenuButtonClickListener { floatingMenu.toggle(true) }
        camera.setOnClickListener(mMenuClickListener)
        thumbnailViewer.setOnClickListener(mMenuClickListener)
        fileManager.setOnClickListener(mMenuClickListener)
        overlay.setOnClickListener(mMenuClickListener)
        find.setOnClickListener(mMenuClickListener)
        timeline.setOnClickListener(mMenuClickListener)
        settings.setOnClickListener(mMenuClickListener)
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
        isMapReady = true
        mMap.let { it ->
            it.setOnCameraChangeListener { cameraPosition ->
                if (cameraPosition.zoom > GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                    it.animateCamera(CameraUpdateFactory.zoomTo(GOOGLE_MAP_MAX_ZOOM_IN_VALUE))
                }
            }
        }

        when {
            intent.hasExtra(COLUMN_INFO) -> {
                val info = intent.getStringExtra(COLUMN_INFO)
                val imagePath = intent.getStringExtra(COLUMN_IMAGE_PATH)
                val latitude = intent.getDoubleExtra(COLUMN_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(COLUMN_LONGITUDE, 0.0)
                val date = intent.getStringExtra(COLUMN_DATE)
                val fOptions = MarkerOptions()
                val latLng = LatLng(latitude, longitude)
                fOptions.position(latLng)
                fOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                mMap.setInfoWindowAdapter(InfoWindow(info, imagePath, latitude, longitude, date))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15.0F), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        val marker = mMap.addMarker(fOptions)
                        mMap.setOnInfoWindowClickListener {
                            val imageViewIntent = Intent(this@MapsActivity, PopupImageActivity::class.java)
                            imageViewIntent.putExtra(COLUMN_IMAGE_PATH, imagePath)
                            startActivity(imageViewIntent)
                        }
                        marker.showInfoWindow()
                    }

                    override fun onCancel() {}
                })
                intent.removeExtra(COLUMN_INFO)
            }
            mHaveCameraPosition -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mSavedCameraLatitude, mSavedCameraLongitude), mSavedCameraZoom))
            else -> {
                handlePermission(PERMISSION_ACCESS_COARSE_LOCATION) {
                    if (it) {
                        handlePermission(PERMISSION_ACCESS_FINE_LOCATION) {
                            if (it) {
                                val location = getLocationWithGPSProvider()
                                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
                                location?.let {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
                                } ?: animateDefaultCamera() 
                            } else {
                                animateDefaultCamera()
                            }
                        }
                    } else {
                        animateDefaultCamera()
                    }
                }    
            }    
        } 
        migrateLegacyData()
    }

    override fun onResume() {
        super.onResume()
        parseMetadata()
    }
            
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (isMapReady) {
            outState?.let {
                it.putBoolean(HAVE_CAMERA_POSITION, true)
                it.putDouble(SAVED_CAMERA_LATITUDE, mMap.cameraPosition.target.latitude)
                it.putDouble(SAVED_CAMERA_LONGITUDE, mMap.cameraPosition.target.longitude)
                it.putFloat(SAVED_CAMERA_ZOOM, mMap.cameraPosition.zoom)
            }    
        }
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
    }
    
    private fun animateDefaultCamera() {
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(GOOGLE_MAP_DEFAULT_LATITUDE, GOOGLE_MAP_DEFAULT_LONGITUDE), GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(GOOGLE_MAP_DEFAULT_LATITUDE, GOOGLE_MAP_DEFAULT_LONGITUDE), GOOGLE_MAP_DEFAULT_ZOOM_VALUE))
    }

    private fun migrateLegacyData() {
        val legacyFile = File(LEGACY_PHOTO_DATA_PATH)
        if (legacyFile.exists()) {
            val listPhotoMapData = EasyPhotoMapUtils.readDataFile(LEGACY_PHOTO_DATA_PATH)
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
            legacyFile.renameTo(File(LEGACY_PHOTO_DATA_PATH + "_BAK"))
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
            max = when (applyFilter) {
                true -> PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_DATE, keyword).size
                false -> PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_INFO, keyword).size
            }
            setCanceledOnTouchOutside(false)
        }
        mProgressDialog?.show()
    }

    private fun getPhotoMarkerScale(): Double = when (config.photoMarkerScale) {
        SCALE_M4 -> 0.2
        SCALE_M3 -> 0.4
        SCALE_M2 -> 0.6
        SCALE_M1 -> 0.8
        SCALE_P1 -> 1.2
        SCALE_P2 -> 1.4
        else -> 1.0
    }

    private fun getPhotoMarkerMinimumCluster(): Int = when (config.photoMarkerMinimumCluster) {
        CLUSTER_L1 -> 5
        CLUSTER_L3 -> 50
        CLUSTER_L4 -> 100
        CLUSTER_L5 -> 200
        CLUSTER_L6 -> 500
        CLUSTER_L7 -> 1000
        else -> 10
    } 

    inner class OverlayThread(private val keyword: String, private val applyFilter: Boolean) : Thread() {

        override fun run() {
            super.run()
            val listTemp = when (applyFilter) {
                true -> {
                    PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_DATE, keyword)
//                    PhotoMapDbHelper.containsPhotoMapItemBy("dateWithoutTime", keyword)
                }
                false -> {
                    PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_INFO, keyword)
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
                    val textView = mProgressDialog?.findViewById<TextView>(android.R.id.message)
                    textView?.ellipsize = TextUtils.TruncateAt.MIDDLE
                    textView?.maxLines = 1
                }

                val options = MarkerOptions()
                val latLng = LatLng(item.latitude, item.longitude)
                options.position(latLng)
                val fileName = FilenameUtils.getName(item.imagePath)
                val tempBitmap: Bitmap = BitmapUtils.decodeFile(this@MapsActivity, WORKING_DIRECTORY + fileName + ".thumb")
                val bm = BitmapUtils.cropCenter(tempBitmap)
                val image = when (config.photoMarkerIcon) {
                    FILM -> {
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.addFilmFrame(this@MapsActivity, bm, getPhotoMarkerScale(), R.drawable.frame_03))
                    }
                    BASIC -> {
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.addBorder(this@MapsActivity, bm, getPhotoMarkerScale()))
                    }
                    FLOWER -> {
                        BitmapDescriptorFactory.fromBitmap(BitmapUtils.addFilmFrame(this@MapsActivity, bm, getPhotoMarkerScale(), R.drawable.frame_02))
                    }
                    else -> {
                        var px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
                        px = (px * getPhotoMarkerScale()).toInt()
                        val dotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(dotMarkerBitmap)
                        val shape = ContextCompat.getDrawable(this@MapsActivity, R.drawable.circle_marker)
                        shape?.setBounds(0, 0, dotMarkerBitmap.width, dotMarkerBitmap.height)
                        shape?.draw(canvas)
                        BitmapDescriptorFactory.fromBitmap(dotMarkerBitmap)
                    }
                }
                options.icon(image)
                mListMarkerOptions.add(options)
                mListPhotoEntity.add(item)
            }

            Handler(Looper.getMainLooper()).post {
                mProgressDialog?.dismiss()
                setUpCluster()
                val listForCluster = when(applyFilter) {
                    true -> {
                        PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_DATE, keyword)
                    }
                    false -> {
                        PhotoMapDbHelper.containsPhotoMapItemBy(COLUMN_INFO, keyword)
                    }
                }
                for (i in mListMarkerOptions.indices) {
                    val item = MyItem(mListMarkerOptions[i], listForCluster[i])
                    mClusterManager?.addItem(item)
                    mListLatLng.add(mListMarkerOptions[i].position)
                }

                mMap.let { map ->
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
                                    item.photoEntity.info,
                                    item.photoEntity.imagePath,
                                    item.photoEntity.latitude,
                                    item.photoEntity.longitude,
                                    item.photoEntity.date
                            ))
                            val fImagePath = item.photoEntity.imagePath
                            map.setOnInfoWindowClickListener {
                                val imageViewIntent = Intent(this@MapsActivity, PopupImageActivity::class.java)
                                imageViewIntent.putExtra(COLUMN_IMAGE_PATH, fImagePath)
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
            markerOptions?.icon(item?.markerOptions?.icon)
        }

        override fun shouldRenderAsCluster(cluster: Cluster<MyItem>): Boolean {
            return if (mapZoom > GOOGLE_MAP_MAX_ZOOM_IN_VALUE - 1) {
                false
            } else {
                cluster.size > getPhotoMarkerMinimumCluster()
            }

        }

        override fun onCameraChange(cameraPosition: CameraPosition) {
            if (cameraPosition.zoom > GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(GOOGLE_MAP_MAX_ZOOM_IN_VALUE))
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
            val imageView = view.findViewById<ImageView>(R.id.info1)
            //            imageView.setImageResource(imageId);
            val imgFile = File(imagePath)

            if (imgFile.exists()) {
                var orientation = 0
                val matrix = Matrix()
                try {
                    val exif = ExifInterface(imagePath)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
                    when (orientation) {
                        6 -> matrix.postRotate(90F)
                        3 -> matrix.postRotate(180F)
                        8 -> matrix.postRotate(270F)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                options.inSampleSize = 5
                var bitmap = BitmapUtils.decodeFile(this@MapsActivity, imgFile.absolutePath, options)
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
            (view.findViewById<TextView>(R.id.info2)).text = "lat: $lat lon: $lon \n $info"
            (view.findViewById<TextView>(R.id.info3)).text = date
            return view
        }
    }

    inner class Recommendation(internal var keyWord: String, internal var count: Int) {
        override fun toString(): String {
            val unit = getString(R.string.photo_map_item_count_unit)
            return "$keyWord [$count$unit]"
        }

        fun getBubbleText() = keyWord
    }
}
