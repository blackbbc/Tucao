package me.sweetll.tucao.di.component

import dagger.Component
import me.sweetll.tucao.di.module.ApiModule
import me.sweetll.tucao.di.module.BaseModule
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(BaseModule::class))
interface BaseComponent {
    fun plus(apiModule: ApiModule) : ApiComponent
}