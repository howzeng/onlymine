package com.onlymine.onlymypart.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 *
 *
 * 此委托类提供了挂载到｛@link RecyclerView.Adapter｝生命周期的方法。因此可以将Adapter中复杂的逻辑进行拆解，
 * 根据 [.isForViewType]来分发到不同的AdapterDelegate中，进而实现解耦和组合的设计理念
 * 这种挂载（转发）机制由｛@link AdapterDelegatesManager｝提供实现，该类只是抽象定义类，可以阅读该类的方法注释，
 * 会介绍这些钩子方法会在什么时候被调用，以及推荐在这方法内做什么操作
 *
 *
 * @param <T> 数据类型
</T> */
abstract class AdapterDelegate<T> {
    /**
     * 判断当前当前代理类是否能够处理给定的数据，当返回true后，后续该数据item在Adapter中的钩子方法调用均会由该类进行代理
     *
     * @param items Adapter的所有数据源
     * @param position 在items中的序号
     * @return true,由该类进行后续代理
     */
    abstract fun isForViewType(items: T, position: Int): Boolean

    /**
     * 为当前item数据对象创建 [RecyclerView.ViewHolder]
     *
     * @param parent 创建的itemView后续挂载的ViewGroup parent
     * @return RecyclerView.ViewHolder对象实例
     */
    abstract fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    /**
     * Called to bind the [RecyclerView.ViewHolder] to the item of the datas source set
     *
     * @param items The data source
     * @param position The position in the datasource
     * @param holder The [RecyclerView.ViewHolder] to bind
     * @param payloads A non-null list of merged payloads. Can be empty list if requires full update.
     */
    abstract fun onBindViewHolder(
        items: T, position: Int,
        holder: RecyclerView.ViewHolder, payloads: List<Any?>
    )

    /**
     *
     *
     * 当该Adapter创建的ViewHolder被添加RecyclerViewPool进行回收的时候，调用此方法。
     * 该方法同时也发生在ViewHolder内部数据清理之前。
     *
     *
     *
     * 这个时机由｛@link RecyclerView.LayoutManager｝决定不再使用该ViewHolder。这可能是因为
     *  *
     * 当 ViewHolder 滑出屏幕并且不再可见时。
     *  *
     * 当 RecyclerView 进行数据更新操作（如 notifyDataSetChanged，notifyItemRemoved 等）时，需要回收旧的 ViewHolder。
     *  *
     * 当 RecyclerView 进行布局刷新（如 requestLayout，invalidate 等）时，需要回收旧的 ViewHolder。
     *
     *
     *
     *
     * what we can do?
     *  *
     * 1. 释放绑定(占用)的资源，例如超大的Bitmap对象，可以在这个时机进行release或者添加到BitmapPool
     * 后续框架，图库等可以结合进一步进行资源回收
     *  *
     * 2. 其他需要用到数据的操作，这里是最后的时机了，仍可以通过[RecyclerView.ViewHolder.getAdapterPosition]
     * 进而获取到在adapter中的数据
     *
     *
     *
     * @param holder 将要recycle的ViewHolder
     */
    open fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    }

    /**
     *
     *
     * 当该Adapter创建的ViewHolder在进行recycle的时候因为View#hasTransientState()，即View在"瞬态"中时，无法进入到recycled.
     * 此时该方法会被调用。
     * 而这个transient state一般是由动画（ViewPropertyAnimator）引起。
     * 在这个方法中，我们可以对正在进行的动画进行取消，然后返回true，来让ViewHolder走到recycled阶段
     * 值得注意的是，在这个调用方法阶段的View已经是从RecyclerView中移除的
     *
     * 当View处于transient state时，仍然是有进行回收的必要的。大多数情况下,
     * View的transient state会在[RecyclerView.Adapter.onBindViewHolder]
     * 中被清除。因此，RecyclerView将回收与否的决策权留给Adapter，并使用返回此方法的值，以决定是否应回收视图。
     *
     *
     * 与[RecyclerView.ItemAnimator]的关系:
     * 当RecyclerView的动画是由[RecyclerView.ItemAnimator]实现的时候，该方法回调并不会被调用。这是因为这个时候
     * 所有的子View都还是RecyclerView的child view，并不存在回收一说
     * 因此该方法调用只会出现在我们实现了自定义的列表动画（没有实现[RecyclerView.ItemAnimator])
     *
     * 永远不要调用!!!
     * `holder.itemView.setHasTransientState(false);`除非前面已经调用过
     * `holder.itemView.setHasTransientState(true);`.
     * 这两个调用内部有计数，必须成对出现。否则, View的状态将会存在一致性问题.
     * 处理transient state 问题，应该优先是end/cancel 触发transient state的动画，而不是手动调用方法来进行重制
     *
     *  [更多分析请看](https://www.jianshu.com/p/97bbce6e3f8c)
     *
     *
     * @param holder 由于ViewHolder#view的transient state.无法进入recycle的ViewHolder
     * @return True，View应该会被忽略是否transient state，直接被回收。
     * False反之。RecyclerView会再检查一遍View的transient state，并作出最后决策，
     * 因此你在此方法中移除了View的transient state并返回false，也能得到跟"一样"的回收效果
     * 默认的内部实现是返回False
     */
    fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
        return false
    }

    /**
     *
     *
     * 当ViewHolder所在的View添加到Window时调用该方法
     *
     *
     *
     * 建议执行的操作:
     *  *
     * 1. ViewTree相关的操作，这个时候，可以往上回溯到[android.view.ViewRootImpl]
     *  *
     * 2  可以在这里做一些[.onViewDetachedFromWindow]成对管理的事情，注册与反注册监听，状态恢复与存储
     *
     *
     *
     * @param holder 被添加到window的ViewHolder
     */
    fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
    }

    /**
     *
     *
     * 当ViewHolder所在的View被移除Window的时候调用该方法
     *
     *
     *
     * 值得注意的是：
     *  *  1. 不可见并不意味着被移除在Window之外，例如给RecyclerView设置了多个prefetch count，
     * ViewPager2设置了offscreenPageLimit > 1等场景，这时，recyclerview会预加载/缓存多个view在可见范围之外
     * 在itemView滑动离开屏幕之外后，在不超过缓存大小之后并不会调用此方法
     *  *  2. View在被detach Window之后，并不是永久的，在后续可能会重新attach上window
     *
     *
     *
     *
     *
     * 建议执行的操作:
     *  *  1. 缓存该视图，在后续新界面[.onCreateViewHolder]的时候进行复用，在一些重复打开场景下也是一种打开速度优化
     *
     *  *   2. 跟 [.onViewAttachedToWindow]成对实现一些注册与反注册监听，状态恢复与存储
     *
     *
     *
     * @param holder 正在被detach出window外的ViewHolder
     */
    fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
    }
}
