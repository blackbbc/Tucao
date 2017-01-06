package me.sweetll.tucao.business.channel.viewmodel

import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.extension.logD

class ChannelDetailViewModel(val context: Context) : BaseViewModel() {
    init {
        jsonApiService.list(19, 1, 10, null)
                .subscribeOn(Schedulers.io())
                .flatMap { response ->
                    if (response.code == 200) {
                        Observable.just(response.result)
                    } else {
                        Observable.error(Error(response.msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    resultList ->
                    "Success".logD()
                }, Throwable::printStackTrace)
    }
}
