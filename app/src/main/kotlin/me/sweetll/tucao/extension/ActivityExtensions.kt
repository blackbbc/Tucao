package me.sweetll.tucao.extension

import android.app.Activity
import android.view.inputmethod.InputMethodManager

fun Activity.hideSoftKeyboard() {
    val view = this.currentFocus
    view?.let {
        val imm = this.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}
