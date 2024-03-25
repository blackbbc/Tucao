package me.sweetll.tucao.extension

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

fun <T> ObservableField<T>.observe(lifecycle: Lifecycle, onChange: (T) -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            get()?.let { onChange(it) }
        }
    }
    addOnPropertyChangedCallback(callback)
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                lifecycle.removeObserver(this)
                removeOnPropertyChangedCallback(callback)
            }
        }
    })
}

fun ObservableBoolean.observe(lifecycle: Lifecycle, onChange: (Boolean) -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }
    val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            onChange(get())
        }
    }
    addOnPropertyChangedCallback(callback)
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                lifecycle.removeObserver(this)
                removeOnPropertyChangedCallback(callback)
            }
        }
    })
}