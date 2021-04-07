package com.openx.internal_test_app.plugplay.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.openx.internal_test_app.R
import com.openx.internal_test_app.data.SimpleListItem
import kotlinx.android.synthetic.main.simple_list_item.view.*

class UtilitiesListAdapter(private val handleItemClick: (action: Int) -> Unit) : RecyclerView.Adapter<UtilitiesListAdapter.UtilitiesListViewHolder>() {
    private val utilitiesList = mutableListOf<SimpleListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            UtilitiesListViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.simple_list_item, parent, false))

    override fun getItemCount() = utilitiesList.size

    override fun onBindViewHolder(holder: UtilitiesListViewHolder, position: Int) {
        holder.bindView(utilitiesList[position], handleItemClick)
    }

    fun setUtilitiesList(utilitiesList: List<SimpleListItem>) {
        this.utilitiesList.clear()
        this.utilitiesList.addAll(utilitiesList)
        notifyDataSetChanged()
    }


    class UtilitiesListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(simpleListItem: SimpleListItem, handleItemClick: (action: Int) -> Unit) {
            itemView.lbl_text.text = simpleListItem.title
            itemView.listitem_demo.setOnClickListener { handleItemClick(simpleListItem.action) }
        }
    }
}