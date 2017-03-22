package me.blog.korn123.easyphotomap.thumbnail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-08-02.
 */
public class ThumbnailEntityAdapter extends ArrayAdapter<ThumbnailEntity> {

    private final Context context;
    private final Activity activity;
    private final List<ThumbnailEntity> entities;
    private final int layoutResourceId;
    public ThumbnailEntityAdapter(Activity activity, Context context, int layoutResourceId, List<ThumbnailEntity> entities) {
        super(context, layoutResourceId, entities);
        this.activity = activity;
        this.context = context;
        this.entities = entities;
        this.layoutResourceId = layoutResourceId;
    }

    ProgressDialog progressDialog;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        ViewHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageView1 = (ImageView)row.findViewById(R.id.image1);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }
//        holder.imageView1.setImageBitmap(null);
        int widthHeight = (int)((CommonUtils.getDefaultDisplay(activity).x - CommonUtils.dpToPixel(activity, 30, 1)) / 3);
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_gallery);
//        Bitmap resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true);
//        holder.imageView1.setImageBitmap(bitmap);
        ThumbnailEntity entity = entities.get(position);
        String imagePath = entity.imagePath ;
        String thumbnailPath = entity.thumbnailPath;
        String imageName = FilenameUtils.getName(imagePath);
        holder.position = position;
        holder.imageView1.getLayoutParams().height = widthHeight;
        holder.imageView1.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_menu_gallery));
        new ThumbnailTask(activity, position, holder).execute(thumbnailPath, String.valueOf(widthHeight));
//        new ThumbnailTask(position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, thumbnailPath, String.valueOf(widthHeight));
//        AsyncUtils.loadBitmap(holder.imageView1, thumbnailPath, widthHeight);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = false;
//        options.outWidth = widthHeight;
//        options.outHeight = widthHeight;
//        options.inSampleSize = 10;
//        Bitmap resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true);
//        holder.imageView1.setImageBitmap(resized);
        return row;
    }

    private static class ThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private int mPosition;
        private ViewHolder mHolder;
        private Activity activity;
        private int widthHeight = 0;

        public ThumbnailTask(Activity activity, int position, ViewHolder holder) {
            mPosition = position;
            mHolder = holder;
            this.activity = activity;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String filePath = params[0];
            widthHeight = Integer.valueOf(params[1]);
//            clearHandler.sendMessage(message);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            if (!new File(filePath).exists()) return null;
            Bitmap resized = null;
            if (mHolder.position == mPosition) {
                try {
                    Bitmap bitmap = CommonUtils.decodeFile(activity, filePath, options);
                    resized = Bitmap.createScaledBitmap(bitmap, widthHeight, widthHeight, true);
                } catch (Exception e) {
                    AAFLogger.error("AAFLogger-doInBackground ERROR: " + e.getMessage(), getClass());
                }
            } else {
                // listView holder가 재활용되면 task cancel 되도록 수정 2016.11.07 Hanjoong Cho
                this.cancel(true);
            }
            return resized;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (mHolder.position == mPosition && bitmap != null) {
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[] {
                                new ColorDrawable(Color.TRANSPARENT),
                                new BitmapDrawable(activity.getResources(), bitmap)
                        });
//                mHolder.imageView1.setImageBitmap(bitmap);
                mHolder.imageView1.setImageDrawable(td);
                td.startTransition(1000);
            }
        }
    }

    private static class ViewHolder {
        public ImageView imageView1;
        public int position;
    }
}
