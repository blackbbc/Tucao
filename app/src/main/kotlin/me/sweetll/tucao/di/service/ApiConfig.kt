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
    const val VIEW_API_URL = "view.php"
    const val RANK_API_URL = "rank.php"
    const val REPLY_API_URL = "http://www.tucao.tv/index.php?m=comment&c=index&a=ajax"

    const val UPDATE_API_URL = "http://45.63.54.11:12450/api/app-portal/version"

    /*
     * XML
     */
    const val PLAY_URL_API_URL = "http://api.tucao.tv/api/playurl"
    const val DANMU_API_URL = "http://www.tucao.tv/index.php?m=mukio&c=index&a=init"

    /*
     * Raw
     */
    const val INDEX_URL = "/"
    const val LIST_URL = "list/{tid}/"
    const val BGM_URL = "bgm/{year}/{month}/"
    const val SEND_DANMU_URL = "index.php?m=mukio&c=index&a=post"
    const val COMMENT_URL = "index.php?m=comment&c=index&a=init&hot=0&iframe=1"
    const val SEND_COMMENT_URL = "index.php?m=comment&c=index&a=post"

    const val CODE_URL = "api.php?op=checkcode&code_len=4&font_size=14&width=446&height=40"
    const val LOGIN_URL = "index.php?m=member&c=index&a=login"
    const val LOGOUT_URL = "index.php?m=member&c=index&a=logout&forward=&siteid=1"
    const val REGISTER_URL = "index.php?m=member&c=index&a=register&siteid=1"
    const val PERSONAL_URL = "index.php?m=member&c=index"
    const val USER_URL = "play/u{userid}/"
    const val SPACE_URL = "index.php?m=member&c=space"
    const val SUPPORT_URL = "index.php?m=comment&c=index&a=support&format=json"

    fun generatePlayerId(hid: String, part: Int) = "11-$hid-1-$part"

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
