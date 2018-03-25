package me.sweetll.tucao.business.home.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.MessageDetailActivity
import me.sweetll.tucao.business.home.model.MessageDetail
import me.sweetll.tucao.di.service.ApiConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MessageDetailViewModel(val activity: MessageDetailActivity, val id: String, val _username: String, val _avatar: String): BaseViewModel() {

    fun loadData() {
        rawApiService.readMessageDetail(id)
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map {
                    parseMessageDetail(Jsoup.parse(it.string()))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    activity.onLoadData(it)
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun parseMessageDetail(doc: Document): MutableList<MessageDetail> {
        val table_right = doc.selectFirst("td.tableright")
        val divs = table_right.child(3).children()

        val res = mutableListOf<MessageDetail>()

        for (index in 0 until divs.size / 3) {
            val div1 = divs[3 * index]
            val div2 = divs[3 * index + 1]

            val class_name = div1.className()
            val type = if ("userpicr" in class_name) MessageDetail.TYPE_RIGHT else MessageDetail.TYPE_LEFT
            val message = div2.ownText().trim()
            val time = div2.selectFirst("div.time").text()
            val avatar = if (type == MessageDetail.TYPE_LEFT) _avatar else user.avatar

            res.add(MessageDetail(avatar, message, time, type))
        }

        return res
    }
}