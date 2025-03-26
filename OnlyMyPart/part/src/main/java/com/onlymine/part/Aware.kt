package com.onlymine.onlymypart.adapter

import android.os.Handler
import android.os.Looper
import androidx.annotation.UiThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.onlymine.onlyminedelegate.Impl.OnlyMyLog.Companion.logInfo

interface Aware


/**
 * 描述一个实图的几个核心钩子方法的抽象定义
 * 1. 可见性的变更 onVisibleChanged
 * 2. ViewTree相关的变更 onAttachedChanged
 * 3. 创建和销毁逻辑，onDestroy 和创建
 */

interface VisibleAware: Aware {

    /**
     * visible 是否可见
     */
    fun onVisibleChanged(visible: Boolean)

    /**
     * attached: 是否attached在window上，不等同于onVisible，一般来说attach不一定是visible，但是detach一定是inVisible
     */
    fun onAttachedChanged(attached: Boolean)

    /**
     * 所在lifecycle#onDestroy的时候将会调用这里
     */
    fun onDestroy()
}

enum class NotifyMode {
    NONE, //不进行通知
    ON_PAGE_SELECT, //ViewPager及ViewPager2使用该种通知方式
    ON_SCROLL_AREA_VISIBLE, //滑动之后，是否可见，不等同于ATTACH，划出屏幕及不可见
    ON_ATTACH_WINDOW //非上述场景的RecyclerView使用该种方式进行通知
}



/**
 * 为了让 ViewHolder 能感知到 onShow, onHide, 需要给到 其对应的 ViewLifecycleOwner
 * 必须在 adapter 里边委托 [notifyViewAttachedToWindow] [notifyViewDetachedFromWindow] 两个方法
 */
abstract class ViewHolderVisibleAwareNotifier : LifecycleEventObserver {

    companion object {
        const val TAG = "ViewHolderVisibleAwareNotifier"
    }

    private var enableVisibleAware = true
    val attachWindowViewHolders = LinkedHashSet<WeakReference<ViewHolder>>()
    private val allViewHolders = LinkedHashSet<WeakReference<ViewHolder>>()
    private var lifecycleVisible = false
    private var notifyMode = NotifyMode.ON_ATTACH_WINDOW
    private var hostRecyclerViewWrf: WeakReference<RecyclerView>? = null

    private var currentVisibleViewHolder: WeakReference<ViewHolder>? = null
    private var currentPagePos: Int = Int.MIN_VALUE

    private var handler = Handler(Looper.getMainLooper())
    private var visibleViewHolders = LinkedHashSet<WeakReference<ViewHolder>>()

    /**
     * onPageChange的调用都在onAttach之后
     */
    private val onPageChangeCallback: ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            // viewPager2设置position之后，会立即回调onPageSelectedChange，这个时候recyclerView还没切换完成，
            // 因此需要一个post来延迟，保证findViewHolderForAdapterPosition不会为空和准确性
            val hostRecyclerView = hostRecyclerViewWrf?.get() ?: return
            hostRecyclerView.post {
                logInfo(tag = TAG) {
                    "onPageChangeCallback prePos is $currentPagePos"
                }
                currentPagePos = position
                val holder: ViewHolder? =
                    hostRecyclerView.findViewHolderForAdapterPosition(position)
                // 1. 如果存在旧的visibleViewHolder，将其置为inVisible
                val currentVisibleHolder = currentVisibleViewHolder?.get()
                if (currentVisibleHolder is VisibleAware && currentVisibleHolder != holder) {
                    (currentVisibleHolder as VisibleAware).onVisibleChanged(
                        visible = false
                    )
                    val isOnWindow = attachWindowViewHolders.contains(currentVisibleViewHolder)
                    logViewHolderStatus(
                        "onPageChangeCallback", currentVisibleHolder, newVisible = false,
                        isOnWindow = isOnWindow
                    )
                }
                // 2. 将选中的viewHolder置为visible
                // 场景1：存在setCurrentItem的场景，这里回调先被调用了，此时holder获取不到，这时候需要在onAttach的时候来调用onVisible
                if (holder is VisibleAware) {
                    if (currentVisibleHolder != holder) {
                        currentVisibleViewHolder = WeakReference(holder)
                        val isOnWindow = attachWindowViewHolders.contains(currentVisibleViewHolder)
                        holder.onVisibleChanged(visible = true)
                        logViewHolderStatus(
                            "onPageChangeCallback",
                            holder,
                            newVisible = true,
                            isOnWindow = isOnWindow
                        )
                    } else {
                        logInfo(tag = TAG) {
                            "onPageChangeCallback same holder change pos to $position"
                        }
                    }
                } else {
                    currentVisibleViewHolder = null
                    logInfo(tag = TAG) { "onPageChangeCallback not find viewHolder:$currentPagePos" }
                }
            }
        }
    }

    private val adapterChangeListener: RecyclerView.AdapterDataObserver =
        object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                // fallback to onItemRangeChanged(positionStart, itemCount) if app
                // does not override this method.
                onItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                // do nothing
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                // do nothing
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                // do nothing
            }
        }

    private val onScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                triggerVisibleChange(recyclerView)
            }
        }

    private val triggerTask: Runnable = Runnable {
        hostRecyclerViewWrf?.get()?.let {
            triggerVisibleChange(recyclerView = it)
        }
    }

    private fun triggerVisibleChange(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val manager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPos = manager.findFirstVisibleItemPosition()
            val lastVisibleItemPos = manager.findLastVisibleItemPosition()
            logInfo(tag = TAG) {
                "firstVisiblePos:$firstVisibleItemPos lastVisibleItemPos:$lastVisibleItemPos"
            }
            attachWindowViewHolders.forEach { viewHolderWrf ->
                val viewHolder = viewHolderWrf.get() ?: return@forEach
                val visible = viewHolder.adapterPosition in firstVisibleItemPos..lastVisibleItemPos
                if (visible && !visibleViewHolders.contains(viewHolderWrf)) {
                    visibleViewHolders.add(viewHolderWrf)
                    (viewHolder as? VisibleAware)?.onVisibleChanged(true)
                    logViewHolderStatus(
                        "onScrollStateChanged",
                        viewHolder,
                        newVisible = true,
                        isOnWindow = true
                    )
                } else if (!visible && visibleViewHolders.contains(viewHolderWrf)) {
                    visibleViewHolders.remove(viewHolderWrf)
                    (viewHolder as? VisibleAware)?.onVisibleChanged(false)
                    logViewHolderStatus(
                        "onScrollStateChanged",
                        viewHolder,
                        newVisible = true,
                        isOnWindow = true
                    )
                }
            }
        }
    }

    private fun logViewHolderStatus(
        tag: String,
        holder: ViewHolder,
        newVisible: Boolean,
        isOnWindow: Boolean
    ) {
        logInfo(tag = TAG) {
            "$tag:hashCode:${holder.hashCode()} " +
                    " position:${holder.adapterPosition}" +
                    " newVisible:$newVisible" +
                    " isOnWindow:" + isOnWindow
        }
    }

    private var lifecycleOwnerWrf: WeakReference<LifecycleOwner>? = null

    @UiThread
    fun install(recyclerView: RecyclerView) {
        hostRecyclerViewWrf = WeakReference(recyclerView)
        if (recyclerView.isAttachedToWindow) {
            val owner = ViewLifecycleUtil.getViewLifecycleOwner(recyclerView)
            lifecycleOwnerWrf = WeakReference(owner)
            owner?.lifecycle?.addObserver(this)
        } else {
            recyclerView.post {
                val owner = ViewLifecycleUtil.getViewLifecycleOwner(recyclerView)
                lifecycleOwnerWrf = WeakReference(owner)
                owner?.lifecycle?.addObserver(this)
            }
        }
        if (!enableVisibleAware) {
            notifyMode = NotifyMode.NONE
        } else if (recyclerView.parent is ViewPager2) {
            notifyMode = NotifyMode.ON_PAGE_SELECT
            (recyclerView.parent as ViewPager2).registerOnPageChangeCallback(onPageChangeCallback)
        } else if (recyclerView.layoutManager is LinearLayoutManager) {
            notifyMode = NotifyMode.ON_SCROLL_AREA_VISIBLE
            recyclerView.addOnScrollListener(onScrollListener)
            recyclerView.adapter?.registerAdapterDataObserver(adapterChangeListener)
        } else {
            notifyMode = NotifyMode.ON_ATTACH_WINDOW
        }
    }

    @UiThread
    fun unInstall() {
        lifecycleOwnerWrf?.get()?.lifecycle?.removeObserver(this)
    }

    /**
     * 保留方法
     */
    @UiThread
    fun unInstall(viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycle.removeObserver(this)
    }

    fun registerViewHolder(viewHolder: ViewHolder) {
        allViewHolders.add(WeakReference(viewHolder))
    }

    @UiThread
    fun notifyViewAttachedToWindow(viewHolder: ViewHolder) {
        logInfo(tag = TAG) {
            "notifyViewAttachedToWindow: attach hashCode${viewHolder.hashCode()} position:${viewHolder.adapterPosition}"
        }
        registerViewHolder(viewHolder)
        attachWindowViewHolders.add(WeakReference(viewHolder))
        if (viewHolder !is VisibleAware || notifyMode == NotifyMode.NONE) {
            return
        }
        viewHolder.onAttachedChanged(true)
        if (notifyMode == NotifyMode.ON_ATTACH_WINDOW) {
            if (lifecycleVisible) {
                viewHolder.onVisibleChanged(visible = true)
                logViewHolderStatus(
                    "notifyViewAttachedToWindow",
                    viewHolder,
                    newVisible = true,
                    isOnWindow = true
                )
            }
        } else if (notifyMode == NotifyMode.ON_PAGE_SELECT && currentVisibleViewHolder == null &&
            viewHolder.adapterPosition == currentPagePos
        ) {
            currentVisibleViewHolder = WeakReference(viewHolder)
            if (lifecycleVisible) {
                viewHolder.onVisibleChanged(visible = true)
                logViewHolderStatus(
                    "notifyViewAttachedToWindow",
                    viewHolder,
                    newVisible = true,
                    isOnWindow = true
                )
            }
        } else if (notifyMode == NotifyMode.ON_SCROLL_AREA_VISIBLE) {
            //doNothing，visibleChange is only notify in scroll listener
            handler.removeCallbacks(triggerTask)
            handler.post(triggerTask)
        }
    }

    @UiThread
    fun notifyViewDetachedFromWindow(viewHolder: ViewHolder) {
        logInfo(tag = TAG) {
            "notifyViewDetachedFromWindow: detached: hashCode${viewHolder.hashCode()} position:${viewHolder.adapterPosition}"
        }
        attachWindowViewHolders.remove(WeakReference(viewHolder))
        if (viewHolder !is VisibleAware || notifyMode == NotifyMode.NONE) {
            return
        }
        viewHolder.onAttachedChanged(false)
        if (notifyMode == NotifyMode.ON_ATTACH_WINDOW) {
            viewHolder.onVisibleChanged(visible = false)
        } else if (notifyMode == NotifyMode.ON_SCROLL_AREA_VISIBLE) {
            val exist = visibleViewHolders.remove(WeakReference(viewHolder))
            if (exist) {
                viewHolder.onVisibleChanged(visible = false)
                logViewHolderStatus(
                    "notifyViewDetachedFromWindow",
                    viewHolder,
                    newVisible = false,
                    isOnWindow = false
                )
            }
        }
    }

    @UiThread
    fun notifyViewRecycled(viewHolder: ViewHolder) {
        if (notifyMode == NotifyMode.ON_SCROLL_AREA_VISIBLE) {
            //visibleViewHolders.remove(WeakReference(viewHolder))
            logInfo(tag = TAG) {
                "viewRecycled: ${viewHolder.adapterPosition}"
            }
        }
    }

    @UiThread
    private fun onVisibleChanged(newVisible: Boolean) {
        if (lifecycleVisible == newVisible) {
            return
        }
        if (notifyMode == NotifyMode.ON_ATTACH_WINDOW) {
            attachWindowViewHolders.forEach { viewHolderWrf ->
                val viewHolder = viewHolderWrf.get() ?: return@forEach
                if (viewHolder is VisibleAware) {
                    viewHolder.onVisibleChanged(newVisible)
                    logViewHolderStatus(
                        "onVisibleChanged",
                        viewHolder,
                        newVisible = newVisible,
                        isOnWindow = true
                    )
                }
            }
        } else if (notifyMode == NotifyMode.ON_PAGE_SELECT) {
            val viewHolder = currentVisibleViewHolder?.get()
            if (viewHolder is VisibleAware) {
                viewHolder.onVisibleChanged(newVisible)
                logInfo(tag = TAG) {
                    " onVisibleChanged:hashCode:${viewHolder.hashCode()} " +
                            " position:${viewHolder.adapterPosition}" +
                            " newVisible:$newVisible"
                }
            }
        } else if (notifyMode == NotifyMode.ON_SCROLL_AREA_VISIBLE) {
            visibleViewHolders.forEach { viewHolderWrf ->
                val viewHolder = viewHolderWrf.get() ?: return@forEach
                if (viewHolder is VisibleAware) {
                    viewHolder.onVisibleChanged(newVisible)
                    logViewHolderStatus(
                        "onVisibleChanged",
                        viewHolder,
                        newVisible = newVisible,
                        isOnWindow = true
                    )
                }
            }
        }
        lifecycleVisible = newVisible
    }

    @UiThread
    private fun onDestroy() {
        //对于没有使用section的ViewHolder，也就没有注册进来，因此就没有执行onDestroy的可能
        allViewHolders.forEach { viewHolderWrf ->
            val viewHolder = viewHolderWrf.get() ?: return@forEach
            if (viewHolder is VisibleAware && notifyMode != NotifyMode.NONE) {
                viewHolder.onDestroy()
            }
        }
        allViewHolders.clear()
        attachWindowViewHolders.clear()
        currentVisibleViewHolder = null
        onAwareDestroyed()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            onVisibleChanged(true)
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            onVisibleChanged(false)
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            logInfo(tag = TAG) {
                "source:$source destroy"
            }
            onDestroy()
            source.lifecycle.removeObserver(this)
        }
    }

    abstract fun onAwareDestroyed()
}



