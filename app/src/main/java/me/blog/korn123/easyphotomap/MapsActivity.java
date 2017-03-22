package me.blog.korn123.easyphotomap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.blog.korn123.easyphotomap.camera.CameraActivity;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.file.FileExplorerActivity;
import me.blog.korn123.easyphotomap.helper.PopupImageActivity;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.PhotoEntity;
import me.blog.korn123.easyphotomap.search.PhotoSearchActivity;
import me.blog.korn123.easyphotomap.setting.SettingsActivity;
import me.blog.korn123.easyphotomap.thumbnail.ThumbnailExplorerActivity;
import me.blog.korn123.easyphotomap.timeline.TimelineActivity;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    final float MAX_ZOOM = 18.0f;
    private GoogleMap mMap;
    private FloatingActionMenu menuDown;

    public Map<String, PhotoEntity> entityMap = new HashMap<String, PhotoEntity>();
    ArrayList<PhotoEntity> entities;
    Map<String, Integer> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.main_maps_activity);
        ButterKnife.bind(this);
        menuDown = (FloatingActionMenu) findViewById(R.id.menu_down);
        if (!new File(Constant.WORKING_DIRECTORY).exists()) {
            new File(Constant.WORKING_DIRECTORY).mkdirs();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menuDown.setClosedOnTouchOutside(true);
        menuDown.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuDown.toggle(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        parseMetadata();
    }

    private void bindButtonEffect() {
        ImageButton camera = (ImageButton) findViewById(R.id.camera);
        ImageButton explorer = (ImageButton) findViewById(R.id.explorer);
        ImageButton overlay = (ImageButton) findViewById(R.id.overlay);
        ImageButton find = (ImageButton) findViewById(R.id.find);
        ImageButton setting = (ImageButton) findViewById(R.id.setting);
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
                    view.setBackgroundColor(0x5fef1014);
                } else if (motionEvent.getAction() == motionEvent.ACTION_UP) {
                    view.setBackgroundColor(0x00ffffff);
                }
                return false;
            }
        };
        camera.setOnTouchListener(onTouchListener);
        explorer.setOnTouchListener(onTouchListener);
        overlay.setOnTouchListener(onTouchListener);
        find.setOnTouchListener(onTouchListener);
        setting.setOnTouchListener(onTouchListener);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera_camera_activity. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (cameraPosition.zoom > MAX_ZOOM) {
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(MAX_ZOOM));
                }
//                latitude.setText(String.valueOf(cameraPosition.target.latitude));
//                longitude.setText(String.valueOf(cameraPosition.target.longitude));
//                try {
//                    List<Address> listAddress = CommonUtils.getFromLocation(MapsActivity.this, cameraPosition.target.latitude, cameraPosition.target.longitude, 1, 0);
//                    address.setText(CommonUtils.fullAddress(listAddress.get(0)));
//                } catch (Exception e) {
//                    AAFLogger.error("MapsActivity-MapsActivity ERROR: " + e.getMessage(), getClass());
//                    e.printStackTrace();
//                }
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
            mMap.setInfoWindowAdapter(new InfoWindow(info, imagePath, latitude, longitude, date));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
//                  options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_myplaces));
                    Marker marker = mMap.addMarker(fOptions);
//                  Toast.makeText(getApplicationContext(), marker.isInfoWindowShown() + "", Toast.LENGTH_SHORT).show();
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
//                            CommonUtils.showAlertDialog(MapsActivity.this, "", MapsActivity.this, fImagePath);
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
//                        Toast.makeText(getApplicationContext(), marker.isInfoWindowShown() + "", Toast.LENGTH_SHORT).show();
        } else {
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.3425, 127.29194444444444), 7.0f));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.4372046, 127.8896971), 7.0f));
        }
    }

    public void parseMetadata() {
        if (entities == null) {
            entities = new ArrayList<>();
        } else {
            entities.clear();
        }
        if (map == null) {
            map = new HashMap<String, Integer>();
        } else {
            map.clear();
        }
        List<String> infoLines = new ArrayList();
        try {
            InputStream is = new FileInputStream(new File(Constant.PHOTO_DATA_PATH));
            infoLines = IOUtils.readLines(is, "UTF-8");
            for (String infoLine : infoLines) {
                String[] infoArray = StringUtils.split(infoLine, "|");
                PhotoEntity entity = new PhotoEntity();
                entity.longitude = Double.parseDouble(infoArray[3]);
                entity.latitude = Double.parseDouble(infoArray[2]);
                entity.imagePath = infoArray[0];
                entity.info = infoArray[1];
                entity.date = infoArray[4];
                entities.add(entity);
            }
            Collections.sort(entities);
        } catch (Exception e) {
            AAFLogger.info("MapsActivity-parseMetadata INFO: " + e.getMessage(), getClass());
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
//            imageView.setRotation(90);
//            imageView.setMaxWidth(100);
//            imageView.setMaxHeight(100);
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


    List<Recommendation> listRecommendationOrigin = new ArrayList<>();
    List<Recommendation> listRecommendation = new ArrayList<>();
    ArrayAdapter adapter;
    @OnClick({R.id.setting, R.id.camera, R.id.explorer, R.id.overlay, R.id.find, R.id.folder, R.id.timeline})
    public void buttonClick(View view) {
        menuDown.close(false);
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        switch (view.getId()) {
            case R.id.camera:
//                CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message1));
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
                mMap.clear();
                parseMetadata();
                if (entities.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message2));
                    return;
                }

                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View customView = inflater.inflate(R.layout.main_marker_popup, null);

                Set<String> set = new HashSet<>();
                if (enableDateFilter) {
                    for (PhotoEntity imageEntity : entities) {
                        String date = null;
                        if (imageEntity.date.contains("(")) {
                            date = imageEntity.date.substring(0, imageEntity.date.lastIndexOf("("));
                        } else {
                            date = imageEntity.date;
                        }
                        if (map.containsKey(date)) {
                            map.put(date, map.get(date) + 1);
                        } else {
                            map.put(date, 1);
                        }
                    }
                } else {
                    for (PhotoEntity imageEntity : entities) {
                        String[] arr = StringUtils.split(imageEntity.info, " ");
                        for (String keyword : arr) {
                            if (Pattern.matches("^(([0-9]{1,9})-([0-9]{1,9}))|(([0-9]{1,9}))$", keyword) || keyword.length() < 2)
                                continue;
                            if (map.containsKey(keyword)) {
                                map.put(keyword, map.get(keyword) + 1);
                            } else {
                                map.put(keyword, 1);
                            }
                        }
                    }
                }
                List<Map.Entry<String, Integer>> listOfSortEntry = CommonUtils.entriesSortedByValues(map);
                listRecommendationOrigin.clear();
                listRecommendation.clear();
                for (Map.Entry<String, Integer> entry : listOfSortEntry) {
                    listRecommendationOrigin.add(new Recommendation(entry.getKey(), entry.getValue()));
                }

                listRecommendation.addAll(listRecommendationOrigin);
                adapter = new ArrayAdapter<Recommendation>(this, R.layout.main_marker_popup_list_item, listRecommendation);
                listView = (ListView) customView.findViewById(R.id.listView);
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
//                customView.findViewById(R.id.viewKorea).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        popupWindow.dismiss();
//                        overlayIcons("대한민국", true);
//                    }
//                });
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
//                popupWindow = new PopupWindow(customView, (int) (point.x * 0.9), (int) (point.y * 0.6), true);
                popupWindow = new PopupWindow(customView, (int)(point.x * 0.9), (int)((point.y - CommonUtils.dpToPixel(this, 25)) * 0.8), true);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                break;
            case R.id.find:
                if (entities.size() < 1) {
                    CommonUtils.showAlertDialog(this, getString(R.string.maps_activity_message2));
                    return;
                }
                Intent photoSearchIntent = new Intent(this, PhotoSearchActivity.class);
                startActivity(photoSearchIntent);
                break;
            case R.id.setting:
//                Intent settingIntent = new Intent(this, EPMPreferenceActivity.class);
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.folder:
                Intent fileExplorerIntent = new Intent(MapsActivity.this, FileExplorerActivity.class);
                startActivity(fileExplorerIntent);
                break;
            case R.id.timeline:
                if (entities.size() < 1) {
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

    private ListView listView;
    private PopupWindow popupWindow;
    boolean enableDateFilter;

    class MyItem implements ClusterItem {
        private final LatLng mPosition;
        private PhotoEntity photoEntity;
        private MarkerOptions markerOptions;

        public MyItem(MarkerOptions markerOptions, PhotoEntity photoEntity) {
            mPosition = markerOptions.getPosition();
            this.markerOptions = markerOptions;
            this.photoEntity = photoEntity;
        }

        public MarkerOptions getMarkerOptions() {
            return markerOptions;
        }

        public PhotoEntity getPhotoEntity() {
            return photoEntity;
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
//            AAFLogger.info("MapsActivity.MyClusterRenderer-shouldRenderAsCluster INFO: " + mapZoom, getClass());
            if (mapZoom > 17) {
                return false;
            } else {
                return cluster.getSize() > Integer.valueOf(CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_enable_minimum_cluster", "10"));
            }

        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if (cameraPosition.zoom > MAX_ZOOM) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(MAX_ZOOM));
            }
            mapZoom = cameraPosition.zoom;
        }
    }

    // Declare a variable for the cluster manager.
    private ClusterManager<MyItem> mClusterManager;
    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        if (mClusterManager == null) {
            mClusterManager = new ClusterManager<MyItem>(this, mMap);
//            mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MyItem>(new NonHierarchicalDistanceBasedAlgorithm<MyItem>()));
        } else {
            mClusterManager.clearItems();
        }

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
//        getMap().setOnCameraChangeListener(mClusterManager);
//        getMap().setOnMarkerClickListener(mClusterManager);
    }

    Handler overlayHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if (msg.obj instanceof String) {
                if (StringUtils.equals((String) msg.obj, "update completed")) {
                    progressDialog.dismiss();
                    setUpClusterer();

                    for (int i = 0; i < listMarkerOptions.size(); i++) {
//                        Marker marker = mMap.addMarker(listMarkerOptions.get(i));
                        MyItem item = new MyItem(listMarkerOptions.get(i), listPhotoEntity.get(i));
                        mClusterManager.addItem(item);
                        listLatLng.add(listMarkerOptions.get(i).getPosition());
//                        listMarker.add(marker);
//                        entityMap.put(marker.getId(), listPhotoEntity.get(i));
                    }

                    mMap.setOnMarkerClickListener(mClusterManager);
                    mMap.setOnCameraChangeListener(mClusterManager);

                    MyClusterRenderer clusterRenderer = new MyClusterRenderer(MapsActivity.this, mMap, mClusterManager);
                    mClusterManager.setRenderer(clusterRenderer);

                    mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
                                @Override
                                public boolean onClusterClick(Cluster<MyItem> cluster) {
//                                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                                    mMap.setInfoWindowAdapter(null);
                                    return false;
                                }
                    });

                    mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
                                @Override
                                public boolean onClusterItemClick(MyItem item) {
                                    mMap.setInfoWindowAdapter(new InfoWindow(
                                            item.getPhotoEntity().info,
                                            item.getPhotoEntity().imagePath,
                                            item.getPhotoEntity().latitude,
                                            item.getPhotoEntity().longitude,
                                            item.getPhotoEntity().date
                                    ));
                                    final String fImagePath = item.getPhotoEntity().imagePath;
                                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
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

//                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                        @Override
//                        public boolean onMarkerClick(Marker marker) {
////                Toast.makeText(getApplicationContext(), marker.getId(), Toast.LENGTH_SHORT).show();
//                            mMap.setInfoWindowAdapter(new InfoWindow(
//                                    entityMap.get(marker.getId()).info,
//                                    entityMap.get(marker.getId()).imagePath,
//                                    entityMap.get(marker.getId()).latitude,
//                                    entityMap.get(marker.getId()).longitude,
//                                    entityMap.get(marker.getId()).date
//                            ));
//                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//                                @Override
//                                public void onInfoWindowClick(Marker marker) {
////                        Log.i("onClick", "setOnInfoWindowClickListener");
////                                    CommonUtils.showAlertDialog(MapsActivity.this, "", MapsActivity.this, entityMap.get(marker.getId()).imagePath);
//                                    Intent imageViewIntent = new Intent(MapsActivity.this, PopupImageActivity.class);
//                                    imageViewIntent.putExtra("imagePath", entityMap.get(marker.getId()).imagePath);
//                                    startActivity(imageViewIntent);
//                                }
//                            });
//                            return false;
//                        }
//                    });
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                    for (Marker marker : listMarker) {
//                        builder.include(marker.getPosition());
//                    }
                    for (LatLng latLng: listLatLng) {
                        builder.include(latLng);
                    }
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }
            } else if (msg.obj instanceof OverlayThread.ProgressInfo) {
                OverlayThread.ProgressInfo progressInfo = (OverlayThread.ProgressInfo)msg.obj;
//                progressDialog.setMessage(progressInfo.getMessage());
                progressDialog.setProgress(progressInfo.getProgress());
                progressDialog.setMessage(progressInfo.getMessage());
            }
            return true;
        }
    });

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
            super.run();
//            listMarker.clear();
            listLatLng.clear();
            listMarkerOptions.clear();
            listPhotoEntity.clear();
            int index = 0;
            for (PhotoEntity imageEntity : entities) {
                Message progressMsg = overlayHandler.obtainMessage();
                index++;
                progressMsg.obj = new ProgressInfo(index, imageEntity.info, entities.size());
                overlayHandler.sendMessage(progressMsg);
                if (enableDateFilter) {
                    if (applyFilter && !imageEntity.date.contains(keyword) && !keyword.equals("대한민국")) continue;
                } else {
                    if (applyFilter && !imageEntity.info.contains(keyword)) continue;
                }
                BitmapDescriptor image = null;
                MarkerOptions options = new MarkerOptions();
                LatLng latLng = new LatLng(imageEntity.latitude, imageEntity.longitude);
                options.position(latLng);
                String fileName = FilenameUtils.getName(imageEntity.imagePath);
                Bitmap bm = CommonUtils.decodeFile(MapsActivity.this, Constant.WORKING_DIRECTORY + fileName + ".thumb");
                if (CommonUtils.loadStringPreference(MapsActivity.this, "photo_marker_setting", "filmFrame").equals("filmFrame")) {
//                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_myplaces));
//                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
//                    String photoSize = CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "1.0");
                    Point point = new Point(bm.getWidth(), bm.getHeight());
                    double fixedWidthHeight = Double.parseDouble(CommonUtils.loadStringPreference(MapsActivity.this, "photo_size_setting", "0.6"));
                    Bitmap bm2 = CommonUtils.createScaledBitmap(bm, point, fixedWidthHeight, fixedWidthHeight);
//                    Log.i("bm2", bm2.getWidth() + "x" + bm2.getHeight());
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
                listPhotoEntity.add(imageEntity);
            }
            Message completeMsg = overlayHandler.obtainMessage();
            completeMsg.obj = "update completed";
            overlayHandler.sendMessage(completeMsg);
        }
    }

    ProgressDialog progressDialog;
//    List<Marker> listMarker = new ArrayList<>();
    List<LatLng> listLatLng = new ArrayList<>();
    List<PhotoEntity> listPhotoEntity = new ArrayList<>();
    List<MarkerOptions> listMarkerOptions = new ArrayList<>();
    public void overlayIcons(String keyword, boolean applyFilter) {
        Thread overlayThread = new OverlayThread(keyword, applyFilter);
        overlayThread.start();
//        progressDialog = ProgressDialog.show(MapsActivity.this, "find " + keyword + "...", "");
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("find " + keyword + "...");
        progressDialog.setMax(entities.size());
        progressDialog.show();
    }
}
