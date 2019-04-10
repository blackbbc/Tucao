package me.sweetll.tucao.extension

import io.reactivex.subjects.BehaviorSubject

class Variable<T>(defaultValue: T) {
    val stream: BehaviorSubject<T> = BehaviorSubject.createDefault(defaultValue)
    var value: T = defaultValue
        set(value) {
            field = value
            stream.onNext(value)
        }
}