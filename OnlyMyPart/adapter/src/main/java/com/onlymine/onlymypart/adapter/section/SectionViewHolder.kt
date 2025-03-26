package com.onlymine.onlymypart.adapter.section


import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.onlymine.onlymypart.adapter.VisibleAware
import com.onlymine.onlymypart.adapter.delegate.AdapterDelegate

/**
 *
 *
 * 桥接[AdapterDelegate] 和 [SectionManager] 的管理类
 *
 *
 *
 * 同时扩展了[VisibleAware]接口，使得ViewHolder能够感知到其"可见与否"的状态变化，方便业务在此实现功能逻辑
 * 具体实现与分发可参考 [com.tencent.biz.richframework.part.adapter.VisibleNotifyAdapter]
 *
 * @param <T> ViewHolder的数据类型
</T> */
class SectionViewHolder<T>(itemView: View, sections: List<Class<out Section<T>>>) :
    RecyclerView.ViewHolder(itemView), VisibleAware {
    private val mSectionManager = SectionManager<T>()

    init {
        mSectionManager.registerSections(sections)
        mSectionManager.initView(itemView)
    }

    fun bindData(data: T, position: Int, payload: List<Any?>?) {
        mSectionManager.bindData(data, position, payload)
    }

    fun onViewRecycled() {
        mSectionManager.onViewRecycled()
    }

    override fun onDestroy() {
        mSectionManager.onDestroy()
    }

    override fun onVisibleChanged(visible: Boolean) {
        mSectionManager.onVisibleChanged(visible)
    }

    override fun onAttachedChanged(attached: Boolean) {
        mSectionManager.onAttachedChanged(attached)
    }
}
