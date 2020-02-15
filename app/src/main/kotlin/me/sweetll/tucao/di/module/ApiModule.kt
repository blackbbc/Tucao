package me.sweetll.tucao.di.module

import dagger.Module
import dagger.Provides
import me.sweetll.tucao.di.scope.ApplicationScope
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.di.service.XmlApiService
import me.sweetll.tucao.model.other.User
import me.sweetll.tucao.rxdownload.function.DownloadApi
import retrofit2.Retrofit
import javax.inject.Named

@Module
class ApiModule {

    @ApplicationScope
    @Provides
    fun provideRawService(@Named("raw") retrofit: Retrofit) = retrofit.create(RawApiService::class.java)

    @ApplicationScope
    @Provides
    fun provideJsonService(@Named("json") retrofit: Retrofit) = retrofit.create(JsonApiService::class.java)

    @ApplicationScope
    @Provides
    fun provideXmlService(@Named("xml") retrofit: Retrofit) = retrofit.create(XmlApiService::class.java)

    @ApplicationScope
    @Provides
    fun provideDownloadApi(@Named("download") retrofit: Retrofit) = retrofit.create(DownloadApi::class.java)
}
