package me.sweetll.tucao.business.video.fragment

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.widget.BubbleSeekBar
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingPlayFragment(val player: DanmuVideoPlayer): BaseFragment() {

    lateinit var speedSeek: BubbleSeekBar
    lateinit var speedText: TextView

    lateinit var rotateSwitch: SwitchCompat
    lateinit var codecSwitch: SwitchCompat
    lateinit var codecHelpImg: ImageView

    val codecHelpDialog: DialogPlus by lazy {
        val codecHelpView = LayoutInflater.from(context).inflate(R.layout.dialog_codec_help, null)
        val codecHelpText = codecHelpView.findViewById<TextView>(R.id.text_codec_help)
        codecHelpText.movementMethod = ScrollingMovementMethod()
        DialogPlus.newDialog(context)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_play, container, false)
        speedText = view.findViewById(R.id.text_speed)
        speedSeek = view.findViewById(R.id.seek_speed)
        rotateSwitch = view.findViewById(R.id.switch_rotate)
        codecSwitch = view.findViewById(R.id.switch_codec)
        codecHelpImg = view.findViewById(R.id.img_codec_help)

        speedText.text = "${PlayerConfig.loadPlayerSpeed()}"
        speedSeek.setProgress(PlayerConfig.loadPlayerSpeed())
        codecSwitch.isChecked = PlayerConfig.loadHardCodec()
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        speedSeek.configBuilder
                .min(0.5f)
                .max(2.0f)
                .progress(1.0f)
                .floatType()
                .trackColor(ContextCompat.getColor(context, R.color.white))
                .secondTrackColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .sectionTextColor(ContextCompat.getColor(context, R.color.white))
                .thumbTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .thumbTextSize(18)
                .showThumbText()
                .sectionCount(3)
                .showSectionText()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .autoAdjustSectionMark()
                .build()

        speedSeek.onProgressChangedListener = object: BubbleSeekBar.OnProgressChangedListenerAdapter() {
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
    }
}
