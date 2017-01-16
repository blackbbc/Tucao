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
    init {
        loadData()
    }

    fun loadData() {
        rawApiService.index()
                .sanitizeHtml({
                    val banners = parseBanners(this)
                    val recommends = parseRecommends(this)
                    Index(banners, recommends)
                })
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

        return banners
    }

    fun parseRecommends(doc: Document): Map<Channel, List<Result>> {

        return mutableMapOf()
    }

}
