/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp.plugplay.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.data.SimpleListItem

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
            itemView.findViewById<TextView>(R.id.lbl_text).text = simpleListItem.title
            itemView.findViewById<RelativeLayout>(R.id.listitem_demo)
                .setOnClickListener { handleItemClick(simpleListItem.action) }
        }
    }
}