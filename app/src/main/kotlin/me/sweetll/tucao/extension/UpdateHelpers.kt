package me.sweetll.tucao.extension

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.BuildConfig
import java.io.File

object UpdateHelpers {

    private val sp by lazy {
        AppApplication.get().getSharedPreferences("update", Context.MODE_PRIVATE)
    }

    fun newVersion(): Boolean {
        return sp.getInt("version_code", 0) != BuildConfig.VERSION_CODE
    }

    /*
     * 114 => 1.1.2
     */
    fun needClearUserData(): Boolean {
        return sp.getInt("version_code", 0) < 114
    }

    fun updateVersion() {
        sp.edit {
            putInt("version_code", BuildConfig.VERSION_CODE)
        }
    }

    fun clearUserData() {
        val cache = AppApplication.get().cacheDir
        val appDir = File(cache.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                if (s != "lib") {
                    deleteDir(File(appDir, s))
                    Log.i("FFF", "File /data/data/APP_PACKAGE/$s DELETED")
                }
            }
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }
}
