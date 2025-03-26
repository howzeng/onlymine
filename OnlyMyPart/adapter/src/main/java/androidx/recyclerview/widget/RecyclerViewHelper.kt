package androidx.recyclerview.widget

import android.view.View

object RecyclerViewHelper {

    fun getAllAttachedViewHolder(recyclerView: RecyclerView): LinkedHashSet<RecyclerView.ViewHolder> {
        val allAttachedViewHolder = LinkedHashSet<RecyclerView.ViewHolder>()
        for (i in 0 until recyclerView.childCount) {
            val itemView = recyclerView.getChildAt(i)
            (itemView.layoutParams as? RecyclerView.LayoutParams)?.mViewHolder?.let {
                allAttachedViewHolder.add(it)
            }
        }
        return allAttachedViewHolder
    }

    fun bindViewHolderToItemView(itemView: View, viewHolder: RecyclerView.ViewHolder) {
        (itemView.layoutParams as? RecyclerView.LayoutParams)?.mViewHolder = viewHolder
    }

    fun getOwnerRecyclerView(itemView: View): RecyclerView? {
        return (itemView.layoutParams as? RecyclerView.LayoutParams)?.mViewHolder?.mOwnerRecyclerView
    }
}