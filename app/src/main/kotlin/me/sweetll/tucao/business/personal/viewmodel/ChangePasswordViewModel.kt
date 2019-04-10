package me.sweetll.tucao.business.personal.viewmodel

import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.personal.fragment.ChangePasswordFragment
import me.sweetll.tucao.extension.NonNullObservableField
import me.sweetll.tucao.extension.sanitizeHtml
import org.greenrobot.eventbus.EventBus
import org.jsoup.nodes.Document

class ChangePasswordViewModel(val fragment: ChangePasswordFragment): BaseViewModel() {
    val oldPassword = NonNullObservableField("")
    val oldError = NonNullObservableField("")
    val newPassword = NonNullObservableField("")
    val newError = NonNullObservableField("")
    val renewPassword = NonNullObservableField("")
    val renewError = NonNullObservableField("")

    var hasError = false

    fun save() {

        hasError = false
        oldError.set("")
        newError.set("")
        renewError.set("")

        if (oldPassword.get().length < 6 || oldPassword.get().length > 20) {
            hasError = true
            oldError.set("密码应在6-20位之间")
        }

        if (newPassword.get() != renewPassword.get()) {
            hasError = true
            newError.set("两次输入的密码不一致")
            renewError.set("两次输入的密码不一致")
        }

        if (newPassword.get().length < 6 || newPassword.get().length > 20) {
            hasError = true
            newError.set("密码应在6-20位之间")
        }

        if (renewPassword.get().length < 6 || renewPassword.get().length > 20) {
            hasError = true
            renewError.set("密码应在6-20位之间")
        }

        if (hasError) return

        rawApiService.changePassword(oldPassword.get(), newPassword.get(), renewPassword.get())
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
                    user.invalidate()
                    EventBus.getDefault().post(RefreshPersonalEvent())
                    fragment.saveSuccess()
                }, {
                    error ->
                    error.printStackTrace()
                    fragment.saveFailed(error.message ?: "修改密码失败")
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
