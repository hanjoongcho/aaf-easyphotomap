package me.blog.korn123.easyphotomap.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.models.ThumbnailItem;

/**
 * Created by CHO HANJOONG on 2016-07-21.
 */
public class CommonUtils {

//    public final static SimpleDateFormat DATE_TIME_PATTERN = new SimpleDateFormat("yyyy-MM-dd(EEE) HH:mm", Locale.getDefault());
    public final static SimpleDateFormat DATE_TIME_PATTERN = new SimpleDateFormat("yyyy년 MM월 dd일(E) HH:mm", Locale.KOREA);

    private static final int MAX_RETRY = 5;
    public static List<Address> getFromLocation(Context context, double latitude, double longitude, int maxResults, int retryCount) throws Exception {
        latitude = Double.parseDouble(String.format("%.6f", latitude));
        longitude = Double.parseDouble(String.format("%.7f", longitude));
        List<Address> listAddress = null;
        Locale locale = Locale.getDefault();
        Geocoder geocoder = new Geocoder(context, locale);
        try {
            listAddress = geocoder.getFromLocation(latitude, longitude, maxResults);
        } catch (Exception e) {
            if (retryCount < MAX_RETRY) {
                return getFromLocation(context, latitude, longitude, maxResults, ++retryCount);
            }
            throw new Exception(e.getMessage());
        }
        return listAddress;
    }

    public static List<Address> getFromLocationName(Context context, String locationName, int maxResults, int retryCount) throws Exception {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(locationName, maxResults);
        } catch (Exception e) {
            if (retryCount < MAX_RETRY) {
                return getFromLocationName(context, locationName, maxResults, ++retryCount);
            }
            throw new Exception(e.getMessage());
        }
        return listAddress;
    }

    public static <K,V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());
        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );
        return sortedEntries;
    }

    public static void bindButtonEffect(View targetView) {
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
        targetView.setOnTouchListener(onTouchListener);
    }

    public static boolean loadBooleanPreference(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enableDateFilter = preferences.getBoolean(key, false);
        return enableDateFilter;
    }

    public static void saveBooleanPreference(Context context, String key, boolean isEnable) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(key, isEnable);
        edit.commit();
    }

    public static int loadIntPreference(Context context, String key, int defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int value = preferences.getInt(key, defaultValue);
        return value;
    }

    public static String loadStringPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String value = preferences.getString(key, defaultValue);
        return value;
    }

    public static void saveStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static float loadFontScaleFactor(Activity activity) {
        SharedPreferences mPreferences = activity.getPreferences(activity.MODE_PRIVATE);
        float fontScaleFactor = mPreferences.getFloat("fontScaleFactor", 1.0f);
        return fontScaleFactor;
    }

    public static void saveFontScaleFactor(Activity activity, float scaleFactor) {
        SharedPreferences mPreferences = activity.getPreferences(activity.MODE_PRIVATE);
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putFloat("fontScaleFactor", scaleFactor);
        edit.commit();
    }

    public static ThumbnailItem fetchThumbnailBy(Context context, String imageId) {
        ThumbnailItem photo = null;
        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
        String[] mSelectionArgs = {imageId};
        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection,
                MediaStore.Images.Thumbnails.IMAGE_ID + " = ?",
                mSelectionArgs,
                MediaStore.Images.Thumbnails.DATA + " desc");
        ArrayList<ThumbnailItem> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
        int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (imageCursor.moveToFirst()) {
                photo = new ThumbnailItem(imageCursor.getString(idColumnIndex), null, imageCursor.getString(dataColumnIndex));
        }
        imageCursor.close();
        return photo;
    }

    public static List<ThumbnailItem> fetchAllThumbnail(Context context) {
        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection,
                null,
                null,
                MediaStore.Images.Thumbnails.DATA + " desc");
        ArrayList<ThumbnailItem> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
        int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (imageCursor.moveToFirst()) {
            do {
                String filePath = imageCursor.getString(dataColumnIndex);
                String imageId = imageCursor.getString(idColumnIndex);

//                Uri thumbnailUri = uriToThumbnail(context, imageId);
//                Uri imageUri = Uri.parse(filePath);
//                Log.i("fetchAllImages", imageUri.toString());
                // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                ThumbnailItem photo = new ThumbnailItem(imageId, null, filePath);
                result.add(photo);
            } while(imageCursor.moveToNext());
        } else {
            // imageCursor가 비었습니다.
        }
        imageCursor.close();
        return result;
    }

    public static List<ThumbnailItem> fetchAllImages(Context context) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection, // DATA, _ID를 출력
                null,       // 모든 개체 출력
                null,
                MediaStore.Images.Media.DATA + " asc");

        ArrayList<ThumbnailItem> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
        int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (imageCursor.moveToFirst()) {
            do {
                String filePath = imageCursor.getString(dataColumnIndex);
                String imageId = imageCursor.getString(idColumnIndex);

//                Uri thumbnailUri = uriToThumbnail(context, imageId);
//                Uri imageUri = Uri.parse(filePath);
//                Log.i("fetchAllImages", imageUri.toString());
                // 원본 이미지와 썸네일 이미지의 uri를 모두 담을 수 있는 클래스를 선언합니다.
                ThumbnailItem photo = new ThumbnailItem(imageId, filePath, null);
                result.add(photo);
            } while(imageCursor.moveToNext());
        } else {
            // imageCursor가 비었습니다.
        }
        imageCursor.close();
        return result;
    }

    public static String getOrignImagepath(Context context, String imageId) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Media.DATA};
        ContentResolver contentResolver = context.getContentResolver();

        // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Images.Media._ID + "=?",
                new String[]{imageId},
                null);
        if (cursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (cursor.moveToFirst()) {
            int thumbnailColumnIndex = cursor.getColumnIndex(projection[0]);
            String path = cursor.getString(thumbnailColumnIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    public static GpsDirectory getGPSDirectory(String filePath) {
        GpsDirectory gpsDirectory = null;
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(new File(filePath));
            gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        } catch (JpegProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gpsDirectory;
    }

    public static int generateThumbnail(Context context, String imageId) {
        int count = 0;
        String[] projection = { MediaStore.Images.Thumbnails.DATA };
        ContentResolver contentResolver = context.getContentResolver();
        Cursor thumbnailCursor = contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, // DATA를 출력
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{imageId},
                null);
        if (thumbnailCursor == null) {
        } else if (thumbnailCursor.moveToFirst()) {
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
            thumbnailCursor.close();
            count = 1;
        }
        return count;
    }

    public static String uriToThumbnail(Context context, String imageId) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Thumbnails.DATA };
        ContentResolver contentResolver = context.getContentResolver();

        // 원본 이미지의 _ID가 매개변수 imageId인 썸네일을 출력
        Cursor thumbnailCursor = contentResolver.query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 썸네일 컨텐트 테이블
                projection, // DATA를 출력
                MediaStore.Images.Thumbnails.IMAGE_ID + "=?", // IMAGE_ID는 원본 이미지의 _ID를 나타냅니다.
                new String[]{imageId},
                null);
        if (thumbnailCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (thumbnailCursor.moveToFirst()) {
            int thumbnailColumnIndex = thumbnailCursor.getColumnIndex(projection[0]);

            String thumbnailPath = thumbnailCursor.getString(thumbnailColumnIndex);
            thumbnailCursor.close();
            return thumbnailPath;
        } else {
            // thumbnailCursor가 비었습니다.
            // 이는 이미지 파일이 있더라도 썸네일이 존재하지 않을 수 있기 때문입니다.
            // 보통 이미지가 생성된 지 얼마 되지 않았을 때 그렇습니다.
            // 썸네일이 존재하지 않을 때에는 아래와 같이 썸네일을 생성하도록 요청합니다
            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, Long.parseLong(imageId), MediaStore.Images.Thumbnails.MINI_KIND, null);
            thumbnailCursor.close();
            return uriToThumbnail(context, imageId);
        }
        return null;
    }

    public static boolean isMatchLine(String dataPath, String lineString) {
        boolean isMatch = false;
        try {
            InputStream is = new FileInputStream(new File(dataPath));
            List<String> listLine = IOUtils.readLines(is, "UTF-8");
            for (String line : listLine) {
//                Log.i("isMatchLine", lineString.length() + "," + lineString);
//                Log.i("isMatchLine", line.length() + "," + line);
                if (StringUtils.equals(line.trim(), lineString.trim())) {
                    isMatch = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isMatch;
    }

    public static String fullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getCountryName() != null) sb.append(address.getCountryName()).append(" ");
        if (address.getAdminArea() != null) sb.append(address.getAdminArea()).append(" ");
        if (address.getLocality() != null) sb.append(address.getLocality()).append(" ");
        if (address.getSubLocality() != null) sb.append(address.getSubLocality()).append(" ");
        if (address.getThoroughfare() != null) sb.append(address.getThoroughfare()).append(" ");
        if (address.getFeatureName() != null) sb.append(address.getFeatureName()).append(" ");
        return sb.toString();
    }

    public static void writeDataFile(String data, String targetPath, boolean append) {
        if (append) {
            appendDataFile(data, targetPath);
        } else {
            writeDataFile(data, targetPath);
        }
    }

    public static void appendDataFile(String data, String targetPath) {
        Writer writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            writer = new FileWriterWithEncoding(new File(targetPath), "UTF-8", true);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(data);
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(writer);
        }
    }

    public static List<String> readDataFile(String targetPath) {
        InputStream inputStream = null;
        List<String> listData = null;
        try {
            inputStream = FileUtils.openInputStream(new File(targetPath));
            listData = IOUtils.readLines(inputStream, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listData;
    }

    public static void writeDataFile(String data, String targetPath) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(targetPath));
            IOUtils.write(data, outputStream, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public static int dpToPixel(Context context, float dp) {
        return dpToPixel(context, dp, 0);
    }

    public static int dpToPixel(Context context, float dp, int policy) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        int pixel = 0;
        switch (policy) {
            case 0:
                pixel = (int) px;
                break;
            case 1:
                pixel = Math.round(px);
                break;
        }
        return pixel;
    }

    public static int getDisplayOrientation(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int orientation = display.getOrientation();
        return orientation;
    }

    public static Point getDefaultDisplay(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static PopupWindow openPopupWindow(Activity activity, View anchorView) {
        PopupWindow popupWindow;
        View popupView = activity.getLayoutInflater().inflate(R.layout.activity_photo_search, null);
        popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(100);
        popupWindow.showAsDropDown(anchorView);
        return popupWindow;
    }

}
