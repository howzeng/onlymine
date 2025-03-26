package com.onlymine.onlyminedelegate.Impl

import com.onlymine.onlyminedelegate.ILogDelegate

class OnlyMyLog {

    companion object {
        val logImpl: ILogDelegate? = null
        val logLevel = 0

        inline fun logInfo(tag: String = "TAG", crossinline block:() -> String) {
            logImpl?.d(tag, logLevel, block())
        }
    }

}