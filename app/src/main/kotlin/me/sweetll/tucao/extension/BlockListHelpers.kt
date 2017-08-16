package me.sweetll.tucao.extension

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object BlockListHelpers {
    private val BLOCK_LIST_FILE_NAME = "block_list"

    private val KEY_S_BLOCK_LIST = "block_list"

    private val adapter by lazy {
        val moshi = Moshi.Builder()
                .build()
        val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
        moshi.adapter<MutableList<String>>(type)
    }

    fun loadBlockList(): MutableList<String> {
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()

        val jsonString = sp.getString(KEY_S_BLOCK_LIST, "[]")
        return adapter.fromJson(jsonString)!!
    }

    fun saveBlockList(keyword: String) {
        val blockList = loadBlockList()
        blockList.removeAll {
            it == keyword
        }
        blockList.add(0, keyword)
        val jsonString = adapter.toJson(blockList)
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()
        sp.edit {
            putString(KEY_S_BLOCK_LIST, jsonString)
        }
    }

}
