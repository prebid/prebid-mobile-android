package com.openx.internal_test_app.utils.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.openx.internal_test_app.R
import com.openx.internal_test_app.data.DemoItem

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