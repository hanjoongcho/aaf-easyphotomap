package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.location.Address
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class AddressItemAdapter(private val mContext: Context, private val mLayoutResourceId: Int, private val mListAddress: List<Address>) : ArrayAdapter<Address>(mContext, mLayoutResourceId, mListAddress) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        var row = convertView
        var holder: ViewHolder?
        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(android.R.id.text1) as TextView
            holder.textView2 = row.findViewById(android.R.id.text2) as TextView
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val address = mListAddress[position]
        val addressBuilder = StringBuilder()
        if (address.countryName != null) addressBuilder.append(address.countryName).append(" ")
        if (address.locality != null) addressBuilder.append(address.locality).append(" ")
        if (address.subLocality != null) addressBuilder.append(address.subLocality).append(" ")
        if (address.thoroughfare != null) addressBuilder.append(address.thoroughfare).append(" ")
        if (address.featureName != null) addressBuilder.append(address.featureName).append(" ")
        holder.textView1?.text = addressBuilder.toString()

        val latLonBuilder = StringBuilder()
        latLonBuilder.append("latitude: " + address.latitude)
        latLonBuilder.append("longitude: " + address.longitude)
        holder.textView2?.text = latLonBuilder.toString()
        return row
    }

    internal class ViewHolder {
        var textView1: TextView? = null
        var textView2: TextView? = null
    }
}
