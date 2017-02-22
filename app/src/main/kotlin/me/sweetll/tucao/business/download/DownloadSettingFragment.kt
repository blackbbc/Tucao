package me.sweetll.tucao.business.download

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import com.tbruyelle.rxpermissions2.RxPermissions
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.business.explorer.FileExplorerActivity
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.edit
import me.sweetll.tucao.extension.toast
import java.io.File

class DownloadSettingFragment: PreferenceFragment() {
    val REQUEST_DOWNLOAD_PATH = 1

    lateinit var downloadPathPref: Preference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_download)

        downloadPathPref = findPreference("download_path")
        downloadPathPref.summary = DownloadHelpers.getDownloadFolder().absolutePath
        downloadPathPref.setOnPreferenceClickListener {
            RxPermissions(activity)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe {
                        granted ->
                        if (granted) {
                            val intent = Intent(activity, FileExplorerActivity::class.java)
                            startActivityForResult(intent, REQUEST_DOWNLOAD_PATH)
                        } else {
                            "请授予读写存储卡权限".toast()
                        }
                    }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DOWNLOAD_PATH) {
            if (resultCode == Activity.RESULT_OK) {
                val folder: File = data!!.getSerializableExtra("folder") as File
                val sharedPref = PreferenceManager.getDefaultSharedPreferences(AppApplication.get())
                sharedPref.edit {
                    putString("download_path", folder.absolutePath)
                }
                downloadPathPref.summary = folder.absolutePath
                // TODO: 下载任务需要暂停嘛？
            }
        }
    }
}
