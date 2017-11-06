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
     * Drrr
     */
    const val CREATE_POST_API_URL = "http://45.63.54.11:13450/comment/create"
    const val POSTS_API_URL = "http://45.63.54.11:13450/comments"
    const val CREATE_REPLY_API_URL = "http://45.63.54.11:13450/reply/create/{commentId}"
    const val REPLIES_API_URL = "http://45.63.54.11:13450/replies/{commentId}"
    const val CREATE_VOTE_API_URL = "http://45.63.54.11:13450/vote/{commentId}"

    /*
     * XML
     */
    const val PLAY_URL_API_URL = "http://api.tucao.tv/api/playurl"
    const val DANMU_API_URL = "http://www.tucao.tv/index.php?m=mukio&c=index&a=init"

    /*
     * Raw
     */
    const val INDEX_URL        = "/"
    const val LIST_URL         = "list/{tid}/"
    const val BGM_URL          = "bgm/{year}/{month}/"
    const val SEND_DANMU_URL   = "index.php?m=mukio&c=index&a=post"
    const val COMMENT_URL      = "index.php?m=comment&c=index&a=init&hot=0&iframe=1"
    const val SEND_COMMENT_URL = "index.php?m=comment&c=index&a=post"

    const val USER_INFO_URL  = "api.php?op=user"
    const val CODE_URL       = "api.php?op=checkcode&code_len=4&font_size=14&width=446&height=40"
    const val LOGIN_URL      = "index.php?m=member&c=index&a=login"
    const val LOGOUT_URL     = "index.php?m=member&c=index&a=logout&forward=&siteid=1"
    const val REGISTER_URL   = "index.php?m=member&c=index&a=register&siteid=1"
    const val PERSONAL_URL   = "index.php?m=member&c=index"
    const val USER_URL       = "play/u{userid}/"
    const val SPACE_URL      = "index.php?m=member&c=space"
    const val SUPPORT_URL    = "index.php?m=comment&c=index&a=support&format=json"
    const val SEND_REPLY_URL = "index.php?m=comment&c=index&a=post&replyuid=undefined"

    const val CHANGE_INFORMATION_URL = "index.php?m=member&c=index&a=account_manage_info&t=account"
    const val CHANGE_PASSWORD_URL    = "index.php?m=member&c=index&a=account_manage_password&t=account"
    const val FORGOT_PASSWORD_URL    = "index.php?m=member&c=index&a=public_forget_password&siteid=1"
    const val CHECK_USERNAME_URL     = "index.php?clientid=username&m=member&c=index&a=public_checkname_ajax"
    const val CHECK_NICKNAME_URL     = "index.php?clientid=nickname&m=member&c=index&a=public_checknickname_ajax"
    const val CHECK_EMAIL_URL        = "index.php?clientid=email&m=member&c=index&a=public_checkemail_ajax"
    const val MANAGE_AVATAR_URL      = "index.php?m=member&c=index&a=account_manage_avatar&t=account"
    const val UPLOAD_AVATAR_URL      = "phpsso_server/index.php?m=phpsso&c=index&a=uploadavatar&auth_data=v=1&appid=1"

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
