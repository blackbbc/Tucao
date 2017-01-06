package me.sweetll.tucao.di.module

import dagger.Module
import dagger.Provides
import me.sweetll.tucao.di.scope.ApplicationScope
import me.sweetll.tucao.di.service.JsonApiService
import retrofit2.Retrofit
import javax.inject.Named

@Module
class ApiModule {

    @ApplicationScope
    @Provides
    fun provideJsonService(@Named("json") retrofit: Retrofit) = retrofit.create(JsonApiService::class.java)

}
