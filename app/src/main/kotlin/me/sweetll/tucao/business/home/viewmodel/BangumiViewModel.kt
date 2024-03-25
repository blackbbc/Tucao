package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.business.home.fragment.BangumiFragment
import me.sweetll.tucao.business.showtimes.ShowtimeActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.raw.Bangumi
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.util.parseBanner
import me.sweetll.tucao.util.parseChannelList
import org.jsoup.nodes.Document

class BangumiViewModel(val fragment: BangumiFragment): BaseViewModel() {

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.list(24)
                .bindToLifecycle(fragment)
                .sanitizeHtml {
                    val banners = parseBanners(this)
                    val recommends = parseRecommends(this)
                    Bangumi(banners, recommends)
                }
                .doAfterTerminate { fragment.setRefreshing(false) }
                .subscribe({
                    bangumi ->
                    fragment.loadBangumi(bangumi)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    fragment.loadError()
                })
    }

    fun parseBanners(doc: Document): List<Banner> {
        val slides = doc.select("div.slide")
        return parseBanner(slides)
    }

    fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val listParentNode = doc.getElementById("loop_num")
        return parseChannelList(listParentNode)
    }

    fun onClickChannel(view: View) {
        ChannelDetailActivity.intentTo(fragment.activity!!, (view.tag as String).toInt())
    }

    fun onClickShowtime(view: View) {
        ShowtimeActivity.intentTo(fragment.activity!!)
    }
}