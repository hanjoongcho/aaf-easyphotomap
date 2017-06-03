package me.blog.korn123.easyphotomap.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import me.blog.korn123.easyphotomap.file.FileExplorerActivity;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.thumbnail.ThumbnailEntity;
import me.blog.korn123.easyphotomap.thumbnail.ThumbnailExplorerActivity;

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
        long start = System.currentTimeMillis();
        try {
//            AAFLogger.info("CommonUtils-getFromLocation INFO: " + latitude + ", " + longitude, CommonUtils.class);
            listAddress = geocoder.getFromLocation(latitude, longitude, maxResults);
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-getFromLocation ERROR: [" + locale + "/retry count " + retryCount + "] " + e.getMessage(), CommonUtils.class);
            if (retryCount < MAX_RETRY) {
                return getFromLocation(context, latitude, longitude, maxResults, ++retryCount);
            }
            throw new Exception(e.getMessage());
        }
//        AAFLogger.info("CommonUtils-getFromLocation INFO: " + latitude + ", " + longitude + ", " + (System.currentTimeMillis() - start), CommonUtils.class);
        return listAddress;
    }

    public static List<Address> getFromLocationName(Context context, String locationName, int maxResults, int retryCount) throws Exception {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(locationName, maxResults);
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-getFromLocationName ERROR: [retry count " + retryCount + "] " + e.getMessage(), CommonUtils.class);
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

    public static ThumbnailEntity fetchThumbnailBy(Context context, String imageId) {
        ThumbnailEntity photo = null;
        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
        String[] mSelectionArgs = {imageId};
        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection,
                MediaStore.Images.Thumbnails.IMAGE_ID + " = ?",
                mSelectionArgs,
                MediaStore.Images.Thumbnails.DATA + " desc");
        ArrayList<ThumbnailEntity> result = new ArrayList<>(imageCursor.getCount());
        int dataColumnIndex = imageCursor.getColumnIndex(projection[0]);
        int idColumnIndex = imageCursor.getColumnIndex(projection[1]);

        if (imageCursor == null) {
            // Error 발생
            // 적절하게 handling 해주세요
        } else if (imageCursor.moveToFirst()) {
                photo = new ThumbnailEntity(imageCursor.getString(idColumnIndex), null, imageCursor.getString(dataColumnIndex));
        }
        imageCursor.close();
        return photo;
    }

    public static List<ThumbnailEntity> fetchAllThumbnail(Context context) {
        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.IMAGE_ID };
        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection,
                null,
                null,
                MediaStore.Images.Thumbnails.DATA + " desc");
        ArrayList<ThumbnailEntity> result = new ArrayList<>(imageCursor.getCount());
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
                ThumbnailEntity photo = new ThumbnailEntity(imageId, null, filePath);
                result.add(photo);
            } while(imageCursor.moveToNext());
        } else {
            // imageCursor가 비었습니다.
        }
        imageCursor.close();
        return result;
    }

    public static List<ThumbnailEntity> fetchAllImages(Context context) {
        // DATA는 이미지 파일의 스트림 데이터 경로를 나타냅니다.
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // 이미지 컨텐트 테이블
                projection, // DATA, _ID를 출력
                null,       // 모든 개체 출력
                null,
                MediaStore.Images.Media.DATA + " asc");

        ArrayList<ThumbnailEntity> result = new ArrayList<>(imageCursor.getCount());
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
                ThumbnailEntity photo = new ThumbnailEntity(imageId, filePath, null);
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

    public static Bitmap addWhiteBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
//        canvas.drawColor(Color.parseColor("#5FEF1014"));
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    public static Bitmap addFrame(Activity activity, Bitmap bmp, int borderSize, int id) {
        Bitmap bmpWithFrame = Bitmap.createBitmap(bmp.getWidth() + borderSize, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithFrame);
        Bitmap temp = BitmapFactory.decodeResource(activity.getResources(), id);
        Bitmap frame = Bitmap.createScaledBitmap(temp, bmp.getWidth() + borderSize, bmp.getHeight() + borderSize * 2, false);
        canvas.drawBitmap(frame, 0, 0, null);
        canvas.drawBitmap(bmp, borderSize / 2, borderSize, null);
        return bmpWithFrame;
    }

    public static void showAlertDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = CommonUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = createScaledBitmap(originBitmap, getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath, ThumbnailExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final ThumbnailExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = CommonUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = createScaledBitmap(originBitmap, getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, FileExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final FileExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context, String message, Activity activity, String imagePath, FileExplorerActivity.PositiveListener listener) {
        long start = System.currentTimeMillis();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final FileExplorerActivity.PositiveListener fLisntener = listener;
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fLisntener.register();
                return;
            }
        };
        DialogInterface.OnClickListener NegativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton(context.getString(R.string.confirm), positiveListener);
        builder.setNegativeButton(context.getString(R.string.cancel), NegativeListener);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 5;
        Bitmap originBitmap = CommonUtils.decodeFile(activity, imagePath, options);
        Bitmap scaledBitmap = createScaledBitmap(originBitmap, getDefaultDisplay(activity));
        ImageView thumbView = new ImageView(activity);
        thumbView.setImageBitmap(scaledBitmap);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thumbView);
        builder.setView(layout);
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showAlertDialog(Context context,
                                       String message,
                                       DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("취소", negativeListener);
        builder.setPositiveButton("확인", positiveListener);
        AlertDialog alert = builder.create();
        alert.show();
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
            AAFLogger.info("CommonUtils-isMatchLine INFO: " + e.getMessage(), CommonUtils.class);
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

    public static boolean createScaledBitmap(String srcPath, String destPath, int fixedWidthHeight) {
        long start = System.currentTimeMillis();
        boolean result = true;
        OutputStream outputStream = null;
//        if (new File(Constant.THUMBNAIL_DIRECTORY + FilenameUtils.getName(srcPath)).exists()) {
//            srcPath = Constant.THUMBNAIL_DIRECTORY + FilenameUtils.getName(srcPath);
//        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 20;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float downSampleHeight = (height / (float)width) * fixedWidthHeight;
            float downSampleWidth  = (width / (float)height) * fixedWidthHeight;
            Bitmap thumbNail = null;
            if (width > height) {
                thumbNail = Bitmap.createScaledBitmap(bitmap, fixedWidthHeight, (int)downSampleHeight, false);
            } else {
                thumbNail = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, fixedWidthHeight, false);
            }
            outputStream = new FileOutputStream(destPath);
//            Log.i("createScaledBitmap0", String.valueOf(thumbNail.getWidth()));
//            Log.i("createScaledBitmap1", String.valueOf(System.currentTimeMillis() - start));
            thumbNail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-createScaledBitmap INFO: " + e.getMessage(), CommonUtils.class);
            result = false;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
//        Log.i("createScaledBitmap2", String.valueOf(System.currentTimeMillis() - start));
        return result;
    }

    public static Bitmap createScaledBitmap(String srcPath, int fixedWidth) {
        boolean result = true;
        OutputStream outputStream = null;
        Bitmap thumbNail = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 10;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float downSampleHeight = (height / (float)width) * fixedWidth;
            thumbNail = Bitmap.createScaledBitmap(bitmap, fixedWidth, (int)downSampleHeight, false);
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-createScaledBitmap INFO: " + e.getMessage(), CommonUtils.class);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return thumbNail;
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, Point point) {
        return createScaledBitmap(bitmap, point, 0.8, 0.5);
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, Point point, double scaleFactorX, double scaleFactorY) {
        Bitmap downscaledBitmap = null;
        double fixedWidth  = point.x * scaleFactorX;
        double fixedHeight = point.y * scaleFactorY;
        try {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            double downSampleWidth = (width / (float)height) * fixedHeight;
            double downSampleHeight = (height / (float)width) * fixedWidth;
            if (width > height && point.x < point.y) { // 가로이미지 & 세로보기 화면에서는 width값에 맞춰 고정함
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)fixedWidth, (int)downSampleHeight, false);
            } else if (width > height && point.x > point.y) { // 가로이미지 & 가로보기 화면에서는 height값에 맞춰 고정함
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else if (width < height) {
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else if (width == height) {
                downscaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)downSampleWidth, (int)fixedHeight, false);
            } else {
                downscaledBitmap = bitmap;
            }
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-createScaledBitmap INFO: " + e.getMessage(), CommonUtils.class);
        }
        return downscaledBitmap;
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
            AAFLogger.info("CommonUtils-appendDataFile INFO: " + e.getMessage(), CommonUtils.class);
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
            AAFLogger.info("CommonUtils-readDataFile INFO: " + e.getMessage(), CommonUtils.class);
        }
        return listData;
    }

    public static void writeDataFile(String data, String targetPath) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(new File(targetPath));
            IOUtils.write(data, outputStream, "UTF-8");
        } catch (Exception e) {
            AAFLogger.info("CommonUtils-writeDataFile INFO: " + e.getMessage(), CommonUtils.class);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public static void makeToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int dpToPixel(Context context, int dp) {
        return dpToPixel(context, dp, 0);
    }

    public static int dpToPixel(Context context, int dp, int policy) {
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

    public static Bitmap decodeFile(Activity activity, String imagePath) {
        return decodeFile(activity, imagePath, null);
    }

    public static Bitmap decodeFile(Activity activity, String imagePath, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (imagePath != null && new File(imagePath).exists()) {
            if (options == null) {
                bitmap = BitmapFactory.decodeFile(imagePath);
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath, options);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), android.R.drawable.ic_menu_gallery);
        }
        return bitmap;
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
        View popupView = activity.getLayoutInflater().inflate(R.layout.search_common_search_activity, null);
        popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(100);
        popupWindow.showAsDropDown(anchorView);
        return popupWindow;
    }

//    public static ListPopupWindow openListPopupWindow(Activity activity, Context context, View anchorView) {
//        final ListPopupWindow listPopupWindow;
//        listPopupWindow = new ListPopupWindow(context);
//        listPopupWindow.setWidth(100);
//        listPopupWindow.setHeight(100);
////        listPopupWindow.setVerticalOffset(CommonUtils.dpToPixel(this, 45));
//        listPopupWindow.setAnchorView(anchorView);
////        PhotoEntityAdapter adapter = new PhotoEntityAdapter(this, R.layout.search_photo_search_activity_list_item, entities);
//        listPopupWindow.setAdapter(null);
//        listPopupWindow.setModal(true);
//        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                PhotoEntity imageEntity = (PhotoEntity) parent.getAdapter().getItem(position);
//                listPopupWindow.dismiss();
//            }
//        });
//        listPopupWindow.show();
//        return listPopupWindow;


//    List<String> list = new ArrayList<>();
//    list.add("b");
//    list.add("b");
//    list.add("b");
//    list.add("b");
//    list.add("b");
//
//    ImageButton button = (ImageButton)findViewById(R.id.overlay);
//    ListPopupWindow mList = new ListPopupWindow(this);
//    Display display = getWindowManager().getDefaultDisplay();
//    Point size = new Point();
//    display.getSize(size);
//    int width = size.x;
//    int height = size.y;
//    mList.setWidth((int)(width * 0.7));
//    mList.setHeight(height / 2);
//    mList.setAnchorView(button);
//    ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
//    mList.setAdapter(adapter);
//    mList.setModal(true);
//
//    mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            String uuid = null;
//        }
//    });
//    mList.show();
//    mList.getListView().setDivider(this.getResources().getDrawable(R.color.color1));
//    mList.getListView().setDividerHeight(2);



//    }

//            (GMT-12:00) Etc/GMT+12
//            (GMT-11:00) Etc/GMT+11
//            (GMT-11:00) Pacific/Midway
//            (GMT-11:00) Pacific/Niue
//            (GMT-11:00) Pacific/Pago_Pago
//            (GMT-11:00) Pacific/Samoa
//            (GMT-11:00) US/Samoa
//            (GMT-10:00) America/Adak
//            (GMT-10:00) America/Atka
//            (GMT-10:00) Etc/GMT+10
//            (GMT-10:00) HST
//            (GMT-10:00) Pacific/Honolulu
//            (GMT-10:00) Pacific/Johnston
//            (GMT-10:00) Pacific/Rarotonga
//            (GMT-10:00) Pacific/Tahiti
//            (GMT-10:00) SystemV/HST10
//            (GMT-10:00) US/Aleutian
//            (GMT-10:00) US/Hawaii
//            (GMT-9:30) Pacific/Marquesas
//            (GMT-9:00) AST
//    (GMT-9:00) America/Anchorage
//            (GMT-9:00) America/Juneau
//            (GMT-9:00) America/Nome
//            (GMT-9:00) America/Sitka
//            (GMT-9:00) America/Yakutat
//            (GMT-9:00) Etc/GMT+9
//            (GMT-9:00) Pacific/Gambier
//            (GMT-9:00) SystemV/YST9
//            (GMT-9:00) SystemV/YST9YDT
//            (GMT-9:00) US/Alaska
//            (GMT-8:00) America/Dawson
//            (GMT-8:00) America/Ensenada
//            (GMT-8:00) America/Los_Angeles
//            (GMT-8:00) America/Metlakatla
//            (GMT-8:00) America/Santa_Isabel
//            (GMT-8:00) America/Tijuana
//            (GMT-8:00) America/Vancouver
//            (GMT-8:00) America/Whitehorse
//            (GMT-8:00) Canada/Pacific
//            (GMT-8:00) Canada/Yukon
//            (GMT-8:00) Etc/GMT+8
//            (GMT-8:00) Mexico/BajaNorte
//            (GMT-8:00) PST
//    (GMT-8:00) PST8PDT
//            (GMT-8:00) Pacific/Pitcairn
//            (GMT-8:00) SystemV/PST8
//            (GMT-8:00) SystemV/PST8PDT
//            (GMT-8:00) US/Pacific
//            (GMT-8:00) US/Pacific-New
//            (GMT-7:00) America/Boise
//            (GMT-7:00) America/Cambridge_Bay
//            (GMT-7:00) America/Chihuahua
//            (GMT-7:00) America/Creston
//            (GMT-7:00) America/Dawson_Creek
//            (GMT-7:00) America/Denver
//            (GMT-7:00) America/Edmonton
//            (GMT-7:00) America/Hermosillo
//            (GMT-7:00) America/Inuvik
//            (GMT-7:00) America/Mazatlan
//            (GMT-7:00) America/Ojinaga
//            (GMT-7:00) America/Phoenix
//            (GMT-7:00) America/Shiprock
//            (GMT-7:00) America/Yellowknife
//            (GMT-7:00) Canada/Mountain
//            (GMT-7:00) Etc/GMT+7
//            (GMT-7:00) MST
//            (GMT-7:00) MST7MDT
//    (GMT-7:00) Mexico/BajaSur
//            (GMT-7:00) Navajo
//    (GMT-7:00) PNT
//            (GMT-7:00) SystemV/MST7
//            (GMT-7:00) SystemV/MST7MDT
//            (GMT-7:00) US/Arizona
//            (GMT-7:00) US/Mountain
//            (GMT-6:00) America/Bahia_Banderas
//            (GMT-6:00) America/Belize
//            (GMT-6:00) America/Cancun
//            (GMT-6:00) America/Chicago
//            (GMT-6:00) America/Costa_Rica
//            (GMT-6:00) America/El_Salvador
//            (GMT-6:00) America/Guatemala
//            (GMT-6:00) America/Indiana/Knox
//            (GMT-6:00) America/Indiana/Tell_City
//            (GMT-6:00) America/Knox_IN
//            (GMT-6:00) America/Managua
//            (GMT-6:00) America/Matamoros
//            (GMT-6:00) America/Menominee
//            (GMT-6:00) America/Merida
//            (GMT-6:00) America/Mexico_City
//            (GMT-6:00) America/Monterrey
//            (GMT-6:00) America/North_Dakota/Beulah
//            (GMT-6:00) America/North_Dakota/Center
//            (GMT-6:00) America/North_Dakota/New_Salem
//            (GMT-6:00) America/Rainy_River
//            (GMT-6:00) America/Rankin_Inlet
//            (GMT-6:00) America/Regina
//            (GMT-6:00) America/Resolute
//            (GMT-6:00) America/Swift_Current
//            (GMT-6:00) America/Tegucigalpa
//            (GMT-6:00) America/Winnipeg
//            (GMT-6:00) CST
//    (GMT-6:00) CST6CDT
//            (GMT-6:00) Canada/Central
//            (GMT-6:00) Canada/East-Saskatchewan
//            (GMT-6:00) Canada/Saskatchewan
//            (GMT-6:00) Chile/EasterIsland
//            (GMT-6:00) Etc/GMT+6
//            (GMT-6:00) Mexico/General
//            (GMT-6:00) Pacific/Easter
//            (GMT-6:00) Pacific/Galapagos
//            (GMT-6:00) SystemV/CST6
//            (GMT-6:00) SystemV/CST6CDT
//            (GMT-6:00) US/Central
//            (GMT-6:00) US/Indiana-Starke
//            (GMT-5:00) America/Atikokan
//            (GMT-5:00) America/Bogota
//            (GMT-5:00) America/Cayman
//            (GMT-5:00) America/Coral_Harbour
//            (GMT-5:00) America/Detroit
//            (GMT-5:00) America/Eirunepe
//            (GMT-5:00) America/Fort_Wayne
//            (GMT-5:00) America/Grand_Turk
//            (GMT-5:00) America/Guayaquil
//            (GMT-5:00) America/Havana
//            (GMT-5:00) America/Indiana/Indianapolis
//            (GMT-5:00) America/Indiana/Marengo
//            (GMT-5:00) America/Indiana/Petersburg
//            (GMT-5:00) America/Indiana/Vevay
//            (GMT-5:00) America/Indiana/Vincennes
//            (GMT-5:00) America/Indiana/Winamac
//            (GMT-5:00) America/Indianapolis
//            (GMT-5:00) America/Iqaluit
//            (GMT-5:00) America/Jamaica
//            (GMT-5:00) America/Kentucky/Louisville
//            (GMT-5:00) America/Kentucky/Monticello
//            (GMT-5:00) America/Lima
//            (GMT-5:00) America/Louisville
//            (GMT-5:00) America/Montreal
//            (GMT-5:00) America/Nassau
//            (GMT-5:00) America/New_York
//            (GMT-5:00) America/Nipigon
//            (GMT-5:00) America/Panama
//            (GMT-5:00) America/Pangnirtung
//            (GMT-5:00) America/Port-au-Prince
//            (GMT-5:00) America/Porto_Acre
//            (GMT-5:00) America/Rio_Branco
//            (GMT-5:00) America/Thunder_Bay
//            (GMT-5:00) America/Toronto
//            (GMT-5:00) Brazil/Acre
//            (GMT-5:00) Canada/Eastern
//            (GMT-5:00) Cuba
//    (GMT-5:00) EST
//            (GMT-5:00) EST5EDT
//    (GMT-5:00) Etc/GMT+5
//            (GMT-5:00) IET
//            (GMT-5:00) Jamaica
//    (GMT-5:00) SystemV/EST5
//            (GMT-5:00) SystemV/EST5EDT
//            (GMT-5:00) US/East-Indiana
//            (GMT-5:00) US/Eastern
//            (GMT-5:00) US/Michigan
//            (GMT-4:30) America/Caracas
//            (GMT-4:00) America/Anguilla
//            (GMT-4:00) America/Antigua
//            (GMT-4:00) America/Aruba
//            (GMT-4:00) America/Asuncion
//            (GMT-4:00) America/Barbados
//            (GMT-4:00) America/Blanc-Sablon
//            (GMT-4:00) America/Boa_Vista
//            (GMT-4:00) America/Campo_Grande
//            (GMT-4:00) America/Cuiaba
//            (GMT-4:00) America/Curacao
//            (GMT-4:00) America/Dominica
//            (GMT-4:00) America/Glace_Bay
//            (GMT-4:00) America/Goose_Bay
//            (GMT-4:00) America/Grenada
//            (GMT-4:00) America/Guadeloupe
//            (GMT-4:00) America/Guyana
//            (GMT-4:00) America/Halifax
//            (GMT-4:00) America/Kralendijk
//            (GMT-4:00) America/La_Paz
//            (GMT-4:00) America/Lower_Princes
//            (GMT-4:00) America/Manaus
//            (GMT-4:00) America/Marigot
//            (GMT-4:00) America/Martinique
//            (GMT-4:00) America/Moncton
//            (GMT-4:00) America/Montserrat
//            (GMT-4:00) America/Port_of_Spain
//            (GMT-4:00) America/Porto_Velho
//            (GMT-4:00) America/Puerto_Rico
//            (GMT-4:00) America/Santiago
//            (GMT-4:00) America/Santo_Domingo
//            (GMT-4:00) America/St_Barthelemy
//            (GMT-4:00) America/St_Kitts
//            (GMT-4:00) America/St_Lucia
//            (GMT-4:00) America/St_Thomas
//            (GMT-4:00) America/St_Vincent
//            (GMT-4:00) America/Thule
//            (GMT-4:00) America/Tortola
//            (GMT-4:00) America/Virgin
//            (GMT-4:00) Antarctica/Palmer
//            (GMT-4:00) Atlantic/Bermuda
//            (GMT-4:00) Brazil/West
//            (GMT-4:00) Canada/Atlantic
//            (GMT-4:00) Chile/Continental
//            (GMT-4:00) Etc/GMT+4
//            (GMT-4:00) PRT
//            (GMT-4:00) SystemV/AST4
//            (GMT-4:00) SystemV/AST4ADT
//            (GMT-3:30) America/St_Johns
//            (GMT-3:30) CNT
//    (GMT-3:30) Canada/Newfoundland
//            (GMT-3:00) AGT
//    (GMT-3:00) America/Araguaina
//            (GMT-3:00) America/Argentina/Buenos_Aires
//            (GMT-3:00) America/Argentina/Catamarca
//            (GMT-3:00) America/Argentina/ComodRivadavia
//            (GMT-3:00) America/Argentina/Cordoba
//            (GMT-3:00) America/Argentina/Jujuy
//            (GMT-3:00) America/Argentina/La_Rioja
//            (GMT-3:00) America/Argentina/Mendoza
//            (GMT-3:00) America/Argentina/Rio_Gallegos
//            (GMT-3:00) America/Argentina/Salta
//            (GMT-3:00) America/Argentina/San_Juan
//            (GMT-3:00) America/Argentina/San_Luis
//            (GMT-3:00) America/Argentina/Tucuman
//            (GMT-3:00) America/Argentina/Ushuaia
//            (GMT-3:00) America/Bahia
//            (GMT-3:00) America/Belem
//            (GMT-3:00) America/Buenos_Aires
//            (GMT-3:00) America/Catamarca
//            (GMT-3:00) America/Cayenne
//            (GMT-3:00) America/Cordoba
//            (GMT-3:00) America/Fortaleza
//            (GMT-3:00) America/Godthab
//            (GMT-3:00) America/Jujuy
//            (GMT-3:00) America/Maceio
//            (GMT-3:00) America/Mendoza
//            (GMT-3:00) America/Miquelon
//            (GMT-3:00) America/Montevideo
//            (GMT-3:00) America/Paramaribo
//            (GMT-3:00) America/Recife
//            (GMT-3:00) America/Rosario
//            (GMT-3:00) America/Santarem
//            (GMT-3:00) America/Sao_Paulo
//            (GMT-3:00) Antarctica/Rothera
//            (GMT-3:00) Atlantic/Stanley
//            (GMT-3:00) BET
//    (GMT-3:00) Brazil/East
//            (GMT-3:00) Etc/GMT+3
//            (GMT-2:00) America/Noronha
//            (GMT-2:00) Atlantic/South_Georgia
//            (GMT-2:00) Brazil/DeNoronha
//            (GMT-2:00) Etc/GMT+2
//            (GMT-1:00) America/Scoresbysund
//            (GMT-1:00) Atlantic/Azores
//            (GMT-1:00) Atlantic/Cape_Verde
//            (GMT-1:00) Etc/GMT+1
//            (GMT0:00) Africa/Abidjan
//            (GMT0:00) Africa/Accra
//            (GMT0:00) Africa/Bamako
//            (GMT0:00) Africa/Banjul
//            (GMT0:00) Africa/Bissau
//            (GMT0:00) Africa/Casablanca
//            (GMT0:00) Africa/Conakry
//            (GMT0:00) Africa/Dakar
//            (GMT0:00) Africa/El_Aaiun
//            (GMT0:00) Africa/Freetown
//            (GMT0:00) Africa/Lome
//            (GMT0:00) Africa/Monrovia
//            (GMT0:00) Africa/Nouakchott
//            (GMT0:00) Africa/Ouagadougou
//            (GMT0:00) Africa/Sao_Tome
//            (GMT0:00) Africa/Timbuktu
//            (GMT0:00) America/Danmarkshavn
//            (GMT0:00) Antarctica/Troll
//            (GMT0:00) Atlantic/Canary
//            (GMT0:00) Atlantic/Faeroe
//            (GMT0:00) Atlantic/Faroe
//            (GMT0:00) Atlantic/Madeira
//            (GMT0:00) Atlantic/Reykjavik
//            (GMT0:00) Atlantic/St_Helena
//            (GMT0:00) Eire
//    (GMT0:00) Etc/GMT
//            (GMT0:00) Etc/GMT+0
//            (GMT0:00) Etc/GMT-0
//            (GMT0:00) Etc/GMT0
//            (GMT0:00) Etc/Greenwich
//            (GMT0:00) Etc/UCT
//            (GMT0:00) Etc/UTC
//            (GMT0:00) Etc/Universal
//            (GMT0:00) Etc/Zulu
//            (GMT0:00) Europe/Belfast
//            (GMT0:00) Europe/Dublin
//            (GMT0:00) Europe/Guernsey
//            (GMT0:00) Europe/Isle_of_Man
//            (GMT0:00) Europe/Jersey
//            (GMT0:00) Europe/Lisbon
//            (GMT0:00) Europe/London
//            (GMT0:00) GB
//    (GMT0:00) GB-Eire
//            (GMT0:00) GMT
//    (GMT0:00) GMT0
//            (GMT0:00) Greenwich
//    (GMT0:00) Iceland
//            (GMT0:00) Portugal
//    (GMT0:00) UCT
//            (GMT0:00) UTC
//    (GMT0:00) Universal
//            (GMT0:00) WET
//    (GMT0:00) Zulu
//            (GMT+1:00) Africa/Algiers
//            (GMT+1:00) Africa/Bangui
//            (GMT+1:00) Africa/Brazzaville
//            (GMT+1:00) Africa/Ceuta
//            (GMT+1:00) Africa/Douala
//            (GMT+1:00) Africa/Kinshasa
//            (GMT+1:00) Africa/Lagos
//            (GMT+1:00) Africa/Libreville
//            (GMT+1:00) Africa/Luanda
//            (GMT+1:00) Africa/Malabo
//            (GMT+1:00) Africa/Ndjamena
//            (GMT+1:00) Africa/Niamey
//            (GMT+1:00) Africa/Porto-Novo
//            (GMT+1:00) Africa/Tunis
//            (GMT+1:00) Africa/Windhoek
//            (GMT+1:00) Arctic/Longyearbyen
//            (GMT+1:00) Atlantic/Jan_Mayen
//            (GMT+1:00) CET
//    (GMT+1:00) ECT
//            (GMT+1:00) Etc/GMT-1
//            (GMT+1:00) Europe/Amsterdam
//            (GMT+1:00) Europe/Andorra
//            (GMT+1:00) Europe/Belgrade
//            (GMT+1:00) Europe/Berlin
//            (GMT+1:00) Europe/Bratislava
//            (GMT+1:00) Europe/Brussels
//            (GMT+1:00) Europe/Budapest
//            (GMT+1:00) Europe/Busingen
//            (GMT+1:00) Europe/Copenhagen
//            (GMT+1:00) Europe/Gibraltar
//            (GMT+1:00) Europe/Ljubljana
//            (GMT+1:00) Europe/Luxembourg
//            (GMT+1:00) Europe/Madrid
//            (GMT+1:00) Europe/Malta
//            (GMT+1:00) Europe/Monaco
//            (GMT+1:00) Europe/Oslo
//            (GMT+1:00) Europe/Paris
//            (GMT+1:00) Europe/Podgorica
//            (GMT+1:00) Europe/Prague
//            (GMT+1:00) Europe/Rome
//            (GMT+1:00) Europe/San_Marino
//            (GMT+1:00) Europe/Sarajevo
//            (GMT+1:00) Europe/Skopje
//            (GMT+1:00) Europe/Stockholm
//            (GMT+1:00) Europe/Tirane
//            (GMT+1:00) Europe/Vaduz
//            (GMT+1:00) Europe/Vatican
//            (GMT+1:00) Europe/Vienna
//            (GMT+1:00) Europe/Warsaw
//            (GMT+1:00) Europe/Zagreb
//            (GMT+1:00) Europe/Zurich
//            (GMT+1:00) MET
//    (GMT+1:00) Poland
//            (GMT+2:00) ART
//    (GMT+2:00) Africa/Blantyre
//            (GMT+2:00) Africa/Bujumbura
//            (GMT+2:00) Africa/Cairo
//            (GMT+2:00) Africa/Gaborone
//            (GMT+2:00) Africa/Harare
//            (GMT+2:00) Africa/Johannesburg
//            (GMT+2:00) Africa/Kigali
//            (GMT+2:00) Africa/Lubumbashi
//            (GMT+2:00) Africa/Lusaka
//            (GMT+2:00) Africa/Maputo
//            (GMT+2:00) Africa/Maseru
//            (GMT+2:00) Africa/Mbabane
//            (GMT+2:00) Africa/Tripoli
//            (GMT+2:00) Asia/Amman
//            (GMT+2:00) Asia/Beirut
//            (GMT+2:00) Asia/Damascus
//            (GMT+2:00) Asia/Gaza
//            (GMT+2:00) Asia/Hebron
//            (GMT+2:00) Asia/Istanbul
//            (GMT+2:00) Asia/Jerusalem
//            (GMT+2:00) Asia/Nicosia
//            (GMT+2:00) Asia/Tel_Aviv
//            (GMT+2:00) CAT
//    (GMT+2:00) EET
//            (GMT+2:00) Egypt
//    (GMT+2:00) Etc/GMT-2
//            (GMT+2:00) Europe/Athens
//            (GMT+2:00) Europe/Bucharest
//            (GMT+2:00) Europe/Chisinau
//            (GMT+2:00) Europe/Helsinki
//            (GMT+2:00) Europe/Istanbul
//            (GMT+2:00) Europe/Kiev
//            (GMT+2:00) Europe/Mariehamn
//            (GMT+2:00) Europe/Nicosia
//            (GMT+2:00) Europe/Riga
//            (GMT+2:00) Europe/Sofia
//            (GMT+2:00) Europe/Tallinn
//            (GMT+2:00) Europe/Tiraspol
//            (GMT+2:00) Europe/Uzhgorod
//            (GMT+2:00) Europe/Vilnius
//            (GMT+2:00) Europe/Zaporozhye
//            (GMT+2:00) Israel
//    (GMT+2:00) Libya
//            (GMT+2:00) Turkey
//    (GMT+3:00) Africa/Addis_Ababa
//            (GMT+3:00) Africa/Asmara
//            (GMT+3:00) Africa/Asmera
//            (GMT+3:00) Africa/Dar_es_Salaam
//            (GMT+3:00) Africa/Djibouti
//            (GMT+3:00) Africa/Juba
//            (GMT+3:00) Africa/Kampala
//            (GMT+3:00) Africa/Khartoum
//            (GMT+3:00) Africa/Mogadishu
//            (GMT+3:00) Africa/Nairobi
//            (GMT+3:00) Antarctica/Syowa
//            (GMT+3:00) Asia/Aden
//            (GMT+3:00) Asia/Baghdad
//            (GMT+3:00) Asia/Bahrain
//            (GMT+3:00) Asia/Kuwait
//            (GMT+3:00) Asia/Qatar
//            (GMT+3:00) Asia/Riyadh
//            (GMT+3:00) EAT
//    (GMT+3:00) Etc/GMT-3
//            (GMT+3:00) Europe/Kaliningrad
//            (GMT+3:00) Europe/Minsk
//            (GMT+3:00) Indian/Antananarivo
//            (GMT+3:00) Indian/Comoro
//            (GMT+3:00) Indian/Mayotte
//            (GMT+3:07) Asia/Riyadh87
//            (GMT+3:07) Asia/Riyadh88
//            (GMT+3:07) Asia/Riyadh89
//            (GMT+3:07) Mideast/Riyadh87
//            (GMT+3:07) Mideast/Riyadh88
//            (GMT+3:07) Mideast/Riyadh89
//            (GMT+3:30) Asia/Tehran
//            (GMT+3:30) Iran
//    (GMT+4:00) Asia/Baku
//            (GMT+4:00) Asia/Dubai
//            (GMT+4:00) Asia/Muscat
//            (GMT+4:00) Asia/Tbilisi
//            (GMT+4:00) Asia/Yerevan
//            (GMT+4:00) Etc/GMT-4
//            (GMT+4:00) Europe/Moscow
//            (GMT+4:00) Europe/Samara
//            (GMT+4:00) Europe/Simferopol
//            (GMT+4:00) Europe/Volgograd
//            (GMT+4:00) Indian/Mahe
//            (GMT+4:00) Indian/Mauritius
//            (GMT+4:00) Indian/Reunion
//            (GMT+4:00) NET
//    (GMT+4:00) W-SU
//            (GMT+4:30) Asia/Kabul
//            (GMT+5:00) Antarctica/Mawson
//            (GMT+5:00) Asia/Aqtau
//            (GMT+5:00) Asia/Aqtobe
//            (GMT+5:00) Asia/Ashgabat
//            (GMT+5:00) Asia/Ashkhabad
//            (GMT+5:00) Asia/Dushanbe
//            (GMT+5:00) Asia/Karachi
//            (GMT+5:00) Asia/Oral
//            (GMT+5:00) Asia/Samarkand
//            (GMT+5:00) Asia/Tashkent
//            (GMT+5:00) Etc/GMT-5
//            (GMT+5:00) Indian/Kerguelen
//            (GMT+5:00) Indian/Maldives
//            (GMT+5:00) PLT
//    (GMT+5:30) Asia/Calcutta
//            (GMT+5:30) Asia/Colombo
//            (GMT+5:30) Asia/Kolkata
//            (GMT+5:30) IST
//    (GMT+5:45) Asia/Kathmandu
//            (GMT+5:45) Asia/Katmandu
//            (GMT+6:00) Antarctica/Vostok
//            (GMT+6:00) Asia/Almaty
//            (GMT+6:00) Asia/Bishkek
//            (GMT+6:00) Asia/Dacca
//            (GMT+6:00) Asia/Dhaka
//            (GMT+6:00) Asia/Qyzylorda
//            (GMT+6:00) Asia/Thimbu
//            (GMT+6:00) Asia/Thimphu
//            (GMT+6:00) Asia/Yekaterinburg
//            (GMT+6:00) BST
//    (GMT+6:00) Etc/GMT-6
//            (GMT+6:00) Indian/Chagos
//            (GMT+6:30) Asia/Rangoon
//            (GMT+6:30) Indian/Cocos
//            (GMT+7:00) Antarctica/Davis
//            (GMT+7:00) Asia/Bangkok
//            (GMT+7:00) Asia/Ho_Chi_Minh
//            (GMT+7:00) Asia/Hovd
//            (GMT+7:00) Asia/Jakarta
//            (GMT+7:00) Asia/Novokuznetsk
//            (GMT+7:00) Asia/Novosibirsk
//            (GMT+7:00) Asia/Omsk
//            (GMT+7:00) Asia/Phnom_Penh
//            (GMT+7:00) Asia/Pontianak
//            (GMT+7:00) Asia/Saigon
//            (GMT+7:00) Asia/Vientiane
//            (GMT+7:00) Etc/GMT-7
//            (GMT+7:00) Indian/Christmas
//            (GMT+7:00) VST
//    (GMT+8:00) Antarctica/Casey
//            (GMT+8:00) Asia/Brunei
//            (GMT+8:00) Asia/Choibalsan
//            (GMT+8:00) Asia/Chongqing
//            (GMT+8:00) Asia/Chungking
//            (GMT+8:00) Asia/Harbin
//            (GMT+8:00) Asia/Hong_Kong
//            (GMT+8:00) Asia/Kashgar
//            (GMT+8:00) Asia/Krasnoyarsk
//            (GMT+8:00) Asia/Kuala_Lumpur
//            (GMT+8:00) Asia/Kuching
//            (GMT+8:00) Asia/Macao
//            (GMT+8:00) Asia/Macau
//            (GMT+8:00) Asia/Makassar
//            (GMT+8:00) Asia/Manila
//            (GMT+8:00) Asia/Shanghai
//            (GMT+8:00) Asia/Singapore
//            (GMT+8:00) Asia/Taipei
//            (GMT+8:00) Asia/Ujung_Pandang
//            (GMT+8:00) Asia/Ulaanbaatar
//            (GMT+8:00) Asia/Ulan_Bator
//            (GMT+8:00) Asia/Urumqi
//            (GMT+8:00) Australia/Perth
//            (GMT+8:00) Australia/West
//            (GMT+8:00) CTT
//    (GMT+8:00) Etc/GMT-8
//            (GMT+8:00) Hongkong
//            (GMT+8:00) PRC
//    (GMT+8:00) Singapore
//            (GMT+8:45) Australia/Eucla
//            (GMT+9:00) Asia/Dili
//            (GMT+9:00) Asia/Irkutsk
//            (GMT+9:00) Asia/Jayapura
//            (GMT+9:00) Asia/Pyongyang
//            (GMT+9:00) Asia/Seoul
//            (GMT+9:00) Asia/Tokyo
//            (GMT+9:00) Etc/GMT-9
//            (GMT+9:00) JST
//            (GMT+9:00) Japan
//    (GMT+9:00) Pacific/Palau
//            (GMT+9:00) ROK
//    (GMT+9:30) ACT
//            (GMT+9:30) Australia/Adelaide
//            (GMT+9:30) Australia/Broken_Hill
//            (GMT+9:30) Australia/Darwin
//            (GMT+9:30) Australia/North
//            (GMT+9:30) Australia/South
//            (GMT+9:30) Australia/Yancowinna
//            (GMT+10:00) AET
//    (GMT+10:00) Antarctica/DumontDUrville
//            (GMT+10:00) Asia/Khandyga
//            (GMT+10:00) Asia/Yakutsk
//            (GMT+10:00) Australia/ACT
//            (GMT+10:00) Australia/Brisbane
//            (GMT+10:00) Australia/Canberra
//            (GMT+10:00) Australia/Currie
//            (GMT+10:00) Australia/Hobart
//            (GMT+10:00) Australia/Lindeman
//            (GMT+10:00) Australia/Melbourne
//            (GMT+10:00) Australia/NSW
//            (GMT+10:00) Australia/Queensland
//            (GMT+10:00) Australia/Sydney
//            (GMT+10:00) Australia/Tasmania
//            (GMT+10:00) Australia/Victoria
//            (GMT+10:00) Etc/GMT-10
//            (GMT+10:00) Pacific/Chuuk
//            (GMT+10:00) Pacific/Guam
//            (GMT+10:00) Pacific/Port_Moresby
//            (GMT+10:00) Pacific/Saipan
//            (GMT+10:00) Pacific/Truk
//            (GMT+10:00) Pacific/Yap
//            (GMT+10:30) Australia/LHI
//            (GMT+10:30) Australia/Lord_Howe
//            (GMT+11:00) Antarctica/Macquarie
//            (GMT+11:00) Asia/Sakhalin
//            (GMT+11:00) Asia/Ust-Nera
//            (GMT+11:00) Asia/Vladivostok
//            (GMT+11:00) Etc/GMT-11
//            (GMT+11:00) Pacific/Efate
//            (GMT+11:00) Pacific/Guadalcanal
//            (GMT+11:00) Pacific/Kosrae
//            (GMT+11:00) Pacific/Noumea
//            (GMT+11:00) Pacific/Pohnpei
//            (GMT+11:00) Pacific/Ponape
//            (GMT+11:00) SST
//    (GMT+11:30) Pacific/Norfolk
//            (GMT+12:00) Antarctica/McMurdo
//            (GMT+12:00) Antarctica/South_Pole
//            (GMT+12:00) Asia/Anadyr
//            (GMT+12:00) Asia/Kamchatka
//            (GMT+12:00) Asia/Magadan
//            (GMT+12:00) Etc/GMT-12
//            (GMT+12:00) Kwajalein
//            (GMT+12:00) NST
//    (GMT+12:00) NZ
//            (GMT+12:00) Pacific/Auckland
//            (GMT+12:00) Pacific/Fiji
//            (GMT+12:00) Pacific/Funafuti
//            (GMT+12:00) Pacific/Kwajalein
//            (GMT+12:00) Pacific/Majuro
//            (GMT+12:00) Pacific/Nauru
//            (GMT+12:00) Pacific/Tarawa
//            (GMT+12:00) Pacific/Wake
//            (GMT+12:00) Pacific/Wallis
//            (GMT+12:45) NZ-CHAT
//            (GMT+12:45) Pacific/Chatham
//            (GMT+13:00) Etc/GMT-13
//            (GMT+13:00) MIT
//            (GMT+13:00) Pacific/Apia
//            (GMT+13:00) Pacific/Enderbury
//            (GMT+13:00) Pacific/Fakaofo
//            (GMT+13:00) Pacific/Tongatapu
//            (GMT+14:00) Etc/GMT-14
//            (GMT+14:00) Pacific/Kiritimati

}
