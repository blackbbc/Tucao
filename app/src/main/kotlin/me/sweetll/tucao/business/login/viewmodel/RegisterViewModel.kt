package me.sweetll.tucao.business.login.viewmodel

import android.databinding.ObservableField
import android.os.Handler
import android.util.Patterns
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.login.RegisterActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.jsoup.nodes.Document

class RegisterViewModel(val activity: RegisterActivity): BaseViewModel() {

    val codeBytes = ObservableField<ByteArray>()

    val account = ObservableField<String>("")
    val nickname = ObservableField<String>("")
    val email = ObservableField<String>("")
    val newPassword = ObservableField<String>("")
    val renewPassword = ObservableField<String>("")
    val code = ObservableField<String>("")

    val accountError = ObservableField<String>()
    val nicknameError = ObservableField<String>()
    val emailError = ObservableField<String>()
    val newError = ObservableField<String>()
    val renewError = ObservableField<String>()
    val codeError = ObservableField<String>()

    var hasError: Boolean = false

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

    fun onClickCreate(view: View) {
        hasError = false
        accountError.set(null)
        nicknameError.set(null)
        emailError.set(null)
        newError.set(null)
        renewError.set(null)
        codeError.set(null)

        /*
        if (account.get().length < 2 || account.get().length > 20) {
            hasError = true
            accountError.set("帐号应在2-20位之间")
        }

        if (nickname.get().length < 2 || nickname.get().length > 20) {
            hasError = true
            nicknameError.set("昵称应在2-20位之间")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.get()).matches()) {
            hasError = true
            emailError.set("这不是一个合法的邮箱")
        }

        if (newPassword.get() != renewPassword.get()) {
            hasError = true
            newError.set("两次输入的密码不一致")
        }

        if (newPassword.get().length < 6 || newPassword.get().length > 20) {
            hasError = true
            newError.set("密码应在6-20位之间")
        }

        if (renewPassword.get().length < 6 || renewPassword.get().length > 20) {
            hasError = true
            newError.set("密码应在6-20位之间")
        }

        if (code.get().isNullOrEmpty()) {
            hasError = true
            codeError.set("验证码不能为空")
        }

        if (hasError) return
        */

        activity.startRegister()

        Handler().postDelayed({
            activity.registerSuccess()
        }, 1000)

//        rawApiService.register(account.get(), nickname.get(), email.get(), newPassword.get(), renewPassword.get(), code.get())
//                .bindToLifecycle(activity)
//                .sanitizeHtml { parseCreateResult(this) }
//                .map {
//                    (code, msg) ->
//                    if (code == 0) {
//                        Object()
//                    } else {
//                        throw Error(msg)
//                    }
//                }
//                .subscribe({
//                    activity.registerSuccess()
//                }, {
//                    error ->
//                    error.printStackTrace()
//                    activity.registerFailed(error.message ?: "注册失败")
//                })

    }

    private fun parseCreateResult(doc: Document):Pair<Int, String>  {
        val result = doc.body().text()
        if ("成功" in result) {
            return Pair(0, "")
        } else {
            return Pair(1, result)
        }
    }

}
