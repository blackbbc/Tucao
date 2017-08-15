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

class SettingDanmuFragment(val player: DanmuVideoPlayer): BaseFragment() {

    lateinit var danmuOpacityText: TextView
    lateinit var danmuOpacitySeek: SeekBar
    lateinit var danmuSizeText: TextView
    lateinit var danmuSizeSeek: SeekBar
    lateinit var danmuSpeedText: TextView
    lateinit var danmuSpeedSeek: SeekBar

    var danmuSizeProgress = PlayerConfig.loadDanmuSize() // 1.00 0.50~2.00
    var danmuOpacityProgress = PlayerConfig.loadDanmuOpacity() // 100% 20%~100%
    var danmuSpeedProgress = PlayerConfig.loadDanmuSpeed() // 1.00 0.3~2.00


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_danmu, container, false)
        danmuOpacityText = view.findViewById(R.id.text_danmu_opacity)
        danmuOpacitySeek = view.findViewById(R.id.seek_danmu_opacity)
        danmuSizeText = view.findViewById(R.id.text_danmu_size)
        danmuSizeSeek = view.findViewById(R.id.seek_danmu_size)
        danmuSpeedText = view.findViewById(R.id.text_danmu_speed)
        danmuSpeedSeek = view.findViewById(R.id.seek_danmu_speed)

        danmuOpacityText.text = danmuOpacityProgress.formatDanmuOpacityToString()
        danmuOpacitySeek.progress = danmuOpacityProgress

        danmuSizeText.text = danmuSizeProgress.formatDanmuSizeToString()
        danmuSizeSeek.progress = danmuSizeProgress

        danmuSpeedText.text = danmuSpeedProgress.formatDanmuSpeedToString()
        danmuSpeedSeek.progress = danmuSpeedProgress

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        danmuOpacitySeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    danmuOpacityProgress = progress
                    player.configDanmuStyle()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        danmuSizeSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    danmuSizeProgress = progress
                    player.configDanmuStyle()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        danmuSpeedSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    danmuSpeedProgress = progress
                    player.configDanmuStyle()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }
}
