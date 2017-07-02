package me.sweetll.tucao.extension

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.sweetll.tucao.model.json.Result

object HistoryHelpers {
    private val HISTORY_FILE_NAME = "history"

    private val KEY_S_SEARCH_HISTORY ="search_history"
    private val KEY_S_PLAY_HISTORY = "play_history"
    private val KEY_S_STAR = "star"

    private val adapter by lazy {
        val moshi = Moshi.Builder()
//                .add(KotlinJsonAdapterFactory())
                .build()
        val type = Types.newParameterizedType(MutableList::class.java, Result::class.java)
        moshi.adapter<MutableList<Result>>(type)
    }

    private fun loadHistory(fileName: String, key: String): MutableList<Result> {
        val sp = fileName.getSharedPreference()

        val jsonString = sp.getString(key, "[]")
        return adapter.fromJson(jsonString)!!
    }

    fun loadSearchHistory(): MutableList<Result> = loadHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY)

    fun loadPlayHistory(): MutableList<Result> = loadHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY)

    fun loadStar(): MutableList<Result> = loadHistory(HISTORY_FILE_NAME, KEY_S_STAR)

    private fun saveHistory(fileName: String, key: String, result: Result) {
        val histories = loadHistory(fileName, key)
        if (key == KEY_S_SEARCH_HISTORY) {
            histories.removeAll {
                it.title == result.title
            }
        } else {
            histories.removeAll {
                it.hid == result.hid
            }
        }
        histories.add(0, result)
        val jsonString = adapter.toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
    }

    fun saveSearchHistory(result: Result) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, result)
    }

    fun savePlayHistory(result: Result) {
        val histories = loadHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY)
        val existResult = histories.find { it.hid == result.hid }
        if (existResult != null) {
            result.video = result.video.plus(existResult.video).distinctBy { it.vid }.toMutableList()
        }
        saveHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, result)
    }

    fun saveStar(result: Result) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_STAR, result)
    }

    private fun removeHistory(fileName: String, key: String, result: Result): Int {
        val histories = loadHistory(fileName, key)
        val removedIndex = histories.indexOf(result)
        histories.remove(result)

        val jsonString = adapter.toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
        return removedIndex
    }

    fun removeSearchHistory(result: Result) = removeHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, result)

    fun removePlayHistory(result: Result) = removeHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, result)

    fun removeStar(result: Result) = removeHistory(HISTORY_FILE_NAME, KEY_S_STAR, result)
}
