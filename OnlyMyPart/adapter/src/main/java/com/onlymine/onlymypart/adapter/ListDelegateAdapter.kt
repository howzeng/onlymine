package com.onlymine.onlymypart.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlymine.onlymypart.adapter.delegate.AdapterDelegatesManager

class ListDelegateAdapter<T>(private val delegatesManager: AdapterDelegatesManager<List<T>>) :
    VisibleNotifyAdapter<RecyclerView.ViewHolder>() {

    protected var items: List<T>? = null
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

    override fun getItemViewType(position: Int): Int {
        return items?.let { delegatesManager.getItemViewType(it, position) } ?: 0
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        delegatesManager.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return delegatesManager.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        delegatesManager.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        delegatesManager.onViewDetachedFromWindow(holder)
    }
}