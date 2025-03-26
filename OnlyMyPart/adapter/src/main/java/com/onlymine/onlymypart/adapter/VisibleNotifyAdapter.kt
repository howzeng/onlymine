package com.onlymine.onlymypart.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class VisibleNotifyAdapter<VH : ViewHolder>: RecyclerView.Adapter<VH>() {
    var recyclerView: RecyclerView? = null

    val visibleAwareNotifier = object: ViewHolderVisibleAwareNotifier() {
        override fun onAwareDestroyed() {
            recyclerView?.let {
                onAdapterDestroy(it)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: VH) {
        visibleAwareNotifier.notifyViewAttachedToWindow(holder)
    }


    override fun onViewDetachedFromWindow(holder: VH) {
        visibleAwareNotifier.notifyViewDetachedFromWindow(holder)
    }


    override fun onViewRecycled(holder: VH) {
        visibleAwareNotifier.notifyViewRecycled(holder)
    }


    override fun onAttachedToRecyclerView(hostRecyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(hostRecyclerView)
        recyclerView = hostRecyclerView
        visibleAwareNotifier.install(hostRecyclerView)
    }

    override fun onDetachedFromRecyclerView(hostRecyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(hostRecyclerView)
        recyclerView = null
        visibleAwareNotifier.unInstall()
    }

    private fun onAdapterDestroy(hostRecyclerView: RecyclerView) {
    }
}