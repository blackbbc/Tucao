package me.sweetll.tucao.extension

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

fun <T> Gson.fromListJson(jsonString: String): MutableList<T> {
    val typeToken = object : TypeToken<ArrayList<T>>() {}.type
    return fromJson(jsonString, typeToken)
}
