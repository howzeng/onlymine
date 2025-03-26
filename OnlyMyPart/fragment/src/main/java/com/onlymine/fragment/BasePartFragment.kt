package com.onlymine.fragment

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import com.onlymine.part.Part
import com.onlymine.part.PartManager
import com.onlymine.part.interfaces.IPartHost

abstract class BasePartFragment : Fragment(), IPartHost {


    private var contentView: View? = null
    protected var parentView: ViewGroup? = null

    protected var partManager: PartManager? = null


    private val innerFragmentLifecycleCallbacks = object : FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            /**
             * 由于基类设计问题,子类可以重写onCreateView，因此，需要将所有Part的初始化逻辑迁移到onViewCreated中
             * 因此 doOnCreateView 的方法中不能保证 contentView已经被赋值，所以提供
             */
            super.onFragmentViewCreated(fm, f, v, savedInstanceState)

            if (f == this@BasePartFragment) {
                contentView = v
                if (contentView?.parent is ViewGroup) {
                    parentView = contentView?.parent as ViewGroup
                }
                val allParts = mutableListOf<Part>()
                val assembleParts = assembleParts()

                if (assembleParts.isNotEmpty()) {
                    allParts.addAll(assembleParts)
                }

                if (allParts.isNotEmpty()) {
                    partManager = getPartManager()
                    partManager?.registPart(allParts)
                }

                dispatchPartInit(v, savedInstanceState)
            }
        }

        override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
            super.onFragmentViewDestroyed(fm, f)
            if (f == this@BasePartFragment) {
                dispatchPartDestroy()
            }
        }
    }


    private fun dispatchPartInit(contentView: View, saveInstanceState: Bundle?) {
        onViewCreatedBeforePartInit(contentView, saveInstanceState)
        partManager?.apply {
            rootView = contentView
            onPartCreate(requireActivity(), saveInstanceState)
        }
        onViewCreatedAfterPartInit(contentView, saveInstanceState)
    }

    fun getFragmentContentView(): View? {
        return contentView
    }






    private fun dispatchPartDestroy() {
        partManager?.onPartDestroy(requireActivity())
        partManager = null
        onFragmentViewDestroyed()
    }

    /**
     * fragment调用过onViewDestroy之后触发的钩子方法，此时已经调用过partManager等一系列的destroy方法，如果有其他操作诉求
     * 可以在此钩子方法中操作
     */
    open fun onFragmentViewDestroyed() {

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  super.onCreateView(inflater, container, savedInstanceState)
        if (view == null || getContentId() != 0) {
            view = inflater.inflate(getContentId(), container, false)
        }
        return view
    }

    abstract fun getContentId(): Int


    override fun onDetach() {
        super.onDetach()
        parentFragmentManager.unregisterFragmentLifecycleCallbacks(innerFragmentLifecycleCallbacks)
    }

    fun getPartManager(): PartManager {
        if (partManager == null) {
            partManager = PartManager(this, contentView)
        }
        return partManager!!
    }


    override fun getHostLifecycleOwner() = viewLifecycleOwner


    /**
     * fraqment 调用onViewCreated中，这里的钩子方法在partManager分发part初始化之前，这里在此方法中进行一些准备工作
     * 例如ioc对象注入，在part初始化的时候即可获取到这些ioc对象
     */
    open fun onViewCreatedBeforePartInit(contentView: View, saveInstanceState: Bundle?) {

    }

    /**
     * fraqment 调用onViewCreated中，这里的钩子方法在partManager分发part初始化之后，这里在此方法中进行一些准备工作
     * 例如数据填充，part内部view的设置等
     */
    open fun onViewCreatedAfterPartInit(contentView: View, saveInstanceState: Bundle?) {

    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            doStart(it)
        }
    }

    private fun doStart(activity: Activity) {
        partManager?.onPartStart(activity)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            doResume(it)
        }
    }

    private fun doResume(activity: Activity) {
        partManager?.onPartResume(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            doPause(it)
        }
    }

    private fun doPause(activity: Activity) {
        partManager?.onPartPause(activity)
    }

    override fun onStop() {
        super.onStop()
        activity?.let {
            doStop(it)
        }
    }

    private fun doStop(activity: Activity) {
        partManager?.onPartStop(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        partManager?.onConfigurationChanged(newConfig)
    }

    override fun getHostActivity() = activity

    override fun getHostContext() = context
}