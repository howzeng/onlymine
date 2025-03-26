package com.onlymine.onlymypart.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


/**
 *
 *
 * 对[AdapterDelegate] 在List数据类型下的简化中间层，通过泛型定义能够减少数据类型，ViewHolder等转换的模板代码
 *
 */
abstract class AbsListItemAdapterDelegate<I : T, T, VH : RecyclerView.ViewHolder> : AdapterDelegate<List<T>>() {
    override fun isForViewType(items: List<T>, position: Int): Boolean {
        // FixMe: Android 14偶现越界问题
        if (position >= items.size) {
            return false
        }
        return isForViewType(items[position], items, position)
    }

    /**
     * 判断当前的数据item是否由该类进行代理，一般有两种实现
     * 方式一：数据类型异化:
     * `protected boolean isForViewType(Animal item, List<Animal> items, position){
     * return item instanceof Cat;
     * }`
     * 方式二：数据内容异化:
     * `protected boolean isForViewType(Animal item, List<Animal> items, position){
     * return item.getType == Type.Cat;
     * }`
     *
     * @param item     给定位置的数据
     * @param items    整个adapter的数据源
     * @param position item在所有数据源中的位置
     * @return true则当前该AdapterDelegate将会代理后续的所有Adapter钩子方法
     */
    protected abstract fun isForViewType(item: T, items: List<T>, position: Int): Boolean

    override fun onBindViewHolder(
        items: List<T>, position: Int,
        holder: RecyclerView.ViewHolder, payloads: List<Any?>
    ) {
        onBindViewHolder(items[position] as I, holder as VH, position, payloads)
    }

    /**
     * 将数据item绑定到 [RecyclerView.ViewHolder]
     *
     * @param item     数据item
     * @param position 在列表中的位置
     * @param holder   目标viewHolder
     * @param payloads 局部刷新的差异数据
     */
    protected abstract fun onBindViewHolder(
        item: I, holder: VH, position: Int,
        payloads: List<Any?>
    )

    /**
     * 为当前的item数据创建 [RecyclerView.ViewHolder]，注意该ViewHolder可能在后续被复用，
     * 不会一直调用该方法进行创建
     *
     * @param parent itemView后续会挂载到的父ViewGroup
     * @return ViewHolder
     */
    abstract override fun onCreateViewHolder(parent: ViewGroup): VH
}
