package com.onlymine.part

import android.app.Activity
import android.os.Bundle

interface PartLifecycleCallbacks {
    fun onPartCreate(activity: Activity, savedInstanceState: Bundle?) {

    }

    fun onPartStart(activity: Activity) {

    }

    fun onPartResume(activity: Activity) {

    }

    fun onPartPause(activity: Activity) {

    }

    fun onPartStop(activity: Activity) {

    }

    fun onPartSaveInstanceState(activity: Activity, savedInstanceState: Bundle) {

    }

    fun onPartDestroy(activity: Activity) {

    }
}