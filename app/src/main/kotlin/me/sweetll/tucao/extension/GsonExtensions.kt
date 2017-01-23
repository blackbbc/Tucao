package me.sweetll.tucao.extension

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

fun <T> Gson.fromListJson(jsonString: String, type: Class<T>): MutableList<T> {
    val typeToken = ListParameterizedType(type)
    return fromJson(jsonString, typeToken)
}

class ListParameterizedType(val type: Type): ParameterizedType {

    override fun getRawType(): Type = ArrayList::class.java

    override fun getOwnerType(): Type? = null

    override fun getActualTypeArguments(): Array<out Type> = arrayOf(type)

}
