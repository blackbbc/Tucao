package me.sweetll.tucao.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.sweetll.tucao.business.home.MainActivity
import me.sweetll.tucao.business.splash.SplashActivity

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashActivity(): SplashActivity

}
