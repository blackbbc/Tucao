package me.sweetll.tucao.extension

import android.util.Log
import android.widget.Toast
import me.sweetll.tucao.AppApplication

fun String.logD(): Unit {
    Log.d("FFF", this)
}

fun String.toast(length: Int = Toast.LENGTH_SHORT): Unit {
    Toast.makeText(AppApplication.get(), this, length).show()
}