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

package org.prebid.mobile.renderingtestapp.utils.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.data.DemoItem

class DemoListAdapter(private val clickListener: DemoItemClickListener) : ListAdapter<DemoItem, DemoListAdapter.DemoItemViewHolder>(DemoItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoItemViewHolder {
        return DemoItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DemoItemViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class DemoItemViewHolder private constructor(private val root: View) : RecyclerView.ViewHolder(root) {
        private lateinit var demoItem: DemoItem

        companion object {
            fun from(parent: ViewGroup): DemoItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val listItem = layoutInflater.inflate(R.layout.simple_list_item, parent, false)
                return DemoItemViewHolder(listItem)
            }
        }

        fun bind(item: DemoItem, clickListener: DemoItemClickListener) {
            demoItem = item
            root.setOnClickListener {
                clickListener.onClick(demoItem)
            }
            val label = root.findViewById<TextView>(R.id.lbl_text)
            label.text = demoItem.label
        }
    }
}

class DemoItemDiffCallback : DiffUtil.ItemCallback<DemoItem>() {
    override fun areItemsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
        return oldItem.label == newItem.label
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DemoItem, newItem: DemoItem): Boolean {
        return oldItem.bundle == newItem.bundle
    }

}

interface DemoItemClickListener {
    fun onClick(item: DemoItem)
}