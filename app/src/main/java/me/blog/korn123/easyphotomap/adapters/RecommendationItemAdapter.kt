package me.blog.korn123.easyphotomap.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.activities.MapsActivity
import java.util.*

/**
 * Created by CHO HANJOONG on 2016-07-20.
 */
class RecommendationItemAdapter(private val activity: Activity,
                                private val listRecommendationItem: ArrayList<MapsActivity.Recommendation>,
                                private val onItemClickListener: AdapterView.OnItemClickListener
) : RecyclerView.Adapter<RecommendationItemAdapter.ViewHolder>() {
    private val layoutInflater = activity.layoutInflater

    private fun createViewHolder(layoutType: Int, parent: ViewGroup?): RecommendationItemAdapter.ViewHolder {
        val view = layoutInflater.inflate(layoutType, parent, false)
        return RecommendationItemAdapter.ViewHolder(view as ViewGroup)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val recommendationItem: MapsActivity.Recommendation = listRecommendationItem[position]
        holder?.let {
            it.text1?.text = recommendationItem.toString()
            it.itemView.setOnClickListener { item ->
                onItemClickListener.onItemClick(null, item, it.adapterPosition, it.itemId)
            }
        }
    }

    override fun getItemCount(): Int = listRecommendationItem.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder = createViewHolder(R.layout.item_recommendation, parent)

    fun getItem(position: Int): MapsActivity.Recommendation = listRecommendationItem[position]

    class ViewHolder(val parent: ViewGroup?) : RecyclerView.ViewHolder(parent) {
        var text1: TextView? = null
        init {
            parent?.let {
                text1 = parent.findViewById(R.id.text1)
            }
        }
    }
}
