package me.sweetll.tucao.business.login.viewmodel

import android.databinding.ObservableField
import android.os.Handler
import android.os.Message
import android.util.Patterns
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.login.RegisterActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.greenrobot.eventbus.EventBus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class RegisterViewModel(val activity: RegisterActivity): BaseViewModel() {

    val codeBytes = ObservableField<ByteArray>()

    val account = ObservableField<String>("")
    val nickname = ObservableField<String>("")
    val email = ObservableField<String>("")
    val newPassword = ObservableField<String>("")
    val renewPassword = ObservableField<String>("")
    val code = ObservableField<String>("")

    val accountEnabled = ObservableField<Boolean>(true)
    val nicknameEnabled = ObservableField<Boolean>(true)
    val emailEnabled = ObservableField<Boolean>(true)
    val newPasswordEnabled = ObservableField<Boolean>(true)
    val renewPasswordEnabled = ObservableField<Boolean>(true)
    val codeEnabled = ObservableField<Boolean>(true)

    val accountError = ObservableField<String>()
    val nicknameError = ObservableField<String>()
    val emailError = ObservableField<String>()
    val newError = ObservableField<String>()
    val renewError = ObservableField<String>()
    val codeError = ObservableField<String>()

    var hasError: Boolean = false

    val MESSAGE_TRANSITION = 1
    val TRANSITION_DELAY = 1000L

    var failMsg = ""
    var success = false
    var canTransition = false

    val handler = object: Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MESSAGE_TRANSITION) {
                if (canTransition) {
                    if (success) {
                        registerSuccess()
                    } else {
                        registerFailed(failMsg)
                    }
                }
            } else {
                super.handleMessage(msg)
            }
        }
    }

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

    fun checkAccount() {

    }

    fun checkNickname() {

    }

    fun checkEmail() {

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

        if (code.get().isNullOrEmpty()) {
            hasError = true
            codeError.set("验证码不能为空")
        }

        if (hasError) return

        accountEnabled.set(false)
        nicknameEnabled.set(false)
        emailEnabled.set(false)
        newPasswordEnabled.set(false)
        renewPasswordEnabled.set(false)
        codeEnabled.set(false)
        activity.startRegister()

        /*
        canTransition = false
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_TRANSITION), TRANSITION_DELAY)

        handler.postDelayed({
//            canTransition = true
//            if (!handler.hasMessages(MESSAGE_TRANSITION)) {
//                activity.registerSuccess()
//            }

            failMsg = "lalala"
            canTransition = true
            if (!handler.hasMessages(MESSAGE_TRANSITION)) {
                registerFailed(failMsg)
            }
        }, 5000)
        */


        canTransition = false
        success = false
        handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_TRANSITION), TRANSITION_DELAY)

        rawApiService.checkUsername(account.get())
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map {
                    response ->
                    parseCheckResult(Jsoup.parse(response.string()))
                }
                .flatMap {
                    (c, _) ->
                    if (c == 0) {
                        rawApiService.checkNickname(nickname.get())
                                .retryWhen(ApiConfig.RetryWithDelay())
                    } else {
                        throw Error("帐号已存在")
                    }
                }
                .map {
                    response ->
                    parseCheckResult(Jsoup.parse(response.string()))
                }
                .flatMap {
                    (c, _) ->
                    if (c == 0) {
                        rawApiService.checkEmail(email.get())
                                .retryWhen(ApiConfig.RetryWithDelay())
                    } else {
                        throw Error("昵称已存在")
                    }
                }
                .map {
                    response ->
                    parseCheckResult(Jsoup.parse(response.string()))
                }
                .flatMap {
                    (c, _) ->
                    if (c == 0) {
                        rawApiService.register(account.get(), nickname.get(), email.get(), newPassword.get(), renewPassword.get(), code.get())
                                .retryWhen(ApiConfig.RetryWithDelay())
                    } else {
                        throw Error("邮箱已存在")
                    }
                }
                .map {
                    response ->
                    parseCreateResult(Jsoup.parse(response.string()))
                }
                .map {
                    (code, msg) ->
                    if (code == 0) {
                        Object()
                    } else {
                        throw Error(msg)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    user.email = email.get()
                    user.name = nickname.get()
                    user.avatar = ""
                    user.level = 1
                    user.signature = ""
                    EventBus.getDefault().post(RefreshPersonalEvent())

                    success = true
                    canTransition = true
                    if (!handler.hasMessages(MESSAGE_TRANSITION)) {
                        registerSuccess()
                    }

                }, {
                    error ->
                    error.printStackTrace()

                    failMsg = error.message ?: "注册失败"
                    canTransition = true
                    if (!handler.hasMessages(MESSAGE_TRANSITION)) {
                        registerFailed(failMsg)
                    }

                })

    }

    private fun registerSuccess() {
        activity.registerSuccess()
    }

    private fun registerFailed(msg: String) {
        accountEnabled.set(true)
        nicknameEnabled.set(true)
        emailEnabled.set(true)
        newPasswordEnabled.set(true)
        renewPasswordEnabled.set(true)
        codeEnabled.set(true)
        activity.registerFailed(msg)
    }

    private fun parseCheckResult(doc: Document): Pair<Int, String> {
        val result = doc.body().text()
        if ("1" == result) {
            return Pair(0, "")
        } else {
            return Pair(1, result)
        }
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
