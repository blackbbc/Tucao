package me.sweetll.tucao.business.video.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.widget.BubbleSeekBar
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingPlayViewFactory() {

    companion object {

        fun create(player: DanmuVideoPlayer, container: ViewGroup): View {
            val view = LayoutInflater.from(player.context).inflate(R.layout.fragment_setting_play, container, false)

            val codecHelpDialog: DialogPlus by lazy {
                val codecHelpView = LayoutInflater.from(player.context).inflate(R.layout.dialog_codec_help, null)
                val codecHelpText = codecHelpView.findViewById<TextView>(R.id.text_codec_help)
                codecHelpText.movementMethod = ScrollingMovementMethod()
                DialogPlus.newDialog(player.context)
                        .setContentHolder(ViewHolder(codecHelpView))
                        .setGravity(Gravity.CENTER)
                        .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setContentBackgroundResource(R.drawable.bg_round_white_rectangle)
                        .setOverlayBackgroundResource(R.color.mask)
                        .setOnClickListener {
                            dialog, view ->
                            when (view.id) {
                                R.id.img_close -> dialog.dismiss()
                            }
                        }
                        .create()
            }

            val speedText: TextView = view.findViewById(R.id.text_speed)
            val speedSeek: BubbleSeekBar = view.findViewById(R.id.seek_speed)

            val rotateSwitch: SwitchCompat = view.findViewById(R.id.switch_rotate)
            val codecSwitch: SwitchCompat = view.findViewById(R.id.switch_codec)
            val codecHelpImg: ImageView = view.findViewById(R.id.img_codec_help)

            speedText.text = "${PlayerConfig.loadPlayerSpeed()}"
            speedSeek.setProgress(PlayerConfig.loadPlayerSpeed())
            codecSwitch.isChecked = PlayerConfig.loadHardCodec()

            speedSeek.configBuilder
                    .min(0.5f)
                    .max(2.0f)
                    .progress(1.0f)
                    .floatType()
                    .trackColor(ContextCompat.getColor(player.context, R.color.white))
                    .secondTrackColor(ContextCompat.getColor(player.context, R.color.colorPrimary))
                    .sectionTextColor(ContextCompat.getColor(player.context, R.color.white))
                    .thumbTextColor(ContextCompat.getColor(player.context, R.color.colorPrimary))
                    .thumbTextSize(18)
                    .showThumbText()
                    .sectionCount(3)
                    .showSectionText()
                    .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                    .autoAdjustSectionMark()
                    .build()

            speedSeek.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListenerAdapter() {
                override fun getProgressOnFinally(progress: Int, progressFloat: Float) {
                    player.speed = progressFloat
                    speedText.text = "$progressFloat"
                }
            }

            codecSwitch.setOnCheckedChangeListener {
                _, checked ->
                PlayerConfig.saveHardCodec(checked)
            }

            codecHelpImg.setOnClickListener {
                codecHelpDialog.show()
            }

            rotateSwitch.isChecked = player.getOrientationUtils().currentScreenType != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            rotateSwitch.setOnCheckedChangeListener {
                _, _ ->
                player.getOrientationUtils().toggleLandReverse()
            }

            return view
        }
    }
}
