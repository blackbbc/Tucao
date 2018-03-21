package me.sweetll.tucao.widget

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import me.sweetll.tucao.R

/**
 * Created by Sweet on 2018/3/20.
 */
class FixedBottomScrollingViewBehavior(context: Context, attrs: AttributeSet): AppBarLayout.ScrollingViewBehavior(context, attrs) {
    private lateinit var commentFab: View
    private lateinit var commentContainer: View

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        super.onLayoutChild(parent, child, layoutDirection)

        commentFab = child.findViewById(R.id.commentFab)
        commentContainer = child.findViewById(R.id.commentContainer)

        val offset1 = parent.height - child.top - commentContainer.height
        ViewCompat.offsetTopAndBottom(commentContainer, offset1)

        val lp = commentFab.layoutParams as FrameLayout.LayoutParams
        val offset2 = parent.height - child.top - commentFab.height - lp.topMargin - lp.bottomMargin
        ViewCompat.offsetTopAndBottom(commentFab, offset2)

        return true
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val offset = dependency.bottom - child.top
        ViewCompat.offsetTopAndBottom(child, offset)

        ViewCompat.offsetTopAndBottom(commentContainer, -offset)
        ViewCompat.offsetTopAndBottom(commentFab, -offset)

        return false
    }
}