package me.sweetll.tucao.business.home.viewmodel

import android.view.View
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.fragment.RecommendFragment
import me.sweetll.tucao.business.rank.RankActivity
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Index
import org.jsoup.nodes.Document

class RecommendViewModel(val fragment: RecommendFragment): BaseViewModel() {
    val HID_PATTERN = "/play/h([0-9]+)/".toRegex()

    init {
        loadData()
    }

    fun loadData() {
        fragment.setRefreshing(true)
        rawApiService.index()
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
                })
    }

    fun onClickRank(view: View) {
        RankActivity.intentTo(fragment.activity)
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
            val hid: String? = HID_PATTERN.find(linkUrl)?.groups?.get(1)?.value

            banners.add(Banner(imgUrl, linkUrl, hid))
        }

        return banners
    }

    fun parseRecommends(doc: Document): Map<Channel, List<Result>> {

        return mutableMapOf()
    }

}
