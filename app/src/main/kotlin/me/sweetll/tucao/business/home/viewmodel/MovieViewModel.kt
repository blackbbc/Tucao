package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.home.fragment.MovieFragment
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Movie
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class MovieViewModel(val fragment: MovieFragment): BaseViewModel() {
    val HID_PATTERN = "/play/h([0-9]+)/".toRegex()
    val TID_PATTERN = "/list/([0-9]+)/".toRegex()

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
        val banners = mutableListOf<Banner>()

        val newcatfocus = doc.select("div.newcatfoucs").first()
        val pic = newcatfocus.child(0)
        pic.children().forEach {
            // <a href="http://www.tucao.tv/play/h4070393/" target="_blank" style="display: none;"><img src="http://www.tucao.tv/uploadfile/2017/0117/20170117080016696.png" alt="【1月】废天使加百列/珈百璃的堕落 02【动漫国】"><div class="title">【1月】废天使加百列/珈百璃的堕落 02【动漫国】</div></a>
            val linkUrl = it.attr("href")
            val imgElement = it.child(0)
            val imgUrl = imgElement.attr("src")
            val hid: String? = HID_PATTERN.find(linkUrl)?.groupValues?.get(1)
            val title = it.child(1).text()

            banners.add(Banner(imgUrl, linkUrl, hid, title))
        }

        return banners
    }

    fun parseRecommends(doc: Document): List<Pair<Channel, List<Video>>> {
        val title_red = doc.select("h2.title_red").takeLast(4)
        val lists_tip = doc.select("div.lists.tip").takeLast(4)
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

        return recommends
    }

    fun onClickChannel(view: View) {
        ChannelDetailActivity.intentTo(fragment.activity!!, (view.tag as String).toInt())
    }

}