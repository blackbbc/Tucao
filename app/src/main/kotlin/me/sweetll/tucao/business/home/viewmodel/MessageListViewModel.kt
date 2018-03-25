package me.sweetll.tucao.business.home.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.MessageListActivity
import me.sweetll.tucao.business.home.model.MessageList
import me.sweetll.tucao.di.service.ApiConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class MessageListViewModel(val activity: MessageListActivity): BaseViewModel() {

    fun loadData() {
        rawApiService.readMessageList()
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map {
                    parseMessageList(Jsoup.parse(it.string()))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    activity.onLoadData(it)
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun parseMessageList(doc: Document): MutableList<MessageList> {
        val message_table = doc.select("table.inbox")[0]
        return message_table.child(0).children().dropLast(1).fold(mutableListOf()) {
            res, ele ->
            val avatar = ele.child(0).selectFirst("img").attr("src")
            val content = ele.child(1)
            val id = content.child(1).`val`()
            val link_a = content.child(0)
            val username = link_a.selectFirst("em.red").text()
            val time = link_a.selectFirst("em.time").text().removePrefix("对话于 ")
            val message = link_a.selectFirst("div.show").text()
            val read = link_a.selectFirst("em.bgblue") == null
            res.add(MessageList(id, username, avatar, time, message, read))
            res
        }
    }

}