package me.sweetll.tucao.extension

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object BlockListHelpers {
    private val BLOCK_LIST_FILE_NAME = "block_list"

    private val KEY_S_BLOCK_LIST = "block_list"
    private val KEY_B_BLOCK_ENABLE = "block_enable"

    private val adapter by lazy {
        val moshi = Moshi.Builder()
                .build()
        val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
        moshi.adapter<MutableList<String>>(type)
    }

    fun loadBlockList(): MutableList<String> {
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()

        val jsonString = sp.getString(KEY_S_BLOCK_LIST, "[\"http\"]") // 内置http
        return adapter.fromJson(jsonString)!!
    }

    private fun save(blockList: MutableList<String>) {
        val jsonString = adapter.toJson(blockList)
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()
        sp.edit {
            putString(KEY_S_BLOCK_LIST, jsonString)
        }
    }

    fun add(keyword: String) {
        val blockList = loadBlockList()
        blockList.remove(keyword)
        blockList.add(0, keyword)
        save(blockList)
    }

    fun remove(keyword: String) {
        val blockList = loadBlockList()
        blockList.remove(keyword)
        save(blockList)
    }

    fun isEnabled(): Boolean {
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()
        return sp.getBoolean(KEY_B_BLOCK_ENABLE, true)
    }

    fun setEnabled(enabled: Boolean) {
        val sp = BLOCK_LIST_FILE_NAME.getSharedPreference()
        sp.edit {
            putBoolean(KEY_B_BLOCK_ENABLE, enabled)
        }
    }

}
