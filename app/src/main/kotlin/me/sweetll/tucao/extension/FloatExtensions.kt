package me.sweetll.tucao.extension

import android.util.TypedValue
import me.sweetll.tucao.AppApplication

fun Float.isPercentageNumber() = this >= 0f && this <= 1f

fun Float.dp2px(): Float {
    val r = AppApplication.get().resources
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, r.displayMetrics)
}