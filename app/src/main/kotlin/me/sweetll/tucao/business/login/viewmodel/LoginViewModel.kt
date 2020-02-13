package me.sweetll.tucao.business.login.viewmodel

import android.app.Activity
import androidx.databinding.ObservableField
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.login.ForgotPasswordActivity
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.business.login.RegisterActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.NonNullObservableField
import me.sweetll.tucao.extension.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class LoginViewModel(val activity: LoginActivity): BaseViewModel() {

    val email = NonNullObservableField("")
    val password = NonNullObservableField("")
    val code = NonNullObservableField("")
    val codeBytes = ObservableField<ByteArray>()

    val container = NonNullObservableField(View.VISIBLE)
    val progress = NonNullObservableField(View.GONE)

    init {
        initSession()
    }

    fun initSession() {
        rawApiService.login_get()
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribe({
                    checkCode()
                }, {
                    error ->
                    error.printStackTrace()
                })
    }

    fun checkCode() {
        rawApiService.checkCode()
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .subscribe({
                    body ->
                    this.codeBytes.set(body.bytes())
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun dismiss(view: View) {
        activity.setResult(Activity.RESULT_CANCELED)
        activity.supportFinishAfterTransition()
    }

    fun onClickCode(view: View) {
        checkCode()
    }

    fun onClickSignUp(view: View) {
        RegisterActivity.intentTo(activity)
        activity.finish()
    }

    fun onClickSignIn(view: View) {
        activity.showLoading()
        rawApiService.login_post(email.get(), password.get(), code.get())
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map { parseLoginResult(Jsoup.parse(it.string())) }
                .flatMap {
                    (code, msg) ->
                    if (code == 0) {
                        rawApiService.personal()
                    } else {
                        Observable.error(Error(msg))
                    }
                }
                .map { parsePersonal(Jsoup.parse(it.string())) }
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate {
                    activity.showLogin()
                }
                .subscribe({
                    user.email = email.get()
                    activity.setResult(Activity.RESULT_OK)
                    activity.supportFinishAfterTransition()
                }, {
                    error ->
                    error.printStackTrace()
                    Snackbar.make(activity.binding.container, error.message ?: "登陆失败", Snackbar.LENGTH_SHORT).show()
                })
    }

    fun onClickForgotPassword(view: View) {
        ForgotPasswordActivity.intentTo(activity)
    }

    fun parseLoginResult(doc: Document): Pair<Int, String>{
        val content = doc.body().text()
        return if ("成功" in content) {
            Pair(0, "")
        } else {
            Pair(1, content)
        }
    }

    private fun parsePersonal(doc: Document): Any {
        // 获取等级
        val lv_a = doc.select("a.lv")[0]
        user.level = lv_a.text().substring(3).toInt()

        // 获取用户名
        val name_div = doc.select("a.name")[0]
        user.name = name_div.text()

        // 获取头像地址
        val index_div = doc.select("div.index")[0]
        val avatar_img = index_div.child(0).child(0)
        user.avatar = avatar_img.attr("src")

        // 获取签名
        val index_table = doc.select("table.index_table")[0]
        val signature_td = index_table.child(0).child(2).child(0)
        user.signature = signature_td.text().removeSuffix(" 更新")

        // 获取短消息
        val message_td = index_table.child(0).child(0).child(3)
        val message = message_td.child(0).child(0).text()
        user.message = if (message == "--") 0 else message.toInt()

        // 获取
        return Object()
    }


}
