package me.blog.korn123.easyphotomap.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.drew.lang.GeoLocation;
import com.drew.metadata.exif.GpsDirectory;

import java.util.List;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.models.FileItem;
import me.blog.korn123.easyphotomap.utils.BitmapUtils;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-30.
 */
public class ExplorerItemAdapter extends ArrayAdapter<FileItem> {

    private final Activity mActivity;
    private final Context mContext;
    private final List<FileItem> mEntities;
    private final int mLayoutResourceId;

    public ExplorerItemAdapter(Activity activity, Context context, int layoutResourceId, List<FileItem> entities) {
        super(context, layoutResourceId, entities);
        this.mActivity = activity;
        this.mContext = context;
        this.mEntities = entities;
        this.mLayoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
            holder = new ViewHolder();
            // bind view
            holder.textView1 = (TextView)row.findViewById(R.id.text1);
            holder.textView2 = (TextView)row.findViewById(R.id.text2);
            holder.textView3 = (TextView)row.findViewById(R.id.text3);
            holder.imageView1 = (ImageView)row.findViewById(R.id.image1);
            holder.textView1.setTypeface(Typeface.DEFAULT);
            holder.textView2.setTypeface(Typeface.DEFAULT);
            holder.textView3.setTypeface(Typeface.DEFAULT);

            // set tag
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        FileItem entity = mEntities.get(position);

        // init default option
        int widthHeight = (int)(CommonUtils.getDefaultDisplay(mActivity).x / 5);
//        holder.imageView1.getLayoutParams().height = widthHeight;
//        holder.imageView1.getLayoutParams().width = widthHeight;

        // init default value
        holder.textView1.setText(entity.fileName);
        holder.textView2.setText("");
        holder.textView3.setText("");
        holder.imageView1.setImageBitmap(getDefaultImage());

        // init async process
        String imagePath = entity.getImagePath();
        holder.position = position;
        if (entity.isDirectory) {
            holder.textView2.setVisibility(View.GONE);
            holder.textView3.setVisibility(View.GONE);
            new ThumbnailTask(mActivity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, String.valueOf(widthHeight));
        } else {
            holder.textView2.setVisibility(View.VISIBLE);
            holder.textView3.setVisibility(View.VISIBLE);
            new ThumbnailTask(mActivity, position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagePath, String.valueOf(widthHeight));
        }
        return row;
    }

    private static class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;
        private Activity mActivity;
        private boolean isDirectory = false;
        private GeoLocation geoLocation;
        public ThumbnailTask(Activity activity, int position, ViewHolder holder) {
            mActivity = activity;
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String filePath = params[0];
            int widthHeight = CommonUtils.dpToPixel(mActivity, 45);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 20;
            Bitmap resized = null;
//            Log.i("doInBack", String.format("%s, %s", mHolder.position, mPosition));
            if (mHolder.position == mPosition) {
                if (filePath == null) {
                    isDirectory = true;
                    Bitmap bitmap = BitmapUtils.getBitmapFromMemCache("defaultBitmap");
                    if (bitmap == null) {
                        bitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.ic_menu_archive);
                        BitmapUtils.addBitmapToMemoryCache("defaultBitmap", bitmap);
                    }
                    resized = bitmap;
                } else {
                    Bitmap bitmap = BitmapUtils.getBitmapFromMemCache(filePath);
                    if (bitmap == null) {
                        bitmap = BitmapUtils.decodeFile(mActivity, filePath, options);
                        BitmapUtils.addBitmapToMemoryCache(filePath, bitmap);
                    }
                    resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true);

                    GpsDirectory gpsDirectory = CommonUtils.getGPSDirectory(filePath);
                    if (gpsDirectory != null && gpsDirectory.getGeoLocation() != null) {
                        geoLocation = gpsDirectory.getGeoLocation();
                    }
                }
            } else {
                // mListView holder가 재활용되면 task cancel 되도록 수정 2016.11.07 Hanjoong Cho
                this.cancel(true);
            }
            return resized;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mHolder.position == mPosition) {
                if (isDirectory) {
                    mHolder.imageView1.setImageBitmap(bitmap);
                } else {
                    if (geoLocation != null) {
                        mHolder.textView2.setText("위도: " + String.valueOf(geoLocation.getLatitude()));
                        mHolder.textView3.setText("경도: " + String.valueOf(geoLocation.getLongitude()));
                    } else {
                        mHolder.textView2.setText("위도: 정보없음");
                        mHolder.textView3.setText("경도: 정보없음");
                    }
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[] {
                                    new ColorDrawable(Color.TRANSPARENT),
                                    new BitmapDrawable(mActivity.getResources(), bitmap)
                            });
                    mHolder.imageView1.setImageDrawable(td);
                    td.startTransition(1000);
                }
            }
        }
    }

    private Bitmap mDefaultBitmap = null;
    private Bitmap getDefaultImage() {
        if (mDefaultBitmap == null) {
            mDefaultBitmap = BitmapFactory.decodeResource(mActivity.getResources(), android.R.drawable.ic_menu_gallery);
        }
        return mDefaultBitmap;
    }

    private static class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        ImageView imageView1;
        public int position;
    }
}
