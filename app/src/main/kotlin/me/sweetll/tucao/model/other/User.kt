package me.sweetll.tucao.model.other

import com.squareup.moshi.Moshi
import me.sweetll.tucao.extension.edit
import me.sweetll.tucao.extension.getSharedPreference

class User() {

    var email: String = ""
        set(value) { field = value;save() }
    var name: String = ""
        set(value) { field = value;save() }
    var avatar: String = ""
        set(value) { field = value;save() }
    var level: Int = 0
        set(value) { field = value;save() }
    var signature: String = ""
        set(value) { field = value;save() }

    fun isValid() = email.isNotEmpty()

    fun invalidate() {
        email = ""
        name = ""
        avatar = ""
        level = 0
        signature = ""
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
}
