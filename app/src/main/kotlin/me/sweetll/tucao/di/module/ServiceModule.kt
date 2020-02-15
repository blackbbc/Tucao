package me.sweetll.tucao.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.sweetll.tucao.rxdownload.function.DownloadService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeDownloadService(): DownloadService

}