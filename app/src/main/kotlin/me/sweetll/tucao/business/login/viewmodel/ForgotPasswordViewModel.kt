package me.sweetll.tucao.business.login.viewmodel

import androidx.databinding.ObservableField
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.login.ForgotPasswordActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.NonNullObservableField
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.jsoup.nodes.Document

class ForgotPasswordViewModel(val activity: ForgotPasswordActivity): BaseViewModel() {

    val email = NonNullObservableField("")
    val code = NonNullObservableField("")
    val codeBytes = ObservableField<ByteArray>()

    init {
        checkCode()
    }

    fun checkCode() {
        rawApiService.checkCode()
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribe({
                    body ->
                    codeBytes.set(body.bytes())
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun onClickCode(view: View) {
        checkCode()
    }

    fun onClickSubmit(view: View) {
        if (email.get().isNullOrEmpty()) {
            "邮箱不能为空".toast()
            return
        }

        if (code.get().isNullOrEmpty()) {
            "验证码不能为空".toast()
            return
        }

        rawApiService.forgotPassword(email.get(), code.get())
                .bindToLifecycle(activity)
                .sanitizeHtml { parseSubmitResult(this) }
                .subscribe({
                    activity.resetSuccess()
                }, {
                    error ->
                    error.printStackTrace()
                    activity.resetFailed(error.message ?: "重设密码失败")
                })
    }

    fun parseSubmitResult(doc: Document): Pair<Int, String>{
        val content = doc.body().text()
        if ("成功" in content) {
            return Pair(0, "")
        } else {
            return Pair(1, content)
        }
    }

}
