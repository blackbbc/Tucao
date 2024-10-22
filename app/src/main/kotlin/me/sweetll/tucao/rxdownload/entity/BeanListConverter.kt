package me.sweetll.tucao.rxdownload.entity

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class BeanListConverter {
    private val adapter by lazy {
        val type = Types.newParameterizedType(MutableList::class.java, DownloadBean::class.java)
        Moshi.Builder().build()
                .adapter<MutableList<DownloadBean>>(type)
    }

    @TypeConverter
    fun getDBValue(model: MutableList<DownloadBean>?): String {
        return if (model == null) {
            "[]"
        } else {
            adapter.toJson(model)
        }
    }

    @TypeConverter
    fun getModelValue(data: String?): MutableList<DownloadBean> {
        if (data == null) {
            return mutableListOf()
        } else {
            return adapter.fromJson(data)!!
        }
    }

}
