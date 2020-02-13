package me.sweetll.tucao.business.personal.viewmodel

import android.app.AlertDialog
import androidx.databinding.ObservableField
import android.net.Uri
import android.util.Base64
import android.view.View
import com.jph.takephoto.model.TImage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.business.personal.fragment.PersonalFragment
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.other.User
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

class PersonalViewModel(val activity: PersonalActivity, val fragment: PersonalFragment) : BaseViewModel() {
    val avatar = ObservableField<String>(user.avatar)
    val nickname = ObservableField<String>(user.name)
    val uuid = ObservableField<String>()
    val signature = ObservableField<String>(user.signature)

    fun refresh() {
        if (!user.isValid()) {
            activity.finish()
            return
        }
        avatar.set(user.avatar)
        nickname.set(user.name)
        signature.set(user.signature)
    }

    fun uploadAvatar(image: TImage) {
        rawApiService.manageAvatar()
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .map {
                    body ->
                    val urlPattern = "'upurl':\"(.+)&callback=return_avatar&\"".toRegex()
                    val result = urlPattern.find(body.string())
                    result?.groupValues?.get(1) ?: throw Exception("请重新登录")
                }.map {
                    encodeUrl ->
                    val decodeUrl = String(Base64.decode(encodeUrl, Base64.DEFAULT))
                    decodeUrl.substring(decodeUrl.indexOf("&data") + 6)
                }.flatMap {
                    data ->
                    val file = File(image.compressPath)
                    val body = RequestBody.create(
                            MediaType.parse("application/octet-stream"),
                            file
                    )
                    rawApiService.uploadAvatar(data, body)
                }.flatMap { rawApiService.personal() }
                .map { parsePersonal(Jsoup.parse(it.string())) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { fragment.showUploadingDialog() }
                .doAfterTerminate { fragment.dismissUploadingDialog() }
                .subscribe({
                    User.updateSignature()
                    EventBus.getDefault().post(RefreshPersonalEvent())
                    "修改头像成功".toast()
                }, {
                    error ->
                    error.message?.toast()
                    error.printStackTrace()
                })
    }

    fun onClickAvatar(view: View) {
        fragment.choosePickType()
    }

    fun onClickNickname(view: View) {
        activity.transitionToChangeInformation()
    }

    fun onClickSignature(view: View) {
        activity.transitionToChangeInformation()
    }

    fun onClickChangePassword(view: View) {
        activity.transitionToChangePassword()
    }

    fun onClickLogout(view: View) {
        val builder = AlertDialog.Builder(activity)
                .setMessage("真的要退出吗QAQ")
                .setPositiveButton("真的", {
                    dialog, _ ->
                    rawApiService.logout()
                            .sanitizeHtml {
                                Object()
                            }
                            .subscribe({

                            }, {

                            })
                    user.invalidate()
                    EventBus.getDefault().post(RefreshPersonalEvent())
                    dialog.dismiss()
                    activity.finish()
                })
                .setNegativeButton("假的", {
                    dialog, _ ->
                    dialog.dismiss()
                })
        builder.create().show()
    }

    private fun parsePersonal(doc: Document): Any {
        // 获取头像地址
        val index_div = doc.select("div.index")[0]
        val avatar_img = index_div.child(0).child(0)
        user.avatar = avatar_img.attr("src")

        return Object()
    }

}
