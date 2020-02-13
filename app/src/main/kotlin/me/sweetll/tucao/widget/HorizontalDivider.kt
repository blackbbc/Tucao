package me.sweetll.tucao.widget

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class HorizontalDivider(private val divider: Drawable, private val drawFirstItemTop: Boolean,
                        private val leftPadding: Int, private val rightPadding: Int) : RecyclerView.ItemDecoration() {

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            //获得child的布局信息
            val params = child.layoutParams as RecyclerView.LayoutParams
            var top = child.bottom + params.bottomMargin
            var bottom = top + divider.intrinsicHeight
            divider.setBounds(left + leftPadding, top, right - rightPadding, bottom)
            divider.draw(canvas)
            if (drawFirstItemTop && i == 1) {
                bottom = child.top + params.topMargin
                top = bottom - divider.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = divider.intrinsicHeight
        if (drawFirstItemTop && parent.getChildAdapterPosition(view) == 0) {
            outRect.top = divider.intrinsicHeight
        }
    }

}
