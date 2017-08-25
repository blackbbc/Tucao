package me.sweetll.tucao.business.video.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.extension.formatDanmuOpacityToString
import me.sweetll.tucao.extension.formatDanmuSizeToString
import me.sweetll.tucao.extension.formatDanmuSpeedToString
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingDanmuViewFactory() {

    companion object {

        fun create(player: DanmuVideoPlayer, container: ViewGroup): View {
            var danmuSizeProgress = PlayerConfig.loadDanmuSize() // 1.00 0.50~2.00
            var danmuOpacityProgress = PlayerConfig.loadDanmuOpacity() // 100% 20%~100%
            var danmuSpeedProgress = PlayerConfig.loadDanmuSpeed() // 1.00 0.3~2.00

            val view = LayoutInflater.from(player.context).inflate(R.layout.fragment_setting_danmu, container, false)

            val danmuOpacityText: TextView = view.findViewById(R.id.text_danmu_opacity)
            val danmuOpacitySeek: SeekBar = view.findViewById(R.id.seek_danmu_opacity)
            val danmuSizeText: TextView = view.findViewById(R.id.text_danmu_size)
            val danmuSizeSeek: SeekBar = view.findViewById(R.id.seek_danmu_size)
            val danmuSpeedText: TextView = view.findViewById(R.id.text_danmu_speed)
            val danmuSpeedSeek: SeekBar = view.findViewById(R.id.seek_danmu_speed)

            danmuOpacityText.text = danmuOpacityProgress.formatDanmuOpacityToString()
            danmuOpacitySeek.progress = danmuOpacityProgress

            danmuSizeText.text = danmuSizeProgress.formatDanmuSizeToString()
            danmuSizeSeek.progress = danmuSizeProgress

            danmuSpeedText.text = danmuSpeedProgress.formatDanmuSpeedToString()
            danmuSpeedSeek.progress = danmuSpeedProgress

            danmuOpacitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        danmuOpacityProgress = progress
                        PlayerConfig.saveDanmuOpacity(danmuOpacityProgress)
                        danmuOpacityText.text = danmuOpacityProgress.formatDanmuOpacityToString()
                        player.configDanmuStyle()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            danmuSizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        danmuSizeProgress = progress
                        danmuSizeText.text = danmuSizeProgress.formatDanmuSizeToString()
                        PlayerConfig.saveDanmuSize(danmuSizeProgress)
                        player.configDanmuStyle()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            danmuSpeedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        danmuSpeedProgress = progress
                        danmuSpeedText.text = danmuSpeedProgress.formatDanmuSpeedToString()
                        PlayerConfig.saveDanmuSpeed(danmuSpeedProgress)
                        player.configDanmuStyle()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            return view
        }
    }
}
