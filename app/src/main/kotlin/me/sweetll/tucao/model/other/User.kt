package me.sweetll.tucao.model.other

import com.squareup.moshi.Moshi
import me.sweetll.tucao.extension.edit
import me.sweetll.tucao.extension.getSharedPreference
import kotlin.reflect.KProperty

class User() {

    var email: String by SPDelegate()

    var name: String by SPDelegate()

    var avatar: String by SPDelegate()

    fun isValid() = email.isNotEmpty()

    fun invalidate() {
        email = ""
        name = ""
        avatar = ""
        save()
    }

    private fun save() {
        val userJson = adapter.toJson(this)
        SP_USER.getSharedPreference().edit {
            putString(KEY_USER, userJson)
        }
    }

    companion object {
        const val SP_USER = "user"
        const val KEY_USER = "user"

        private val adapter by lazy {
            val moshi = Moshi.Builder()
                    .build()
            moshi.adapter(User::class.java)
        }

        fun load(): User {
            try {
                val userJson = SP_USER.getSharedPreference().getString(KEY_USER ,"")
                val user = adapter.fromJson(userJson)
                return user!!
            } catch (e: Exception) {
                return User()
            }
        }
    }

    inner class SPDelegate {
        var value: String = ""

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            this.value = value
            save()
        }
    }
}
