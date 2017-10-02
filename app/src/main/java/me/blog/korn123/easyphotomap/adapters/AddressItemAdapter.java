package me.blog.korn123.easyphotomap.adapters;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
public class AddressItemAdapter extends ArrayAdapter<Address> {

    private final Context mContext;
    private final List<Address> mListAddress;
    private final int mLayoutResourceId;

    public AddressItemAdapter(Context context, int layoutResourceId, List<Address> listAddress) {
        super(context, layoutResourceId, listAddress);
        this.mContext = context;
        this.mListAddress = listAddress;
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
            holder.textView1 = (TextView)row.findViewById(android.R.id.text1);
            holder.textView2 = (TextView)row.findViewById(android.R.id.text2);
            row.setTag(holder);
        } else {
            holder = (ViewHolder)row.getTag();
        }

        Address address = mListAddress.get(position);
        StringBuilder addressBuilder = new StringBuilder();
        if (address.getCountryName() != null) addressBuilder.append(address.getCountryName()).append(" ");
        if (address.getLocality() != null) addressBuilder.append(address.getLocality()).append(" ");
        if (address.getSubLocality() != null) addressBuilder.append(address.getSubLocality()).append(" ");
        if (address.getThoroughfare() != null) addressBuilder.append(address.getThoroughfare()).append(" ");
        if (address.getFeatureName() != null) addressBuilder.append(address.getFeatureName()).append(" ");
        holder.textView1.setText(addressBuilder.toString());

        StringBuilder latlonBuilder = new StringBuilder();
        latlonBuilder.append("latitude: " + address.getLatitude());
        latlonBuilder.append("longitude: " + address.getLongitude());
        holder.textView2.setText(latlonBuilder.toString());
        return row;
    }

    static class ViewHolder {
        TextView textView1;
        TextView textView2;
    }
}
