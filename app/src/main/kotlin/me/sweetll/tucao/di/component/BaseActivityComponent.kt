package me.sweetll.tucao.di.component

import dagger.Subcomponent
import dagger.android.AndroidInjector
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.di.module.UserModule

@Subcomponent(modules = [UserModule::class])
interface BaseActivityComponent: AndroidInjector<BaseActivity> {

    @Subcomponent.Factory
    interface Factory: AndroidInjector.Factory<BaseActivity>

}