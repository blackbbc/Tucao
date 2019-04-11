package me.sweetll.tucao

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).

 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    @Throws(Exception::class)
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    @Throws(Exception::class)
    fun rx_base() {
        Single.just(1)
                .subscribe()
        Observable.just(1)
                .map { x -> 2 * x }
                .subscribeOn(Schedulers.io())
                .subscribe {
                    print(it)
                }
    }

}