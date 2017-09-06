package me.sweetll.tucao.business.personal.viewmodel

import android.app.AlertDialog
import android.databinding.ObservableField
import android.net.Uri
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
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream

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
        val file = File(image.compressPath)
//        val inputStream = FileInputStream(file)
//        val buf = ByteArray(inputStream.available())
        val body = RequestBody.create(
                MediaType.parse("application/octet-stream"),
                file
        )
        rawApiService.uploadAvatar(body)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    "上传图片成功".logD()
                }, {
                    error ->
                    "上传图片失败".logD()
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


}
