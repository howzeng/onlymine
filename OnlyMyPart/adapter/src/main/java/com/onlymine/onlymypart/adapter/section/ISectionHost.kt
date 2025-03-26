package com.onlymine.onlymypart.adapter.section

import android.view.View

interface ISectionHost {

    /**
     * 获取itemView中StubId对应的布局
     */

    fun getStubView(viewStubId: Int): View
}