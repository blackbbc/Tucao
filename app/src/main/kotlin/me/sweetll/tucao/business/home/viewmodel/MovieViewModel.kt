package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.business.home.fragment.MovieFragment
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Movie
import me.sweetll.tucao.util.parseBanner
import me.sweetll.tucao.util.parseChannelList
import org.jsoup.nodes.Document

class MovieViewModel(val fragment: MovieFragment): BaseViewModel() {

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.list(23)
                .bindToLifecycle(fragment)
                .sanitizeHtml {
                    val banners = parseBanners(this)
                    val recommends = parseRecommends(this)
                    Movie(banners, recommends)
                }
                .doAfterTerminate { fragment.setRefreshing(false) }
                .subscribe({
                    movie ->
                    fragment.loadMovie(movie)
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

    private fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val listParentNode = doc.getElementById("loop_num")
        return parseChannelList(listParentNode)
    }

    fun onClickChannel(view: View) {
        ChannelDetailActivity.intentTo(fragment.activity!!, (view.tag as String).toInt())
    }

}