package me.sweetll.tucao.business.login.viewmodel

import android.app.Activity
import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.transition.TransitionManager
import android.view.View
import android.widget.ImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class LoginViewModel(val activity: LoginActivity): BaseViewModel() {

    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val code = ObservableField<String>()
    val codeBytes = ObservableField<ByteArray>()

    val container = ObservableField<Int>(View.VISIBLE)
    val progress = ObservableField<Int>(View.GONE)

    init {
        initSession()
    }

    companion object {
        @BindingAdapter("app:imageUrl")
        @JvmStatic
        fun loadImage(imageView: ImageView, bytes: ByteArray?) {
            bytes?.let {
                imageView.load(imageView.context, it)
            }
        }
    }

    fun initSession() {
        rawApiService.login_get()
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
        val intent = Intent("android.intent.action.VIEW")
        intent.data = Uri.parse("http://www.tucao.tv/index.php?m=member&c=index&a=register&siteid=1")
        activity.startActivity(intent)
    }

    fun onClickSignIn(view: View) {
        activity.showLoading()
        rawApiService.login_post(email.get(), password.get(), code.get())
                .sanitizeHtml {
                    parseLoginResult(this)
                }
                .doAfterTerminate {
                    activity.showLogin()
                }
                .subscribe({
                    (code, msg) ->
                    when (code) {
                        0 -> {
                            user.email = email.get()
                            activity.setResult(Activity.RESULT_OK)
                            activity.supportFinishAfterTransition()
                        }
                        else -> {
                            Snackbar.make(activity.binding.container, msg, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun parseLoginResult(doc: Document): Pair<Int, String>{
        val content = doc.body().text()
        if ("登录成功" in content) {
            return Pair(0, "")
        } else {
            return Pair(1, content)
        }
    }

}
