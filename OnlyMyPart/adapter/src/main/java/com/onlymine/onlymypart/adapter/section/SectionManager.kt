package com.onlymine.onlymypart.adapter.section

import android.view.View
import android.view.ViewStub
import com.onlymine.onlyminedelegate.Impl.OnlyMyLog
import com.onlymine.onlymypart.adapter.VisibleAware

/**
 * 管理Section对象，进行Section对象的分发及onVisible,onDestroy等周期的派发
 *
 * @param <T> SectionManager的数据类型
</T> */
class SectionManager<T> : VisibleAware, ISectionHost {
    private val mSectionList = ArrayList<Section<T>>()
    private val mSectionViewStubMap = HashMap<Int, View>()

    /**
     * 初始化Section列表
     *
     * @param sectionClasses 需要初始化的Section类列表
     */
    fun registerSections(sectionClasses: List<Class<out Section<T>>>?) {
        if (sectionClasses == null) {
            OnlyMyLog.logInfo {
                "registerSections  sectionClasses == null"
            }
            return
        }
        for (sectionClass in sectionClasses) {
            try {
                val section = sectionClass.newInstance()
                if (!section.isSectionEnabled) {
                    continue
                }
                section.mSectionHost = this
                mSectionList.add(section)
            } catch (e: InstantiationException) {
                OnlyMyLog.logInfo {
                    e.message.toString()
                }
            } catch (e: IllegalAccessException) {
                OnlyMyLog.logInfo {
                    e.message.toString()
                }
            }
        }
    }

    /**
     * 由各个section进行itemView的初始化，常规的操作包含
     * 1. findViewById，对itemView的一些元素进行一些获取
     * 2. addView，往itemView上添加一些元素
     *
     * @param rootView itemView
     */
    fun initView(rootView: View) {
        for (section in mSectionList) {
            section.rootView = rootView
            initSectionStub(rootView, section)
            section.onInitView(rootView)
        }
    }

    /**
     * 初始化Section里指定ViewStub
     *
     * @param rootView itemView的根布局
     * @param section 待初始化操作的section对象
     */
    private fun initSectionStub(rootView: View, section: Section<*>) {
        if (section.viewStubLayoutId == null || section.viewStubLayoutId!!.size == 0) {
            return
        }
        for (stubLayoutId in section.viewStubLayoutId!!) {
            var sectionView = mSectionViewStubMap[stubLayoutId]
            if (sectionView == null) {
                val viewStub = rootView.findViewById<ViewStub>(stubLayoutId)
                if (viewStub != null) {
                    sectionView = viewStub.inflate()
                }
            }
            if (sectionView == null) {
                OnlyMyLog.logInfo {
                    "can't find find stub view with layoutId:$stubLayoutId"
                }
                return
            }
            mSectionViewStubMap[stubLayoutId] = sectionView
        }
    }

    /**
     * 为ViewHolder绑定数据
     */
    fun bindData(data: T, position: Int, payload: List<Any?>?) {
        for (section in mSectionList) {
            section.mData = data
            section.mPosition = position
            section.onBindData(data, position, payload)
        }
    }

    fun onViewRecycled() {
        for (section in mSectionList) {
            section.onViewRecycled()
        }
    }


    override fun onVisibleChanged(visible: Boolean) {
        for (section in mSectionList) {
            section.onVisibleChanged(visible)
        }
    }

    override fun onAttachedChanged(attached: Boolean) {
        for (section in mSectionList) {
            section.onAttachedChanged(attached)
        }
    }

    override fun onDestroy() {
        for (section in mSectionList) {
            section.onDestroy()
        }
    }

    override fun getStubView(viewStubId: Int): View {
        return mSectionViewStubMap[viewStubId]!!
    }

    companion object {
        private const val TAG = "SectionManager"
    }
}
