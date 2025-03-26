package com.onlymine.onlymypart.adapter

import android.content.ContextWrapper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.OnlyMyFragmentUtil
import androidx.lifecycle.LifecycleOwner
import com.onlymine.onlyminedelegate.Impl.OnlyMyLog.Companion.logInfo

object ViewLifecycleUtil {

    const val TAG = "ViewLifecycleUtil"
    /**
     * 获取一个View最临近的Lifecycle
     *
     * @param view 目标View
     * @return
     */
    @JvmStatic
    fun getViewLifecycleOwner(view: View?): LifecycleOwner? {
        if (view == null) {
            logInfo(tag = TAG) {
                "getViewLifecycleOwner but find view is empty"
            }
            return null
        }
        var lifecycleOwner: LifecycleOwner? = null
        val hostFragment: Fragment? = OnlyMyFragmentUtil.findViewFragment(view)
        if (hostFragment != null) {
            try {
                lifecycleOwner = hostFragment.viewLifecycleOwner
            } catch (e: IllegalStateException) {
                logInfo(TAG) {
                    "getViewLifecycleOwner error:$e"
                }
            }
        } else if (view.context is LifecycleOwner) {
            lifecycleOwner = view.context as LifecycleOwner
        } else if (view.context is ContextWrapper) {
            val baseContext = (view.context as ContextWrapper).baseContext
            if (baseContext.applicationContext != null && baseContext is LifecycleOwner) {
                lifecycleOwner = baseContext as ContextWrapper as LifecycleOwner
            }
        }
        return lifecycleOwner
    }


}