package me.sweetll.tucao.business.download

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import me.sweetll.tucao.R
import me.sweetll.tucao.business.explorer.FileExplorerActivity
import me.sweetll.tucao.extension.toast

class DownloadSettingFragment: PreferenceFragment() {
    val REQUEST_DOWNLOAD_PATH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings_download)

        val downloadPathPref = findPreference("download_path")
        downloadPathPref.setOnPreferenceClickListener {
            RxPermissions(activity)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe {
                        granted ->
                        if (granted) {
                            val intent = Intent(activity, FileExplorerActivity::class.java)
                            activity.startActivityForResult(intent, REQUEST_DOWNLOAD_PATH)
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

        }
    }
}
