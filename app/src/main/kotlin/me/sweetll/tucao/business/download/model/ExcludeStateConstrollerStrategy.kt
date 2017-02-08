package me.sweetll.tucao.business.download.model

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

class ExcludeStateConstrollerStrategy: ExclusionStrategy {
    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false

    override fun shouldSkipField(f: FieldAttributes): Boolean {
        return f.declaringClass == StateController::class.java
    }

}