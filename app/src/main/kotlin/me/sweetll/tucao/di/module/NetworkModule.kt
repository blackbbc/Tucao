package me.sweetll.tucao.di.module

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import dagger.Module
import dagger.Provides
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.di.service.ApiConfig
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(AppApplication.get()))

    @Provides
    @Singleton
    @Named("raw")
    fun provideRawOkHttpClient(cookieJar: CookieJar): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("download")
    fun provideDownloadOkHttpClient(cookieJar: CookieJar): OkHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()

    @Provides
    @Singleton
    @Named("json")
    fun provideJsonClient(cookieJar: CookieJar, @Named("apiKey") apiKey: String): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val url = chain.request().url()
                        .newBuilder()
                        .addQueryParameter("apikey", apiKey)
                        .addQueryParameter("type", "json")
                        .build()
                val request = chain.request().newBuilder()
                        .url(url)
                        .build()
                val response = chain.proceed(request)
                response
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    @Provides
    @Singleton
    @Named("xml")
    fun provideXmlClient(cookieJar: CookieJar): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .cookieJar(cookieJar)
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
    @Named("download")
    fun provideDownloadRetrofit(@Named("download") client: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_RAW_API_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()


    @Provides
    @Singleton
    @Named("json")
    fun provideJsonRetrofit(@Named("json") client: OkHttpClient) : Retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_JSON_API_URL)
            .addConverterFactory(MoshiConverterFactory.create())
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
