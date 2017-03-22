package me.blog.korn123.easyphotomap.search;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
public class PhotoEntityAdapter extends ArrayAdapter<PhotoEntity> {

    private final Context context;
    private final ArrayList<PhotoEntity> entities;
    private final int layoutResourceId;
    private Activity activity;

    public PhotoEntityAdapter(Context context, Activity activity, int layoutResourceId, ArrayList<PhotoEntity> entities) {
        super(context, layoutResourceId, entities);
        this.context = context;
        this.entities = entities;
        this.layoutResourceId = layoutResourceId;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView1 = (TextView)row.findViewById(R.id.text1);
            holder.textView2 = (TextView)row.findViewById(R.id.text2);
            holder.imageView1 = (ImageView)row.findViewById(R.id.image1);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        PhotoEntity imageEntity = entities.get(position);
        Bitmap bitmap = null;
        Bitmap scaledBitmap = null;
        String fileName = FilenameUtils.getName(imageEntity.imagePath);
        bitmap = CommonUtils.decodeFile(activity, Constant.WORKING_DIRECTORY + fileName + ".thumb");
        Point point = new Point();
        point.x = bitmap.getWidth();
        point.y = bitmap.getHeight();
        double fixedWidthHeight = Double.parseDouble(CommonUtils.loadStringPreference(context, "photo_size_setting", "0.6")) * 2;
        scaledBitmap = CommonUtils.createScaledBitmap(bitmap, point, fixedWidthHeight, fixedWidthHeight);
//        int height = bitmap.getHeight();
//        int width = bitmap.getWidth();
        holder.textView1.setText(imageEntity.info);
        holder.textView2.setText(imageEntity.date.toString());
        holder.imageView1.setImageBitmap(scaledBitmap);
//        holder.textView3.setText(smsDto.getBody());
        return row;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
        ImageView imageView1;
    }
}
