package me.sweetll.tucao.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

class HorizontalDividerBuilder private constructor(private val context: Context) {

    private lateinit var divider: Drawable
    private var drawFirstItemTop: Boolean = false
    private var leftPadding: Int = 0
    private var rightPadding: Int = 0

    fun setDivider(@DrawableRes dividerRes: Int): HorizontalDividerBuilder {
        this.divider = ContextCompat.getDrawable(context, dividerRes)!!
        return this
    }

    fun setDrawFirstItemTop(drawFirstItemTop: Boolean): HorizontalDividerBuilder {
        this.drawFirstItemTop = drawFirstItemTop
        return this
    }

    fun setLeftPadding(leftPadding: Int): HorizontalDividerBuilder {
        this.leftPadding = leftPadding
        return this
    }

    fun setRightPadding(rightPadding: Int): HorizontalDividerBuilder {
        this.rightPadding = rightPadding
        return this
    }

    fun build(): HorizontalDivider {
        return HorizontalDivider(divider, drawFirstItemTop, leftPadding, rightPadding)
    }

    companion object {

        fun newInstance(context: Context): HorizontalDividerBuilder {
            return HorizontalDividerBuilder(context)
        }
    }
}
