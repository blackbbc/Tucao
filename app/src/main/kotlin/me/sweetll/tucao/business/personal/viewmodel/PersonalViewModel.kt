package me.sweetll.tucao.business.personal.viewmodel

import android.app.AlertDialog
import android.databinding.ObservableField
import android.view.View
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import org.greenrobot.eventbus.EventBus

class PersonalViewModel(val activity: PersonalActivity) : BaseViewModel() {
    val avatar = ObservableField<String>(user.avatar)
    val nickname = ObservableField<String>(user.name)
    val uuid = ObservableField<String>()
    val signature = ObservableField<String>()


    init {

    }

    fun onClickAvatar(view: View) {
        "修改头像施工中".toast()
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
