package me.blog.korn123.easyphotomap.timeline;

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
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

import me.blog.korn123.easyphotomap.R;
import me.blog.korn123.easyphotomap.constant.Constant;
import me.blog.korn123.easyphotomap.log.AAFLogger;
import me.blog.korn123.easyphotomap.search.PhotoEntity;
import me.blog.korn123.easyphotomap.utils.BitmapUtils;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
public class TimelineEntityAdapter extends ArrayAdapter<PhotoEntity> {

    private final Context context;
    private final int layoutResourceId;
    private final ArrayList<PhotoEntity> entities;
    private Activity activity;

    public TimelineEntityAdapter(Context context, Activity activity, int layoutResourceId, ArrayList<PhotoEntity> entities) {
        super(context, layoutResourceId, entities);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.entities = entities;
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
            holder.textView1 = (TextView)row.findViewById(R.id.address);
            holder.imageView1 = (ImageView)row.findViewById(R.id.thumbnail);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        PhotoEntity entity = entities.get(position);
        if (isDateChange(position)) {
            row.findViewById(R.id.timelineHeader).setVisibility(View.VISIBLE);
            ((TextView)row.findViewById(R.id.timelineDate)).setText(entity.date);
        } else {
            row.findViewById(R.id.timelineHeader).setVisibility(View.GONE);
        }
        holder.textView1.setText(entity.originDate + "\n" + entity.info);
        String fileName = FilenameUtils.getName(entity.imagePath);
        Bitmap bm = CommonUtils.decodeFile(activity, Constant.WORKING_DIRECTORY + fileName + ".thumb");
        holder.imageView1.setImageBitmap(bm);

        return row;
    }

    public boolean isDateChange(int position) {

        boolean isChange = false;
        String previousDate = null;
        String currentDate = null;
        if (position > 0) {
            PhotoEntity previous = entities.get(position - 1);
            PhotoEntity current = entities.get(position);
            previousDate = previous.date;
            currentDate = current.date;
            if (!StringUtils.equals(previousDate, currentDate)) {
                isChange = true;
            }
        } else {
            isChange = true;
        }
        AAFLogger.info("TimelineEntityAdapter-isDateChange INFO: " + position + ", " + isChange, getClass());
        return isChange;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
        ImageView imageView1;
    }
}
