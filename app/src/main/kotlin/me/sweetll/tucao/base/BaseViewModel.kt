package me.sweetll.tucao.base

import androidx.databinding.BaseObservable
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.di.service.XmlApiService
import me.sweetll.tucao.model.other.User
import javax.inject.Inject

open class BaseViewModel : BaseObservable() {

    @Inject
    lateinit var user: User

    @Inject
    lateinit var rawApiService: RawApiService

    @Inject
    lateinit var jsonApiService: JsonApiService

    @Inject
    lateinit var xmlApiService: XmlApiService;

    init {
        AppApplication.get()
                .getUserComponent()
                .inject(this)
    }
}