package me.sweetll.tucao.business.uploader.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.uploader.UploaderActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.jsoup.nodes.Document

class UploaderViewModel(val activity: UploaderActivity, val userId: String): BaseViewModel() {
    var pageIndex = 1
    var pageSize = 20

    init {
        loadData()
    }

    fun loadData() {
        pageIndex = 1
        rawApiService.space(userId, pageIndex)
                .bindToLifecycle(activity)
                .sanitizeHtml {
                    parseVideos(this)
                }
                .subscribe({
                    data ->
                    pageIndex ++
                    activity.loadData(data)
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun loadMoreData() {
        rawApiService.space(userId, pageIndex)
                .bindToLifecycle(activity)
                .sanitizeHtml {
                    parseVideos(this)
                }
                .subscribe({
                    data ->
                    if (data.size < pageSize) {
                        activity.loadMoreData(data, Const.LOAD_MORE_END)
                    } else {
                        pageIndex ++
                        activity.loadMoreData(data, Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    activity.loadMoreData(null, Const.LOAD_MORE_FAIL)
                })
    }

    fun parseVideos(doc: Document): MutableList<Video> {
        val v_divs = doc.select("div.v")

        return v_divs.fold(mutableListOf()) {
            total, v_div ->

            val tt_a = v_div.select("a.tt")[0]
            val title = tt_a.text()
            val hid = tt_a.attr("href").replace("[\\D]".toRegex(), "")
            val thumb = v_div.select("img")[0].attr("src")

            val i_div = v_div.select("div.i")[0]
            val info = i_div.text().replace(",", "").replace("[\\D]+".toRegex(), " ")
            val nums = info.trim().split(" ").takeLast(4)

            val play = nums[0].toInt()
            val mukio = nums[1].toInt()

            val video = Video(title = title, hid = hid, thumb = thumb, play = play, mukio = mukio)

            total.add(video)
            total
        }
    }

    fun onClickSendMessage(view: View) {
        "不发不发就不发σ`∀´)".toast()
    }

}
