package com.onlymine.onlymypart.adapter.delegate

/**
 *
 *
 * 该类可以作为所有兜底的AdapterDelegate的基类，一般在兜底类中实现展示"不支持(UnSupported)"或者是"零高度的空白隐藏(Blank)"等视图样式，
 * 这样能保证应用在发布环境下面对数据异常也能获得较好的体验，而不是异常崩溃。
 * 后续考虑在 [AdapterDelegatesManager.setFallbackDelegate].限定参数为该类类型
 *
 */
abstract class AbsFallbackAdapterDelegate<T> : AdapterDelegate<T>() {
    /**
     * final实现，不需要业务定义重写，恒定返回true,作为兜底的AdapterDelegate
     * 如果不恒定返回true，在发布环境下可能会存在异常数据的情况，导致兜底策略失效。
     *
     * @param items Adapter的所有列表数据
     * @param position 给定的数据在items中的所在位置
     * @return true
     */
    override fun isForViewType(items: T, position: Int): Boolean {
        return true
    }
}
