package me.sweetll.tucao.di.service

import io.reactivex.Observable
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

object ApiConfig {
    const val API_KEY = "25tids8f1ew1821ed"

    const val BASE_RAW_API_URL = "http://www.tucao.tv/"
    const val BASE_JSON_API_URL = "http://www.tucao.tv/api_v2/"
    const val BASE_XML_API_URL = "http://www.tucao.tv/"

    /*
     * Json
     */
    const val LIST_API_URL = "list.php"
    const val SEARCH_API_URL = "search.php"
    const val VIEW_API_URL = "view.php";

    /*
     * XML
     */
    const val PLAY_URL_API_URL = "http://api.tucao.tv/api/playurl"

    fun generateDanmuUrl(hid: String, part: Int) = "http://www.tucao.tv/index.php?m=mukio&c=index&a=init&playerID=11-$hid-1-$part&r=${System.currentTimeMillis()/1000}"

    class RetryWithDelay(val maxRetries: Int = 3, val delayMillis: Long = 2000L) : Function<Observable<in Throwable>, Observable<*>> {
        var retryCount = 0

        override fun apply(observable: Observable<in Throwable>): Observable<*> = observable
                .flatMap { throwable ->
                    if (++retryCount < maxRetries) {
                        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
                    } else {
                        Observable.error(throwable as Throwable)
                    }
                }
    }

}
