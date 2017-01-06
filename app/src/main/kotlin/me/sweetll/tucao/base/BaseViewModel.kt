package me.sweetll.tucao.base

import android.databinding.BaseObservable
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.di.service.JsonApiService

class BaseViewModel : BaseObservable() {

    lateinit var jsonApiService: JsonApiService

    init {
        AppApplication.get()
                .getApiComponent()
                .inject(this)
    }
}