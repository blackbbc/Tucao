package me.sweetll.tucao.business.home.viewmodel

import androidx.databinding.ObservableField
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.MessageDetailActivity
import me.sweetll.tucao.business.home.model.MessageDetail
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.NonNullObservableField
import me.sweetll.tucao.extension.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*

class MessageDetailViewModel(val activity: MessageDetailActivity, val id: String, val _username: String, val _avatar: String): BaseViewModel() {

    val message = NonNullObservableField<String>("")

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

    fun onClickSendMessage(view: View) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        activity.addMessage(MessageDetail(user.avatar, message.get(), sdf.format(Date()), MessageDetail.TYPE_RIGHT))
        rawApiService.replyMessage(message.get(), id, user.name)
                .bindToLifecycle(activity)
                .doOnNext {
                    message.set("")
                }
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map { parseSendResult(Jsoup.parse(it.string())) }
                .flatMap {
                    (code, msg) ->
                    if (code == 0) {
                        Observable.just(Object())
                    } else {
                        Observable.error(Error(msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //
                }, {
                    error ->
                    error.printStackTrace()
                    error.localizedMessage.toast()
                })
    }

    fun parseSendResult(doc: Document): Pair<Int, String> {
        val content = doc.body().text()
        return if ("成功" in content) {
            Pair(0, "")
        } else {
            Pair(1, content)
        }
    }
}