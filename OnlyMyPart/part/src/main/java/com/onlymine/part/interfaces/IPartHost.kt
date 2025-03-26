package com.onlymine.part.interfaces

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.onlymine.part.Part

interface IPartHost {

    /**
     * 拿到宿主的context
     */
    fun getHostContext(): Context?


    /**
     * 获取宿主的activity
     */
    fun getHostActivity(): Activity?

    /**
     * 获取viewModelStoreOwner对象,用于Part内获取viewModel
     */
    fun getViewModelStoreOwner(): ViewModelStoreOwner


    /**
     * 获取Part生命周期所在的lifecycle，如果宿主是fragment，返回的应该是viewLifecycle
     */
    fun getHostLifecycleOwner(): LifecycleOwner


    fun assembleParts(): List<Part>


    fun <T : ViewModel> getViewModel(keyPrefix: String?, viewModelClass: Class<T>): T {
        val prefix = if (keyPrefix.isNullOrEmpty()) {
            viewModelClass.canonicalName
        } else {
            keyPrefix + viewModelClass.canonicalName
        }
        return ViewModelProvider(getViewModelStoreOwner())[prefix, viewModelClass]
    }
}