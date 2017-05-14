package me.sweetll.tucao.base

import android.databinding.BaseObservable
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.di.service.XmlApiService
import javax.inject.Inject

open class BaseViewModel : BaseObservable() {

    @Inject
    lateinit var rawApiService: RawApiService

    @Inject
    lateinit var jsonApiService: JsonApiService

    @Inject
    lateinit var xmlApiService: XmlApiService;

    init {
        AppApplication.get()
                .getApiComponent()
                .inject(this)
    }
}