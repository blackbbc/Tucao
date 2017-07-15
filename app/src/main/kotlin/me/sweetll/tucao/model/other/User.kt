package me.sweetll.tucao.model.other

import com.squareup.moshi.Moshi
import me.sweetll.tucao.extension.edit
import me.sweetll.tucao.extension.getSharedPreference

class User() {

    var email: String = ""
        get() = field
        set(value) {
            field = value
            val userJson = adapter.toJson(this)
            SP_USER.getSharedPreference().edit {
                putString(KEY_USER, userJson)
            }
        }

    var name: String = ""
        get() = field
        set(value) {
            field = value
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
}
