package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.content.Context
import android.location.Address
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import me.blog.korn123.easyphotomap.R

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class AddressItemAdapter(private val mContext: Context, private val mLayoutResourceId: Int, private val mListAddress: List<Address>) : ArrayAdapter<Address>(mContext, mLayoutResourceId, mListAddress) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder
        if (row == null) {
            val inflater = (mContext as Activity).layoutInflater
            row = inflater.inflate(mLayoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.text1) as TextView
            holder.textView2 = row.findViewById(R.id.text2) as TextView
            holder.textView3 = row.findViewById(R.id.text3) as TextView
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
        holder.textView2?.text = "latitude: ${address.latitude}" 
        holder.textView3?.text = "longitude: ${address.longitude}"
        return row
    }

    internal class ViewHolder {
        var textView1: TextView? = null
        var textView2: TextView? = null
        var textView3: TextView? = null
    }
}
