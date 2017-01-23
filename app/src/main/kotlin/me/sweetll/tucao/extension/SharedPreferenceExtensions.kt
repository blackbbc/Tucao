package me.sweetll.tucao.extension

import android.content.SharedPreferences
import com.google.gson.Gson
import me.sweetll.tucao.model.json.Result

private val HISTORY_FILE_NAME = "history"

private val KEY_S_SEARCH_HISTORY ="search_history"
private val KEY_S_PLAY_HISTORY = "play_history"

fun SharedPreferences.edit(func: SharedPreferences.Editor.() -> Unit) {
    val editor = edit()
    editor.func()
    editor.commit()
}

object HistoryHelpers {

    private fun loadHistory(fileName: String, key: String): MutableList<Result> {
        val sp = fileName.getSharedPreference()
        val jsonString = sp.getString(key, "[]")
        return Gson().fromListJson(jsonString, Result::class.java)
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
}