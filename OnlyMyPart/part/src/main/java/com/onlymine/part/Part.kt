package com.onlymine.part

import android.app.Activity
import android.view.View
import com.onlymine.part.interfaces.IPartHost

abstract class Part : PartLifecycleCallbacks {

    private var rootView: View? = null
    private var partManager: PartManager? = null
    private var hostActivity: Activity? = null
    private var partHost: IPartHost? = null


    /**
     * 用于控制 part 是否可用的开关
     */
    fun isPartEnable(): Boolean = true

    public fun getLogTag() = javaClass.name

    public fun handleMessage(action: String, obj: Any) {

    }

    public fun onInitView(view: View) {

    }

    fun setEnvironment(host: IPartHost?, rootView: View, partManager: PartManager) {
        this@Part.partManager = partManager
        this@Part.partHost = host
        this@Part.rootView = rootView
    }

    public fun sendMessage(action: String, obj: Any) {
        partManager?.sendMessage(action, obj)
    }


    fun handleDataRequest(action: String, msg: Any?): Any? {
        return null
    }

    fun onBackEvent(): Boolean {
        return false
    }

}