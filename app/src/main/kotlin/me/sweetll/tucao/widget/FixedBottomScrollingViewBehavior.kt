package me.sweetll.tucao.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import me.sweetll.tucao.R

/**
 * Created by Sweet on 2018/3/20.
 */
class FixedBottomScrollingViewBehavior(val context: Context, attrs: AttributeSet): AppBarLayout.ScrollingViewBehavior(context, attrs) {
    private var fabOffsetHelper: ViewOffsetHelper? = null
    private var containerOffsetHelper: ViewOffsetHelper? = null

    private var offset1: Int = 0
    private var offset2: Int = 0

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        super.onLayoutChild(parent, child, layoutDirection)

        if (fabOffsetHelper == null) {
            val fab: View = child.findViewById(R.id.commentFab)
            val container: View = child.findViewById(R.id.commentContainer)
            val tab: View = child.findViewById(R.id.tab)

            val headerHeight = tab.height + (context.resources.displayMetrics.density * 0.5f).toInt()

            val lp = fab.layoutParams as FrameLayout.LayoutParams
            val margin = lp.bottomMargin

            offset1 = parent.height - headerHeight - container.height
            offset2 = parent.height - headerHeight - fab.height - margin

            containerOffsetHelper = ViewOffsetHelper(container)
            fabOffsetHelper = ViewOffsetHelper(fab)
        }

        containerOffsetHelper!!.setTopAndBottomOffset(offset1 - child.top)
        fabOffsetHelper!!.setTopAndBottomOffset(offset2 - child.top)

        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val offset = dependency.bottom - child.top
        ViewCompat.offsetTopAndBottom(child, offset)

        containerOffsetHelper!!.setTopAndBottomOffset(offset1 - child.top)
        fabOffsetHelper!!.setTopAndBottomOffset(offset2 - child.top)

        return false
    }

    class ViewOffsetHelper(val view: View) {
        fun setTopAndBottomOffset(offset: Int) {
            if (offset != view.top)
            ViewCompat.offsetTopAndBottom(view, offset - view.top)
        }
    }

}