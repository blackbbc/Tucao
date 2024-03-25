package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.business.home.fragment.GameFragment
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.raw.Game
import me.sweetll.tucao.util.parseChannelList
import org.jsoup.nodes.Document

class GameViewModel(val fragment: GameFragment): BaseViewModel() {

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.list(21)
                .bindToLifecycle(fragment)
                .sanitizeHtml {
                    val recommends = parseRecommends(this)
                    Game(recommends)
                }
                .doAfterTerminate { fragment.setRefreshing(false) }
                .subscribe({
                    game ->
                    fragment.loadGame(game)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    fragment.loadError()
                })
    }

    fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val listParentNode = doc.getElementById("loop_num")
        val recommends = parseChannelList(listParentNode).toMutableList()

        //解析今日推荐
//        val channel = Channel(0, "今日推荐")
//        val list8 = doc.getElementsByClass("list_hot").first()
//        val videos = parseListVideo(list8)
//        recommends.add(0, channel to videos)

        return recommends
    }

    fun onClickChannel(view: View) {
        ChannelDetailActivity.intentTo(fragment.activity!!, (view.tag as String).toInt())
    }
}
