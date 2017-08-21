package me.sweetll.tucao.extension

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import me.sweetll.tucao.AppApplication

fun String.logD(TAG: String = "FFF") {
    Log.d(TAG, this)
}

fun String.toast(length: Int = Toast.LENGTH_SHORT): Unit {
    Toast.makeText(AppApplication.get(), this, length).show()
}

fun String.decode(): String {
    var decodeString = this
    if (decodeString.contains("&amp;")) {
        decodeString = decodeString.replace("&amp;", "&")
    }
    if (decodeString.contains("&quot;")) {
        decodeString = decodeString.replace("&quot;", "\"")
    }
    if (decodeString.contains("&gt;")) {
        decodeString = decodeString.replace("&gt;", ">")
    }
    if (decodeString.contains("&lt;")) {
        decodeString = decodeString.replace("&lt;", "<")
    }
    return decodeString
}

fun String.getSharedPreference(): SharedPreferences = AppApplication.get().getSharedPreferences(this, 0)