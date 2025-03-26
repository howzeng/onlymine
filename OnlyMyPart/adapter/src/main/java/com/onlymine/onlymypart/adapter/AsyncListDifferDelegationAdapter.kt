package com.onlymine.onlymypart.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.onlymine.onlymypart.adapter.delegate.AdapterDelegatesManager

class AsyncListDifferDelegationAdapter<T>(private val diffCallback: DiffUtil.Callback) :
    VisibleNotifyAdapter<RecyclerView.ViewHolder>() {


    protected var items: List<T>? = null

    private val delegatesManager = AdapterDelegatesManager<List<T>>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = delegatesManager.onCreateViewHolder(parent, viewType)
        visibleAwareNotifier.registerViewHolder(viewHolder)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items?.let {
            delegatesManager.onBindViewHolder(it, position, holder, null)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        items?.let {
            delegatesManager.onBindViewHolder(it, position, holder, payloads)
        }
    }
}