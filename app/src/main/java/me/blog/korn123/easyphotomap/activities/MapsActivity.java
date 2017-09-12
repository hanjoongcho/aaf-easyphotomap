package me.blog.korn123.easyphotomap.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.camera.CameraActivity;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.helper.PhotoMapDbHelper;
import me.blog.korn123.easyphotomap.helper.PopupImageActivity;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.setting.SettingsActivity;
import me.blog.korn123.easyphotomap.thumbnail.ThumbnailExplorerActivity;
import me.blog.korn123.easyphotomap.timeline.TimelineActivity;
import me.blog.korn123.easyphotomap.utils.CommonUtils;
import me.blog.korn123.easyphotomap.utils.FontUtils;
import me.blog.korn123.easyphotomap.utils.GPSUtils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private ClusterManager<MyItem> clusterManager;
    private ArrayList<PhotoMapItem> listPhotoMapItem;
    private Map<String, Integer> recommendMap;
    private List<LatLng> listLatLng = new ArrayList<>();
    private List<PhotoMapItem> listPhotoEntity = new ArrayList<>();
    private List<Recommendation> listRecommendationOrigin = new ArrayList<>();
    private List<Recommendation> listRecommendation = new ArrayList<>();
    private List<MarkerOptions> listMarkerOptions = new ArrayList<>();
    private boolean enableDateFilter;
    private ArrayAdapter adapter;
    private ListView listView;

    @BindView(R.id.floatingMenu)
    public FloatingActionMenu mFloatingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        if (!new File(Constant.WORKING_DIRECTORY).exists()) {
            new File(Constant.WORKING_DIRECTORY).mkdirs();
        }

        // Obtain the SupportMapFragment and get notified when the recommendMap is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFloatingMenu.setClosedOnTouchOutside(true);
        mFloatingMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFloatingMenu.toggle(true);
            }
        });
    }

    /**
     * Manipulates the recommendMap once available.
     * This callback is triggered when the recommendMap is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera_camera_activity. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                    map.animateCamera(CameraUpdateFactory.zoomTo(Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE));
                }
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("info")) {
            String info = intent.getStringExtra("info");
            String imagePath = intent.getStringExtra("imagePath");
            double latitude = intent.getDoubleExtra("latitude", 0);
            double longitude = intent.getDoubleExtra("longitude", 0);
            String date = intent.getStringExtra("date");
            final MarkerOptions fOptions = new MarkerOptions();
            LatLng latLng = new LatLng(latitude, longitude);
            fOptions.position(latLng);

//            fOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            fOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
            final String fImagePath = imagePath;
            map.setInfoWindowAdapter(new InfoWindow(info, imagePath, latitude, longitude, date));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
//                  options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_myplaces));
                    Marker marker = map.addMarker(fOptions);
                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent imageViewIntent = new Intent(MapsActivity.this, PopupImageActivity.class);
                            imageViewIntent.putExtra("imagePath", fImagePath);
                            startActivity(imageViewIntent);
                        }
                    });
                    marker.showInfoWindow();
                }
                @Override
                public void onCancel() {
                }
            });
        } else {
            Location location = GPSUtils.getLocationWithGPSProvider(this);
            if (location == null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(Constant.GOOGLE_MAP_DEFAULT_LATITUDE, Constant.GOOGLE_MAP_DEFAULT_LONGITUDE),
                        Constant.GOOGLE_MAP_DEFAULT_ZOOM_VALUE)
                );
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), Constant.GOOGLE_MAP_DEFAULT_ZOOM_VALUE));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        parseMetadata();
    }

    public void parseMetadata() {
        if (recommendMap == null) {
            recommendMap = new HashMap<String, Integer>();
        } else {
            recommendMap.clear();
        }
        List<String> infoLines = new ArrayList();
        listPhotoMapItem = PhotoMapDbHelper.selectPhotoMapItemAll();
        Collections.sort(listPhotoMapItem);
    }

    @OnClick({R.id.setting, R.id.camera, R.id.explorer, R.id.overlay, R.id.find, R.id.folder, R.id.timeline})
    public void buttonClick(View view) {
        mFloatingMenu.close(false);
        switch (view.getId()) {
            case R.id.camera:
                if (CommonUtils.loadBooleanPreference(MapsActivity.this, "disable_info_popup")) {
                    Intent camera = new Intent(view.getContext(), CameraActivity.class);
                    startActivity(camera);
                } else {
                    final View infoView = getLayoutInflater().inflate(R.layout.main_camera_info, null);
                    TextView textView2 = (TextView)infoView.findViewById(R.id.textView2);
                    TextView textView3 = (TextView)infoView.findViewById(R.id.textView3);
                    popupWindow = new PopupWindow(infoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    CommonUtils.bindButtonEffect(textView2);
                    CommonUtils.bindButtonEffect(textView3);
                    textView2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                            Intent camera = new Intent(v.getContext(), CameraActivity.class);
                            startActivity(camera);
                        }
                    });
                    textView3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommonUtils.saveBooleanPreference(MapsActivity.this, "disable_info_popup", true);
                            popupWindow.dismiss();
                            Intent camera = new Intent(v.getContext(), CameraActivity.class);
                            startActivity(camera);
                        }
                    });
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                }
                break;
            case R.id.explorer:
                Intent intent = new Intent(view.getContext(), ThumbnailExplorerActivity.class);
                startActivity(intent);
                break;
            case R.id.overlay:
                enableDateFilter = CommonUtils.loadBooleanPreference(MapsActivity.this, "date_filter_setting");
//                Log.i("enableDateFilter", String.valueOf(enableDateFilter));
                map.clear();
                parseMetadata();
                if (listPhotoMapItem.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message2));
                    return;
                }

                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.main_marker_popup, null);
                listView = (ListView) customView.findViewById(R.id.listView);
                FontUtils.setChildViewTypeface((ViewGroup) customView);
                Set<String> set = new HashSet<>();
                if (enableDateFilter) {
                    for (PhotoMapItem item : listPhotoMapItem) {
                        String date = null;
                        if (item.date.contains("(")) {
                            date = item.date.substring(0, item.date.lastIndexOf("("));
                        } else {
                            date = item.date;
                        }
                        if (recommendMap.containsKey(date)) {
                            recommendMap.put(date, recommendMap.get(date) + 1);
                        } else {
                            recommendMap.put(date, 1);
                        }
                    }
                } else {
                    for (PhotoMapItem item : listPhotoMapItem) {
                        String[] arr = StringUtils.split(item.info, " ");
                        for (String keyword : arr) {
                            if (Pattern.matches("^(([0-9]{1,9})-([0-9]{1,9}))|(([0-9]{1,9}))$", keyword) || keyword.length() < 2)
                                continue;
                            if (recommendMap.containsKey(keyword)) {
                                recommendMap.put(keyword, recommendMap.get(keyword) + 1);
                            } else {
                                recommendMap.put(keyword, 1);
                            }
                        }
                    }
                }
                List<Map.Entry<String, Integer>> listOfSortEntry = CommonUtils.entriesSortedByValues(recommendMap);
                listRecommendationOrigin.clear();
                listRecommendation.clear();
                for (Map.Entry<String, Integer> entry : listOfSortEntry) {
                    listRecommendationOrigin.add(new Recommendation(entry.getKey(), entry.getValue()));
                }

                listRecommendation.addAll(listRecommendationOrigin);
                adapter = new ArrayAdapter<Recommendation>(this, R.layout.main_marker_popup_list_item, listRecommendation);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Recommendation recommendation = (Recommendation) parent.getAdapter().getItem(position);
                        popupWindow.dismiss();
                        overlayIcons(recommendation.keyWord, true);
                    }
                });
                Point point = CommonUtils.getDefaultDisplay(this);
                customView.findViewById(R.id.viewWorld).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                        overlayIcons("", false);
                    }
                });
                customView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

                SearchView searchView = (SearchView)customView.findViewById(R.id.searchKey);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        List<Recommendation> listTemp = new ArrayList<Recommendation>();
                        listRecommendation.clear();
                        for (Recommendation recommendation : listRecommendationOrigin) {
                            if (StringUtils.contains(recommendation.keyWord, newText)) {
                                listRecommendation.add(recommendation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        return false;
                    }
                });
                popupWindow = new PopupWindow(customView, (int)(point.x * 0.9), (int)((point.y - CommonUtils.dpToPixel(this, 25)) * 0.8), true);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                break;
            case R.id.find:
                if (listPhotoMapItem.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message2));
                    return;
                }
                Intent photoSearchIntent = new Intent(this, PhotoSearchActivity.class);
                startActivity(photoSearchIntent);
                break;
            case R.id.setting:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.folder:
                Intent fileExplorerIntent = new Intent(MapsActivity.this, FileExplorerActivity.class);
                startActivity(fileExplorerIntent);
                break;
            case R.id.timeline:
                if (listPhotoMapItem.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message2));
                    return;
                }
                Intent timelineIntent = new Intent(MapsActivity.this, TimelineActivity.class);
                startActivity(timelineIntent);
                break;
            default:
                break;
        }
    }

    private void setUpCluster() {
        // Initialize the manager with the context and the recommendMap.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        if (clusterManager == null) {
            clusterManager = new ClusterManager<MyItem>(this, map);
//            clusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MyItem>(new NonHierarchicalDistanceBasedAlgorithm<MyItem>()));
        } else {
            clusterManager.clearItems();
        }

        // Point the recommendMap's listeners at the listeners implemented by the cluster
        // manager.
//        getMap().setOnCameraChangeListener(clusterManager);
//        getMap().setOnMarkerClickListener(clusterManager);
    }

    Handler overlayHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof OverlayThread.ProgressInfo) {
                OverlayThread.ProgressInfo progressInfo = (OverlayThread.ProgressInfo)msg.obj;
                progressDialog.setProgress(progressInfo.getProgress());
                progressDialog.setMessage(progressInfo.getMessage());
            }
            return true;
        }
    });

    public void overlayIcons(String keyword, boolean applyFilter) {
        Thread overlayThread = new OverlayThread(keyword, applyFilter);
        overlayThread.start();
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("find " + keyword + "...");
        progressDialog.setMax(PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword).size());
        progressDialog.show();
    }

    class OverlayThread extends Thread {
        String keyword;
        boolean applyFilter;

        OverlayThread(String keyword, boolean applyFilter) {
            this.keyword = keyword;
            this.applyFilter = applyFilter;
        }

        class ProgressInfo {
            public ProgressInfo(int progress, String message, int totalItem) {
                this.progress = progress;
                this.message = message;
                this.totalItem = totalItem;
            }

            public int getProgress() {
                float percentage = ((float)progress/totalItem) * 100;
//                return (int)percentage;
                return progress;
            }

            public void setProgress(int progress) {
                this.progress = progress;
            }

            public String getMessage() {
//                return message;
                return StringUtils.abbreviate(message, 17);
            }

            public void setMessage(String message) {
                this.message = message;
            }

            int totalItem;
            int progress;
            String message;
        }

        @Override
        public void run() {
            ArrayList<PhotoMapItem> listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword);
            super.run();
            listLatLng.clear();
            listMarkerOptions.clear();
            listPhotoEntity.clear();
            int index = 0;
            for (PhotoMapItem item : listTemp) {
                Message progressMsg = overlayHandler.obtainMessage();
                index++;
                progressMsg.obj = new ProgressInfo(index, item.info, listTemp.size());
                overlayHandler.sendMessage(progressMsg);
//                if (enableDateFilter) {
//                    if (applyFilter && !item.date.contains(keyword) && !keyword.equals("대한민국")) continue;
//                } else {
//                    if (applyFilter && !item.info.contains(keyword)) continue;
//                }
                BitmapDescriptor image = null;
                MarkerOptions options = new MarkerOptions();
                LatLng latLng = new LatLng(item.latitude, item.longitude);
                options.position(latLng);
                String fileName = FilenameUtils.getName(item.imagePath);
                Bitmap bm = CommonUtils.decodeFile(MapsActivity.this, Constant.WORKING_DIRECTORY + fileName + ".thumb");
                if (CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_setting", "filmFrame").equals("filmFrame")) {
                    Point point = new Point(bm.getWidth(), bm.getHeight());
                    double fixedWidthHeight = Double.parseDouble(CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "0.6"));
                    Bitmap bm2 = CommonUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight);
                    image = BitmapDescriptorFactory.fromBitmap(CommonUtils.addFrame(MapsActivity.this, bm2, CommonUtils.dpToPixel(MapsActivity.this, 6), R.drawable.frame_03));
                } else if (CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_setting", "filmFrame").equals("basicFrame")) {
                    Point point = new Point(bm.getWidth(), bm.getHeight());
                    double fixedWidthHeight = Double.parseDouble(CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "0.6"));
                    Bitmap bm2 = CommonUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight);
                    image = BitmapDescriptorFactory.fromBitmap(CommonUtils.addWhiteBorder(bm2, CommonUtils.dpToPixel(MapsActivity.this, 3)));
                } else if (CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_setting", "filmFrame").equals("flowerFrame")) {
                    Point point = new Point(bm.getWidth(), bm.getHeight());
                    double fixedWidthHeight = Double.parseDouble(CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "0.6"));
                    Bitmap bm2 = CommonUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight);
                    image = BitmapDescriptorFactory.fromBitmap(CommonUtils.addFrame(MapsActivity.this, bm2, CommonUtils.dpToPixel(MapsActivity.this, 6), R.drawable.frame_02));
                } else {
                    int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
                    px = (int)(px * Double.parseDouble(CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "0.6")));
                    Bitmap mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(mDotMarkerBitmap);
                    Drawable shape = getResources().getDrawable(R.drawable.circle);
                    shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
                    shape.draw(canvas);
                    image = BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap);
                }
                options.icon(image);
                listMarkerOptions.add(options);
                listPhotoEntity.add(item);
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    setUpCluster();
                    ArrayList<PhotoMapItem> listTemp = PhotoMapDbHelper.containsPhotoMapItemBy("info", keyword);
                    for (int i = 0; i < listMarkerOptions.size(); i++) {
                        MyItem item = new MyItem(listMarkerOptions.get(i), listTemp.get(i));
                        clusterManager.addItem(item);
                        listLatLng.add(listMarkerOptions.get(i).getPosition());
                    }

                    map.setOnMarkerClickListener(clusterManager);
                    map.setOnCameraChangeListener(clusterManager);

                    MyClusterRenderer clusterRenderer = new MyClusterRenderer(MapsActivity.this, map, clusterManager);
                    clusterManager.setRenderer(clusterRenderer);

                    clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                        @Override
                        public boolean onClusterClick(Cluster<MyItem> cluster) {
                            map.setInfoWindowAdapter(null);
                            return false;
                        }
                    });

                    clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
                        @Override
                        public boolean onClusterItemClick(MyItem item) {
                            map.setInfoWindowAdapter(new InfoWindow(
                                    item.getPhotoEntity().info,
                                    item.getPhotoEntity().imagePath,
                                    item.getPhotoEntity().latitude,
                                    item.getPhotoEntity().longitude,
                                    item.getPhotoEntity().date
                            ));
                            final String fImagePath = item.getPhotoEntity().imagePath;
                            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    Intent imageViewIntent = new Intent(MapsActivity.this, PopupImageActivity.class);
                                    imageViewIntent.putExtra("imagePath", fImagePath);
                                    startActivity(imageViewIntent);
                                }
                            });
                            return false;
                        }
                    });

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng latLng: listLatLng) {
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }
            });
        }
    }

    class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private PhotoMapItem item;
        private MarkerOptions markerOptions;

        public MyItem(MarkerOptions markerOptions, PhotoMapItem photoMapItem) {
            mPosition = markerOptions.getPosition();
            this.markerOptions = markerOptions;
            this.item = photoMapItem;
        }

        public MarkerOptions getMarkerOptions() {
            return markerOptions;
        }

        public PhotoMapItem getPhotoEntity() {
            return item;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }

    class MyClusterRenderer extends DefaultClusterRenderer<MyItem> implements GoogleMap.OnCameraChangeListener {
        private IconGenerator mClusterIconGenerator = null;
        public float mapZoom;

        public MyClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
            mClusterIconGenerator = new IconGenerator(getApplicationContext());
        }

        @Override
        public Cluster<MyItem> getCluster(Marker marker) {
            return super.getCluster(marker);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);
//            mClusterIconGenerator.setContentPadding(100, 100, 100, 100);
//            mClusterIconGenerator.setColor(Color.BLACK);
//            Bitmap clusterIcon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(clusterIcon));
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item,
                                                   MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            markerOptions.icon(item.markerOptions.getIcon());
        }

        @Override
        protected void onClusterItemRendered(MyItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<MyItem> cluster) {
            if (mapZoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE - 1) {
                return false;
            } else {
                return cluster.getSize() > Integer.valueOf(CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_enable_minimum_cluster", "10"));
            }

        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if (cameraPosition.zoom > Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE) {
                map.animateCamera(CameraUpdateFactory.zoomTo(Constant.GOOGLE_MAP_MAX_ZOOM_IN_VALUE));
            }
            mapZoom = cameraPosition.zoom;
        }
    }

    class InfoWindow implements GoogleMap.InfoWindowAdapter {
        String info;
        String imagePath;
        String date;
        double latitude;
        double longitude;

        InfoWindow(String info, String imagePath, double latitude, double longitude, String date) {
            this.info = info;
            this.imagePath = imagePath;
            this.latitude = latitude;
            this.longitude = longitude;
            this.date = date;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            ContextThemeWrapper cw = new ContextThemeWrapper(getApplicationContext(), R.style.Transparent);
            LayoutInflater inflater = (LayoutInflater) cw.getSystemService(LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.main_info_window, null);
            ImageView imageView = (ImageView) (view.findViewById(R.id.info1));
//            imageView.setImageResource(imageId);
            File imgFile = new File(imagePath);

            if (imgFile.exists()) {
                int orientation = 0;
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imagePath);
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                    } else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                } catch (Exception e) {
                    AAFLogger.info("MapsActivity-getInfoContents INFO: " + e.getMessage(), getClass());
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inSampleSize = 5;
                Bitmap bitmap = CommonUtils.decodeFile(MapsActivity.this, imgFile.getAbsolutePath(), options);
                if (orientation > 1) {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
                }

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                Bitmap resized = null;
                if (orientation > 1) {
                    float downSampleHeight = (height / (float) width) * 200;
                    resized = Bitmap.createScaledBitmap(bitmap, 200, (int) downSampleHeight, true);
                } else {
                    float downSampleWidth = (width / (float) height) * 200;
                    resized = Bitmap.createScaledBitmap(bitmap, (int) downSampleWidth, 200, true);
                }
//            Log.i("getInfoContents o: ", String.valueOf(orientation));
//            Log.i("getInfoContents w: ", String.valueOf(resized.getWidth()));
//            Log.i("getInfoContents h: ", String.valueOf(resized.getHeight()));
                imageView.setImageBitmap(resized);
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery));
            }
            ((TextView) (view.findViewById(R.id.info2))).setText("lat: " + StringUtils.substring(String.valueOf(latitude), 0, 6) + " lon: " + StringUtils.substring(String.valueOf(longitude), 0, 6) + "\n" + info);
            ((TextView) (view.findViewById(R.id.info3))).setText(date);
            return view;
        }
    }

    private class Recommendation {
        String keyWord;
        int count;

        public Recommendation(String keyWord, int count) {
            this.keyWord = keyWord;
            this.count = count;
        }

        @Override
        public String toString() {
            return keyWord + " [" + count +"건]";
        }
    }

}
