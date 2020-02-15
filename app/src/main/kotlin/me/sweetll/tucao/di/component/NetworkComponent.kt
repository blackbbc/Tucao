package me.sweetll.tucao.di.component

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import me.sweetll.tucao.di.module.NetworkModule
import javax.inject.Named
import javax.inject.Singleton


@Singleton
@Component(modules = [NetworkModule::class, AndroidInjectionModule::class])
interface NetworkComponent {

    fun apiComponent(): ApiComponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance @Named("apiKey") apiKey: String): NetworkComponent
    }
}