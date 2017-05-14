package me.sweetll.tucao.di.module

import dagger.Module
import dagger.Provides
import me.sweetll.tucao.di.service.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class BaseModule(val apiKey: String) {
    @Provides
    @Singleton
    @Named("raw")
    fun provideRawOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("json")
    fun provideJsonClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                var request = chain.request()
                request = chain.request().newBuilder()
                        .url("${request.url()}&apikey=$apiKey&type=json")
                        .build()
                val response = chain.proceed(request)
                response
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("xml")
    fun provideXmlClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("raw")
    fun provideRawRetrofit(@Named("raw") client: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_RAW_API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()

    @Provides
    @Singleton
    @Named("json")
    fun provideJsonRetrofit(@Named("json") client: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_JSON_API_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()

    @Provides
    @Singleton
    @Named("xml")
    fun provideXmlRetrofit(@Named("xml") client: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_XML_API_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()

}
