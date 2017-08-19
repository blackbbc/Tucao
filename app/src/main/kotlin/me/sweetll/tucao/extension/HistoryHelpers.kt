package me.sweetll.tucao.extension

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.sweetll.tucao.model.json.Video

object HistoryHelpers {
    private val HISTORY_FILE_NAME = "history"

    private val KEY_S_SEARCH_HISTORY ="search_history"
    private val KEY_S_PLAY_HISTORY = "play_history"
    private val KEY_S_STAR = "star"

    private val adapter by lazy {
        val moshi = Moshi.Builder()
                .build()
        val type = Types.newParameterizedType(MutableList::class.java, Video::class.java)
        moshi.adapter<MutableList<Video>>(type)
    }

    private fun loadHistory(fileName: String, key: String): MutableList<Video> {
        val sp = fileName.getSharedPreference()

        val jsonString = sp.getString(key, "[]")
        return adapter.fromJson(jsonString)!!
    }

    fun loadSearchHistory(): MutableList<Video> = loadHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY)

    fun loadPlayHistory(): MutableList<Video> = loadHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY)

    fun loadStar(): MutableList<Video> = loadHistory(HISTORY_FILE_NAME, KEY_S_STAR)

    private fun saveHistory(fileName: String, key: String, video: Video) {
        val histories = loadHistory(fileName, key)
        if (key == KEY_S_SEARCH_HISTORY) {
            histories.removeAll {
                it.title == video.title
            }
        } else {
            histories.removeAll {
                it.hid == video.hid
            }
        }
        histories.add(0, video)
        val jsonString = adapter.toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
    }

    fun saveSearchHistory(video: Video) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, video)
    }

    fun savePlayHistory(video: Video) {
        val histories = loadHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY)
        val existVideo = histories.find { it.hid == video.hid }
        if (existVideo != null) {
            video.parts = video.parts.plus(existVideo.parts).distinctBy { it.vid }.toMutableList()
        }
        saveHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, video)
    }

    fun saveStar(video: Video) {
        saveHistory(HISTORY_FILE_NAME, KEY_S_STAR, video)
    }

    private fun removeHistory(fileName: String, key: String, video: Video): Int {
        val histories = loadHistory(fileName, key)
        val removedIndex = histories.indexOf(video)
        histories.remove(video)

        val jsonString = adapter.toJson(histories)
        val sp = fileName.getSharedPreference()
        sp.edit {
            putString(key, jsonString)
        }
        return removedIndex
    }

    fun removeSearchHistory(video: Video) = removeHistory(HISTORY_FILE_NAME, KEY_S_SEARCH_HISTORY, video)

    fun removePlayHistory(video: Video) = removeHistory(HISTORY_FILE_NAME, KEY_S_PLAY_HISTORY, video)

    fun removeStar(video: Video) = removeHistory(HISTORY_FILE_NAME, KEY_S_STAR, video)
}
