package me.sweetll.tucao.extension

import androidx.annotation.NonNull
import androidx.databinding.Observable
import androidx.databinding.ObservableField

class NonNullObservableField<T: Any>(
        value: T, vararg dependencies: Observable
): ObservableField<T>(*dependencies) {
    init {
        set(value)
    }

    override fun get(): T = super.get()!!

    @Suppress("RedundantOverride")
    override fun set(@NonNull value: T) = super.set(value)
}