package me.sweetll.tucao.extension

import com.google.gson.Gson
import me.sweetll.tucao.model.json.Result
import com.github.salomonbrys.kotson.*

object HistoryHelpers {
    private val HISTORY_FILE_NAME = "history"

    private val KEY_S_SEARCH_HISTORY ="search_history"
    private val KEY_S_PLAY_HISTORY = "play_history"

    private fun loadHistory(fileName: String, key: String): MutableList<Result> {
        val sp = fileName.getSharedPreference()
        val jsonString = sp.getString(key, "[]")
        return Gson().fromJson<List<Result>>(jsonString).toMutableList()
    }

    fun loadSearchHistory(): MutableList<Result> = loadHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY)

    fun loadPlayHistory(): MutableList<Result> = loadHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY)

    private fun saveHistory(fileName: String, key: String, result: Result) {
        val histories = loadHistory(fileName, key)
        histories.removeAll {
            it == result
        }
        histories.add(0, result)
        val jsonString = Gson().toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
    }

    fun saveSearchHistory(result: Result) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, result)
    }

    fun savePlayHistory(result: Result) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, result)
    }

    private fun removeHistory(fileName: String, key: String, result: Result): Int {
        val histories = loadHistory(fileName, key)
        val removedIndex = histories.indexOf(result)
        histories.remove(result)
        val jsonString = Gson().toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
        return removedIndex
    }

    fun removeSearchHistory(result: Result) = removeHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, result)

    fun removePlayHistory(result: Result) = removeHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, result)
}
