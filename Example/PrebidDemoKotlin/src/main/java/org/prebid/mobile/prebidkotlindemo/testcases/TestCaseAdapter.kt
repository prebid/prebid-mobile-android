package org.prebid.mobile.prebidkotlindemo.testcases

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.prebid.mobile.prebidkotlindemo.databinding.ListItemAdTypeBinding

class TestCaseAdapter : RecyclerView.Adapter<TestCaseAdapter.AdTypeViewHolder>() {

    private var list: ArrayList<TestCase> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdTypeViewHolder {
        val binding = ListItemAdTypeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdTypeViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size


    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: ArrayList<TestCase>) {
        this.list = list
        notifyDataSetChanged()
    }


    class AdTypeViewHolder(
        private val binding: ListItemAdTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(testCase: TestCase) {
            binding.tvName.text = testCase.name
        }

    }

}