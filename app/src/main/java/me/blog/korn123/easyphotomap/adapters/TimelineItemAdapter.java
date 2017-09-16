package me.blog.korn123.easyphotomap.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import me.blog.korn123.easyphotomap.constants.Constant;
import me.blog.korn123.easyphotomap.models.PhotoMapItem;
import me.blog.korn123.easyphotomap.utils.CommonUtils;

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
public class TimelineItemAdapter extends ArrayAdapter<PhotoMapItem> {

    private final Context context;
    private final int layoutResourceId;
    private final ArrayList<PhotoMapItem> listPhotoMapItem;
    private Activity activity;

    public TimelineItemAdapter(Context context, Activity activity, int layoutResourceId, ArrayList<PhotoMapItem> listPhotoMapItem) {
        super(context, layoutResourceId, listPhotoMapItem);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.listPhotoMapItem = listPhotoMapItem;
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

        PhotoMapItem photoMapItem = listPhotoMapItem.get(position);
        if (isDateChange(position)) {
            row.findViewById(R.id.timelineHeader).setVisibility(View.VISIBLE);
            ((TextView)row.findViewById(R.id.timelineDate)).setText(photoMapItem.dateWithoutTime);
        } else {
            row.findViewById(R.id.timelineHeader).setVisibility(View.GONE);
        }
        holder.textView1.setText(photoMapItem.date + "\n" + photoMapItem.info);
        String fileName = FilenameUtils.getName(photoMapItem.imagePath);
        Bitmap bm = CommonUtils.decodeFile(activity, Constant.WORKING_DIRECTORY + fileName + ".thumb");
        holder.imageView1.setImageBitmap(bm);

        return row;
    }

    public boolean isDateChange(int position) {
        boolean isChange = false;
        String previousDate = null;
        String currentDate = null;
        if (position > 0) {
            PhotoMapItem previous = listPhotoMapItem.get(position - 1);
            PhotoMapItem current = listPhotoMapItem.get(position);
            previousDate = previous.dateWithoutTime;
            currentDate = current.dateWithoutTime;
            if (!StringUtils.equals(previousDate, currentDate)) {
                isChange = true;
            }
        } else {
            isChange = true;
        }
        return isChange;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
        ImageView imageView1;
    }
}
