package me.sweetll.tucao.business.authenticate.viewmodel

import android.accounts.AccountManager
import android.content.Intent
import android.databinding.BindingAdapter
import android.databinding.ObservableField
import android.view.View
import android.widget.ImageView
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.authenticate.AuthenticatorActivity
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.jsoup.nodes.Document

class AuthenticatorViewModel(val activity: AuthenticatorActivity, accountName: String?, val accountType: String?): BaseViewModel() {

    val email = ObservableField<String>()
    val password = ObservableField<String>()
    val code = ObservableField<String>()
    val codeUrl = ObservableField<String>()

    init {
        email.set(accountName)
        checkCode()
    }

    companion object {
        @BindingAdapter("app:imageUrl")
        @JvmStatic
        fun loadImage(imageView: ImageView, url: String?) {
            if (!url.isNullOrEmpty()) imageView.load(imageView.context, url!!)
        }
    }

    fun checkCode() {
        rawApiService.checkCode()
                .sanitizeHtml {
                    parseCode(this)
                }
                .subscribe({
                    codeUrl ->
                    this.codeUrl.set(codeUrl)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun onClickCode(view: View) {
        checkCode()
    }

    fun onClickLogin(view: View) {
        rawApiService.login(email.get(), password.get(), code.get())
                .sanitizeHtml {
                    parseLoginResult(this)
                }
                .subscribe({
                    (resultCode, authToken) ->
                    when (resultCode) {
                        0 -> {
                            val res = Intent()
                            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email.get())
                            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                            res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
                            res.putExtra(AuthenticatorActivity.PARAM_USER_PASS, password.get())
                            activity.finishLogin(res)
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
