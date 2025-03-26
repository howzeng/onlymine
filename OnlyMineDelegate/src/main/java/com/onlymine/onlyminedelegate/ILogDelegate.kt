package com.onlymine.onlyminedelegate

interface ILogDelegate {

    fun i(tag: String, level: Int, msg: String)

    fun w(tag: String, level: Int, msg: String)

    fun d(tag: String, level: Int, msg: String)

    fun e(tag: String, level: Int, msg: String)

    fun isColorLevel(): Boolean

    fun isDevelopLevel(): Boolean
}