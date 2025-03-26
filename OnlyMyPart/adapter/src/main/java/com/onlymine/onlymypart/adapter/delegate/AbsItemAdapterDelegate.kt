package com.onlymine.onlymypart.adapter.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.RecyclerViewHelper.bindViewHolderToItemView
import com.onlymine.onlymypart.adapter.section.Section
import com.onlymine.onlymypart.adapter.section.SectionViewHolder


/**
 *
 *
 * 该类是通过对AbsItemAdapterDelegate封装"Section"(区域级复用方案)的中间层抽象类。如果不需要使用Section，可以直接使用父类。
 *
 *
 *
 * 设计背景：虽然通过AbsListItemAdapterDelegate实现了不同类型ItemView之间的逻辑隔离，我们可以把不同的数据类型（继承同一个父数据类型）
 * 定义不同的AdapterDelegate来实现分离。（当然这个数据类型也可以是相同的，最后都会走到 [.isForViewType] 来进行区分。）
 * 但是对于一些拥有复杂逻辑的ItemView来说，不同ItemView存在着逻辑的复用以及异化。因此我们需要对其进行更进一步的功能拆分，
 * 这里我们引入一个'Section'的概念来对ItemView进行功能的拆解，通过复用Section的方式来实现ItemView之间的功能复用，
 * 具体的介绍可以参考 [Section]
 *
 *
 * @param <I> 该AdapterDelegate所"负责"的数据类型，必须为列表基础数据类型或者其子类
 * @param <T> 列表所定义的基础数据类型
</T></I> */
abstract class AbsItemAdapterDelegate<I : T, T> :
    AbsListItemAdapterDelegate<I, T, SectionViewHolder<I>>() {
    abstract override fun isForViewType(item: T, items: List<T>, position: Int): Boolean

    /**
     * 构造Section的宿主容器ViewHolder
     *
     * @param parent itemView的父容器(由RecycleView内部创建提供）
     * @return 已经添加sections完毕的SectionViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup): SectionViewHolder<I> {
        val sections: List<Class<out Section<I>?>> = ArrayList()
        val itemView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        initSection(parent, itemView, sections)
        return SectionViewHolder(itemView, sections)
    }

    /**
     * 这里需要拆分onBind功能
     *
     * @param item The data item
     * @param holder The ViewHolder
     * @param position The position
     * @param payloads The payloads
     */
    override fun onBindViewHolder(
        item: I, holder: SectionViewHolder<I>, position: Int,
        payloads: List<Any?>
    ) {
        // 提前把viewHolder.itemView与viewHolder进行关联
        bindViewHolderToItemView(holder.itemView, holder)
        holder.bindData(item, position, payloads)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is SectionViewHolder<*>) {
            (holder as SectionViewHolder<*>).onViewRecycled()
        }
    }

    /***
     * 获取itemView的布局id，由子类重写实现，在此类中进行inflate创建
     * @return R.layout类型的资源
     */
    abstract val layoutId: Int

    /**
     * 对Sections列表对象进行组装，子类通过往sections中进行添加逻辑处理section [Section]，来实现功能逻辑拆分
     * @param parent itemView后续会挂载的parent
     * @param itemView 当前通过getLayoutId创建的itemView
     * @param sections 该类型item包含的所有sections处理对象
     */
    abstract fun initSection(
        parent: ViewGroup, itemView: View,
        sections: List<Class<out Section<I>?>>
    )
}
