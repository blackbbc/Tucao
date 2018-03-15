package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.disposables.Disposable
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.home.fragment.RecommendFragment
import me.sweetll.tucao.business.rank.RankActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Index
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class RecommendViewModel(val fragment: RecommendFragment): BaseViewModel() {
    val HID_PATTERN = "/play/h([0-9]+)/".toRegex()
    val TID_PATTERN = "/list/([0-9]+)/".toRegex()

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
        val banners = mutableListOf<Banner>()

        val index_pos9 = doc.select("div.index_pos9").first()
        val ul = index_pos9.child(1)
        ul.children().forEach {
            // <li><a href="http://www.tucao.tv/play/h4070217/" target="_blank"><img src="http://www.tucao.tv/uploadfile/2017/0102/thumb_296_190_20170102034446261.jpg" alt="【合集】 FLIP FLAPPERS 01~13话【SweetSub&amp;LoliHouse】"><p>对你而言，世界是怎样的呢——。获得了打开大门的钥匙的两位女主人公，帕皮卡和可可娜。少~~</p><b>【合集】 FLIP FLAPPERS 01~13话【SweetSub&amp;LoliHouse】</b><i class="time">--:--</i></a></li>
            val aElement = it.child(0)
            val linkUrl = aElement.attr("href")
            val imgElement = aElement.child(0)
            val imgUrl = imgElement.attr("src")
            val hid: String? = HID_PATTERN.find(linkUrl)?.groupValues?.get(1)

            banners.add(Banner(imgUrl, linkUrl, hid))
        }

        return banners
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
        val pos8_show = doc.select("div.pos8_show").first()

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

}
