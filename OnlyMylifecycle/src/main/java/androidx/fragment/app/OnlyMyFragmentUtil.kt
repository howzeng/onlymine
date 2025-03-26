package androidx.fragment.app

import android.view.View

object OnlyMyFragmentUtil {

    fun findViewFragment(view: View): Fragment? {
        return findViewFragmentInner(view)
    }

    private fun findViewFragmentInner(view: View): Fragment? {
        getViewFragment(view)?.let {
                return it
        }
        if (view.parent is View) {
           return findViewFragmentInner(view.parent as View)
        }
        return null
    }

    private fun getViewFragment(view: View): Fragment? {
        return FragmentManager.getViewFragment(view)
    }
}