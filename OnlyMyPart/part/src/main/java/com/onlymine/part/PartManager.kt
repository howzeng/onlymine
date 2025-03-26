package com.onlymine.part

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.onlymine.part.interfaces.IPartHost

class PartManager(host: IPartHost?, root: View?) :
    PartLifecycleCallbacks {


    private var partHost: IPartHost? = host
    var rootView: View? = root
    private val stubView = mutableMapOf<Int, View>()
    private val partsMap = mutableMapOf<String, Part>()


    fun onBackEvent() {
        val partsList = partsMap.values
        for (part in partsList) {
            if (part.onBackEvent()) {
                return
            }
        }
    }

    public fun sendMessage(action: String, obj: Any) {
        val partsList = partsMap.values
        for (part in partsList) {
            part.sendMessage(action, obj)
        }
    }

    fun registPart(parts: List<Part>) {
        for (part in parts) {
            if (part.isPartEnable().not()) {
                continue
            }
            part.setEnvironment(partHost, rootView, this)
            val partName = part.javaClass.name
            if (partsMap.containsKey(partName)) {
                // todo
            } else {
                partsMap[partName] = part
            }
        }
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        val partsList = partsMap.values
        for (part in partsList) {

        }

    }


    fun getMessageFromPart(action: String, msg: Any?): Any? {
        val partsList = partsMap.values
        for (part in partsList) {
            val obj = part.handleDataRequest(action, msg)
            if (obj != null) {
                return obj
            }
        }
        return null
    }


    override fun onPartCreate(activity: Activity, savedInstanceState: Bundle?) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartCreate(activity, savedInstanceState)
            part.onInitView(rootView!!)
        }
    }

    override fun onPartStart(activity: Activity) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartStart(activity)
        }
    }

    override fun onPartResume(activity: Activity) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartResume(activity)
        }
    }

    override fun onPartPause(activity: Activity) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartPause(activity)
        }
    }

    override fun onPartStop(activity: Activity) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartStop(activity)
        }
    }

    override fun onPartDestroy(activity: Activity) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartDestroy(activity)
        }
        partsMap.clear()
        partHost = null
    }

    override fun onPartSaveInstanceState(activity: Activity, savedInstanceState: Bundle) {
        val partsList = partsMap.values
        for (part in partsList) {
            // todo 做一个reporter, 记录耗时
            val startCreateTime = System.currentTimeMillis()
            part.onPartSaveInstanceState(activity, savedInstanceState)
        }
    }

    fun <T : ViewModel> getViewModel(keyPrefix: String?, viewModelClass: Class<T>): T? {
        return partHost?.getViewModel(keyPrefix, viewModelClass)
    }
}