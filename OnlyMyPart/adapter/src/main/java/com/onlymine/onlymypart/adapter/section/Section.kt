package com.onlymine.onlymypart.adapter.section

import android.view.View
import com.onlymine.onlymypart.adapter.VisibleAware


/**
 * 对ItemView功能进行拆分的最小粒度单元，可以一个按钮功能的封装，也可以是一些数据上报逻辑的封装
 * @param <T> Section的数据来源类型，与容器类AbsItemAdapterDelegate一致
</T> */
abstract class Section<T> : VisibleAware {
    /**
     * Section对应的宿主
     */
    var mSectionHost: ISectionHost? = null

    /**
     * Section对应的itemView的根布局，数据，在Adapter中的位置
     */
    var rootView: View? = null
    var mData: T? = null
    var mPosition: Int = 0

    val logTag: String
        /**
         * 获取Section的日志tag，默认为类名
         *
         * @return Section的日志tag
         */
        get() = javaClass.name

    val isSectionEnabled: Boolean
        /**
         * 判断Section是否可以用
         *
         * @return true是Section可用，当返回false时，Section将不会挂载到SectionManager中
         */
        get() = true

    /**
     * Section在使用上为组装使用，设计上对layout的设计也是按区域进行ViewStub处理，因此一个Section在功能上对应的可能有一个或者多个
     * ViewStub，因此提供此方法，由子类实现获取该Section依赖的viewStub id，在该Section初始化之前会进行inflateStub，确保在
     * onInitView调用的时候ViewStub已经被inflate完成了
     *
     * @return 该Section依赖的viewStub id
     */
    abstract val viewStubLayoutId: IntArray?

    /**
     * Section初始化View的位置
     *
     * @param containerView itemView的容器
     */
    abstract fun onInitView(containerView: View?)

    /**
     * Section进行数据绑定的位置
     *
     * @param data itemData
     * @param position itemView在adapter中的位置
     * @param payload payload差异化数据
     */
    abstract fun onBindData(data: T, position: Int, payload: List<Any?>?)

    /**
     * 当Visible改变的时候会调用此方法
     *
     * @param visible true时该itemView可见，反之不可见
     */
    override fun onVisibleChanged(visible: Boolean) {
    }

    /**
     * 当attach状态改变的时候会调用此方法。
     * 注意这个不等同于visible。一般来说attach不一定是visible（像ViewPager2设置了offPageLimit的场景），
     * 但是detach一般是inVisible，当然存在change动画的场景下，发生快速的attach和detach切换时候，visible也会快速的调用两次切换。
     *
     * @param attached true时该itemView挂载在Window上
     */
    override fun onAttachedChanged(attached: Boolean) {
    }

    /**
     * 当Section所在的containerView销毁的时候调用此方法，可以在此时机释放一些资源
     */
    override fun onDestroy() {
    }

    fun onViewRecycled() {
    }

    /**
     * 通过viewStubId获取对对应的初始化后的view
     *
     * @param viewStubId
     * @return
     */
    fun getStubView(viewStubId: Int): View {
        return mSectionHost!!.getStubView(viewStubId)
    }
}
