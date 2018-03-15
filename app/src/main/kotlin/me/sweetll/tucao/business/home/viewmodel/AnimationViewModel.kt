package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.home.fragment.AnimationFragment
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.raw.Animation
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class AnimationViewModel(val fragment: AnimationFragment): BaseViewModel() {
    val HID_PATTERN = "/play/h([0-9]+)/".toRegex()
    val TID_PATTERN = "/list/([0-9]+)/".toRegex()

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.list(19)
                .bindToLifecycle(fragment)
                .sanitizeHtml {
                    val recommends = parseRecommends(this)
                    Animation(recommends)
                }
                .doAfterTerminate { fragment.setRefreshing(false) }
                .subscribe({
                    animation ->
                    fragment.loadAnimation(animation)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    fragment.loadError()
                })
    }

    fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val title_red = doc.select("h2.title_red").takeLast(5)
        val lists_tip = doc.select("div.lists.tip").takeLast(5)
        val titleZipLists = title_red zip lists_tip

        val recommends = titleZipLists.fold(mutableListOf<Pair<Channel, List<Video>>>()) {
            total, zipElement ->
            // Parse Channel
            val aChannelElement = zipElement.first.child(1)
            val channelLinkUrl = aChannelElement.attr("href")
            val tid: Int = TID_PATTERN.find(channelLinkUrl)!!.groupValues[1].toInt()
            val channel = Channel.find(tid)!!

            // Parse List
            val ul = zipElement.second.child(0)
            val results = ul.children().filter {
                it is Element
            }.map {
                it.child(0)
            }.fold(mutableListOf<Video>()) {
                total, aElement ->
                // a
                val linkUrl = aElement.attr("href")
                val hid: String = HID_PATTERN.find(linkUrl)!!.groupValues[1]
                val thumb = aElement.child(0).attr("src")
                val title = aElement.child(1).text()
                val play = aElement.child(2).text().replace(",", "").toInt()
                total.add(Video(hid = hid, title = title, play = play, thumb = thumb))
                total
            }

            total.add(channel to results)
            total
        }

        //解析今日推荐
        val channel = Channel(0, "今日推荐")
        val pos8_show = doc.select("div.pos1_show").first()

        val ul = pos8_show.child(0)
        val results = ul.children().filter {
            it is Element
        }.map {
            it.child(0)
        }.fold(mutableListOf<Video>()) {
            total, aElement ->
            // a
            val linkUrl = aElement.attr("href")
            val hid: String = HID_PATTERN.find(linkUrl)!!.groupValues[1]
            val thumb = aElement.child(0).attr("src")
            val title = aElement.child(1).text()
            val play = aElement.child(2).text().replace(",", "").toInt()
            total.add(Video(hid = hid, title = title, play = play, thumb = thumb))
            total
        }
        recommends.add(0, channel to results)

        return recommends
    }

    fun onClickChannel(view: View) {
        ChannelDetailActivity.intentTo(fragment.activity!!, (view.tag as String).toInt())
    }
}
