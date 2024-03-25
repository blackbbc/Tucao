package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.fragment.RecommendFragment
import me.sweetll.tucao.business.rank.RankActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Index
import me.sweetll.tucao.util.parseBanner
import me.sweetll.tucao.util.parseChannelList
import me.sweetll.tucao.util.parseListVideo
import org.jsoup.nodes.Document

class RecommendViewModel(val fragment: RecommendFragment): BaseViewModel() {

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.index()
                .bindToLifecycle(fragment)
                .sanitizeHtml({
                    val banners = parseBanners(this)
                    val recommends = parseRecommends(this)
                    Index(banners, recommends)
                })
                .doAfterTerminate { fragment.setRefreshing(false) }
                .subscribe({
                    index ->
                    fragment.loadIndex(index)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    fragment.loadError()
                })
    }

    fun onClickRank(view: View) {
        RankActivity.intentTo(fragment.activity!!)
    }

    fun parseBanners(doc: Document): List<Banner> {
        val slides = doc.select("div.slide")
        return parseBanner(slides)
    }

    fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val listParentNode = doc.getElementsByClass("list loop_list24").first().parent()
        val recommends = parseChannelList(listParentNode).toMutableList()

        //解析今日推荐
        val channel = Channel(0, "今日推荐")
        val list8 = doc.getElementsByClass("list list8").first()
        val videos = parseListVideo(list8)
        recommends.add(0, channel to videos.subList(0,4))
//        recommends.add(1, channel to videos.subList(4,8))

        return recommends
    }

}
