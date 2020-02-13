package me.sweetll.tucao.business.personal.viewmodel

import androidx.databinding.ObservableField
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.personal.fragment.ChangeInformationFragment
import me.sweetll.tucao.extension.NonNullObservableField
import me.sweetll.tucao.extension.sanitizeHtml
import org.greenrobot.eventbus.EventBus
import org.jsoup.nodes.Document

class ChangeInformationViewModel(val fragment: ChangeInformationFragment): BaseViewModel() {
    val nickname = NonNullObservableField(user.name)
    val signature = NonNullObservableField(user.signature)

    fun save() {
        rawApiService.changeInformation(nickname.get(), signature.get())
                .bindToLifecycle(fragment)
                .subscribeOn(Schedulers.io())
                .sanitizeHtml { parseSaveResult(this) }
                .map {
                    (code, msg) ->
                    if (code == 0) {
                        Object()
                    } else {
                        throw Error(msg)
                    }
                }
                .subscribe({
                    user.name = nickname.get()
                    user.signature = signature.get()
                    EventBus.getDefault().post(RefreshPersonalEvent())
                    fragment.saveSuccess()
                }, {
                    error ->
                    error.printStackTrace()
                    fragment.saveFailed(error.message ?: "保存失败")
                })
    }

    private fun parseSaveResult(doc: Document):Pair<Int, String>  {
        val result = doc.body().text()
        if ("成功" in result) {
            return Pair(0, "")
        } else {
            return Pair(1, result)
        }
    }
}
