package me.sweetll.tucao.business.login.viewmodel

import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.net.Uri
import android.view.View
import android.widget.ImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.extension.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class LoginViewModel(val activity: LoginActivity): BaseViewModel() {

    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val code = ObservableField<String>()
    val codeBytes = ObservableField<ByteArray>()

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
        activity.finish()
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
        rawApiService.login_post(email.get(), password.get(), code.get())
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map {
                    response ->
                    val doc = Jsoup.parse(response.body()!!.string())
                    val res = parseLoginResult(doc)
                    Pair(1, response.headers()["cookie"])
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    (resultCode, authToken) ->
                    when (resultCode) {
                        0 -> {
                            val res = Intent()
//                            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email.get())
//                            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
//                            res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
//                            res.putExtra(LoginActivity.PARAM_USER_PASS, password.get())
                        }
                        else -> {
                            "登录失败".toast()
                        }
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun parseCode(doc: Document): String {
        val codeDom = doc.select("img#code_img")
        if (codeDom != null) {
            return codeDom.attr("src")
        } else {
            throw Error("未知错误，请检查网络")
        }
    }

    fun parseLoginResult(doc: Document): Pair<Int, String>{
        return Pair(0, "authToken")
    }

}
