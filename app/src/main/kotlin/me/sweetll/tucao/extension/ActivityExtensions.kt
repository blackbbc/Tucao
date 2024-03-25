package me.sweetll.tucao.extension

import android.app.Activity
import android.content.Intent
import android.view.inputmethod.InputMethodManager

fun Activity.hideSoftKeyboard() {
    val view = this.currentFocus
    view?.let {
        val imm =
            this.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

inline fun <reified T : Activity> Activity.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivity(vararg pair: Pair<String, String>) {
    startActivity<T> {
        pair.forEach {
            putExtra(it.first, it.second)
        }
    }
}