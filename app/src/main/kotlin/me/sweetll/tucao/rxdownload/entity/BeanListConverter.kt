package me.sweetll.tucao.rxdownload.entity

import com.raizlabs.android.dbflow.converter.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class BeanListConverter: TypeConverter<String, MutableList<DownloadBean>>() {
    private val adapter by lazy {
        val type = Types.newParameterizedType(MutableList::class.java, DownloadBean::class.java)
        Moshi.Builder().build()
                .adapter<MutableList<DownloadBean>>(type)
    }

    override fun getDBValue(model: MutableList<DownloadBean>?): String {
        if (model == null) {
            return "[]"
        } else {
            return adapter.toJson(model)
        }
    }

    override fun getModelValue(data: String?): MutableList<DownloadBean> {
        if (data == null) {
            return mutableListOf()
        } else {
            return adapter.fromJson(data)!!
        }
    }

}
