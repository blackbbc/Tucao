package me.sweetll.tucao.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Service
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.utils.OrientationUtils

import com.shuyu.gsyvideoplayer.video.PreviewGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.GSYBaseVideoPlayer
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView

import me.sweetll.tucao.R
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import me.sweetll.tucao.extension.dp2px
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.toast

class DanmuVideoPlayer : PreviewGSYVideoPlayer {
    var loadText: TextView? = null
    var danmakuContext: DanmakuContext? = null
    var danmuUri: String? = null
    var danmuParser: BaseDanmakuParser? = null

    lateinit var danmakuContainer: FrameLayout
    var danmakuView: DanmakuView? = null

    lateinit var settingLayout: View
    lateinit var switchDanmu: TextView
    lateinit var settingButton: Button
    lateinit var danmuOpacityText: TextView
    lateinit var danmuOpacitySeek: SeekBar
    lateinit var danmuSizeText: TextView
    lateinit var danmuSizeSeek: SeekBar
    lateinit var rotateSwitch: SwitchCompat
    lateinit var codecSwitch: SwitchCompat
    lateinit var codecHelpImg: ImageView

    lateinit var speedSeek: BubbleSeekBar
    lateinit var speedText: TextView

    lateinit var closeImg: ImageView
    lateinit var sendDanmuText: TextView
    lateinit var sendDanmuLinear: LinearLayout
    lateinit var danmuEdit: EditText
    lateinit var sendDanmuImg: ImageView

    lateinit var jumpLinear: LinearLayout
    lateinit var closeJumpImg: ImageView
    lateinit var jumpTimeText: TextView
    lateinit var jumpText: TextView

    var danmuSizeProgress = PlayerConfig.loadDanmuSize() // 1.00 0.50~2.00
    var danmuOpacityProgress = PlayerConfig.loadDanmuOpacity() // 100% 20%~100%

    fun Int.formatDanmuSizeToString(): String = String.format("%.2f", this.formatDanmuSizeToFloat())
    fun Int.formatDanmuSizeToFloat(): Float = (this + 50) / 100f

    fun Int.formatDanmuOpacityToString(): String = String.format("%d%%", this + 20)
    fun Int.formatDanmuOpacityToFloat(): Float = (this + 20) / 100f

    var mLastState = -1
    var needCorrectDanmu = false
    var isShowDanmu = true

    val codecHelpDialog: DialogPlus by lazy {
        val codecHelpView = LayoutInflater.from(context).inflate(R.layout.dialog_codec_help, null)
        val codecHelpText = codecHelpView.findViewById(R.id.text_codec_help) as TextView
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

    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun init(context: Context) {
        super.init(context)
        initView()
    }

    fun showDanmu(show: Boolean) {
        isShowDanmu = show
        if (danmakuView != null && danmakuView!!.isPrepared) {
            if (show) {
                switchDanmu.text = "弹幕开"
                danmakuView!!.show()
            } else {
                switchDanmu.text = "弹幕关"
                danmakuView!!.hide()
            }
        }
    }

    fun showSetting() {
        settingLayout.animate()
                .translationX(0f)
                .setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        settingLayout.visibility = View.VISIBLE
                    }
                })
                .start()
        cancelDismissControlViewTimer()
    }

    fun hideSetting() {
            settingLayout.animate()
                .translationX((250f).dp2px())
                .setDuration(200)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        settingLayout.visibility = View.INVISIBLE
                    }
                })
                .start()
    }

    fun showJump(position: Int) {
        val minute: Int = position / 1000 / 60
        val seconds: Int = position / 1000 % 60
        jumpTimeText.text = "记忆您上次播放到%d:%02d".format(minute, seconds)
        jumpText.setOnClickListener {
            GSYVideoManager.instance().mediaPlayer.seekTo(position.toLong())
            hideJump()
        }
        jumpLinear.animate()
                .translationX(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        jumpLinear.visibility = View.VISIBLE
                    }
                })
                .start()
    }

    fun hideJump() {
        jumpLinear.animate()
            .translationX((-250f).dp2px())
            .setDuration(400)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    jumpLinear.visibility = View.INVISIBLE
                }
            })
            .start()
    }

    // 重新修改弹幕样式
    fun configDanmuStyle() {
        if (isIfCurrentIsFullscreen) {
            danmuOpacityText.text = danmuOpacityProgress.formatDanmuOpacityToString()
            danmuOpacitySeek.progress = danmuOpacityProgress

            danmuSizeText.text = danmuSizeProgress.formatDanmuSizeToString()
            danmuSizeSeek.progress = danmuSizeProgress

            codecSwitch.isChecked = PlayerConfig.loadHardCodec()
        }

        PlayerConfig.saveDanmuOpacity(danmuOpacityProgress)
        PlayerConfig.saveDanmuSize(danmuSizeProgress)

        danmakuContext?.setDanmakuTransparency(danmuOpacityProgress.formatDanmuOpacityToFloat())
        danmakuContext?.setScaleTextSize(danmuSizeProgress.formatDanmuSizeToFloat())
    }

    private fun initView() {
        //初始化弹幕控件
        danmakuContainer = findViewById(R.id.danmaku_container) as FrameLayout

        jumpLinear = findViewById(R.id.linear_jump) as LinearLayout
        closeJumpImg = findViewById(R.id.img_close_jump) as ImageView
        jumpTimeText = findViewById(R.id.text_jump_time) as TextView
        jumpText = findViewById(R.id.text_jump) as TextView
        if (!isIfCurrentIsFullscreen) {
            loadText = findViewById(R.id.text_load) as TextView
        } else {
            settingLayout = findViewById(R.id.setting_layout)
            danmuOpacityText = findViewById(R.id.text_danmu_opacity) as TextView
            danmuOpacitySeek = findViewById(R.id.seek_danmu_opacity) as SeekBar
            danmuSizeText = findViewById(R.id.text_danmu_size) as TextView
            danmuSizeSeek = findViewById(R.id.seek_danmu_size) as SeekBar
            rotateSwitch = findViewById(R.id.switch_rotate) as SwitchCompat
            codecSwitch = findViewById(R.id.switch_codec) as SwitchCompat
            codecHelpImg = findViewById(R.id.img_codec_help) as ImageView

            settingButton = findViewById(R.id.btn_setting) as Button
            settingButton.visibility = View.VISIBLE
            settingButton.setOnClickListener {
                showSetting()
            }

            danmuOpacitySeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        danmuOpacityProgress = progress
                        configDanmuStyle()
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
                        configDanmuStyle()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })

            speedText = findViewById(R.id.text_speed) as TextView
            speedSeek = findViewById(R.id.seek_speed) as BubbleSeekBar
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
                    this@DanmuVideoPlayer.speed = progressFloat
                    speedText.text = "$progressFloat"
                }
            }

            codecSwitch.setOnCheckedChangeListener {
                button, checked ->
                PlayerConfig.saveHardCodec(checked)
                "设置成功，重新打开视频后生效".toast()
            }

            codecHelpImg.setOnClickListener {
                codecHelpDialog.show()
            }

            // 顶部发送弹幕栏
            sendDanmuText = findViewById(R.id.text_send_danmu) as TextView
            sendDanmuLinear = findViewById(R.id.linear_send_danmu) as LinearLayout
            danmuEdit = findViewById(R.id.edit_danmu) as EditText
            sendDanmuImg = findViewById(R.id.img_send_danmu) as ImageView
            closeImg = findViewById(R.id.img_close) as ImageView

            sendDanmuText.setOnClickListener {
                if (sendDanmuLinear.visibility == View.VISIBLE) {
                    sendDanmuLinear.visibility = View.GONE
                } else {
                    danmuEdit.setText("")
                    sendDanmuLinear.visibility = View.VISIBLE
                    danmuEdit.requestFocus()
                    cancelDismissControlViewTimer()
                }
            }

            danmuEdit.setOnEditorActionListener {
                v, actionId, event ->
                val danmuContent = danmuEdit.editableText.toString()
                if (danmuContent.isNotEmpty()) {
                    sendDanmu(danmuContent)
                }
                false
            }

            sendDanmuImg.setOnClickListener {
                val danmuContent = danmuEdit.editableText.toString()
                if (danmuContent.isNotEmpty()) {
                    sendDanmu(danmuContent)
                }
            }

            closeImg.setOnClickListener {
                hideAllWidget()
                hideSoftKeyBoard()
            }

        }

        // 左侧跳转栏
        closeJumpImg.setOnClickListener {
            hideJump()
        }

        loadText?.let {
            it.visibility = View.VISIBLE
        }

        switchDanmu = findViewById(R.id.switchDanmu) as TextView
        switchDanmu.setOnClickListener {
            showDanmu(!isShowDanmu)
        }
    }

    private fun sendDanmu(content: String) {
        (context as DanmuPlayerHolder).onSendDanmu(currentPositionWhenPlaying/1000f, content)

        danmakuView?.let {
            val danmaku = danmakuContext!!.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL)
            danmaku.text = content
            danmaku.padding = 5
            danmaku.priority = 1  // 一定会显示
            danmaku.isLive = false
            danmaku.time = it.currentTime + 1200
            danmaku.textSize = 25f * (danmuParser!!.displayer.density - 0.6f)
            danmaku.textColor = Color.RED
            danmaku.textShadowColor = Color.WHITE
            danmaku.borderColor = Color.GREEN
            it.addDanmaku(danmaku)
        }

        hideAllWidget()
        hideSoftKeyBoard()
    }

    fun setOrientationUtils(orientationUtils: OrientationUtils) {
        mOrientationUtils = orientationUtils

        if (isIfCurrentIsFullscreen) {
            rotateSwitch.isChecked = mOrientationUtils.currentScreenType != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            rotateSwitch.setOnCheckedChangeListener {
                button, checked ->
                mOrientationUtils.toggleLandReverse()
            }
        }
    }

    override fun setUp(url: String): Boolean {
        return super.setUp(if (url.startsWith("http")) "async:$url" else url)
    }

    fun setUpDanmu(uri: String) {
        danmakuView = DanmakuView(context)
        val lp = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        danmakuContainer.removeAllViews()
        danmakuContainer.addView(danmakuView, lp)

        danmuUri = uri
        val overlappingEnablePair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to true,
                BaseDanmaku.TYPE_FIX_TOP to true
        )

        danmakuContext = DanmakuContext.create()
        danmakuContext!!.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .preventOverlapping(overlappingEnablePair)
                .setScaleTextSize(danmuSizeProgress.formatDanmuSizeToFloat())
                .setDanmakuTransparency(danmuOpacityProgress.formatDanmuOpacityToFloat())

        if (!mIfCurrentIsFullscreen) {
            val maxLinesPair = mapOf(BaseDanmaku.TYPE_SCROLL_RL to 5)
            danmakuContext!!.setMaximumLines(maxLinesPair)
        }

        danmuParser = createParser(uri)
        danmakuView!!.setCallback(object : DrawHandler.Callback {
            override fun danmakuShown(danmaku: BaseDanmaku?) {

            }

            override fun updateTimer(timer: DanmakuTimer) {

            }

            override fun drawingFinished() {

            }

            override fun prepared() {
                loadText?.let {
                    it.post {
                        it.text = it.text.replace("全舰弹幕装填...".toRegex(), "全舰弹幕装填...[完成]")
                    }
                }
                danmakuView!!.start(currentPositionWhenPlaying.toLong())
                if (currentState != GSYVideoPlayer.CURRENT_STATE_PLAYING) {
                    danmakuView!!.postDelayed({
                        danmakuView!!.pause()
                    }, 32)
                }
            }

        })
        danmakuView!!.bindClockProvider {
            currentPositionWhenPlaying.toLong()
        }
        danmakuView!!.prepare(danmuParser, danmakuContext)
        danmakuView!!.enableDanmakuDrawingCache(false) // TODO: 改回true
        configDanmuStyle()
    }

    private fun createParser(uri: String): BaseDanmakuParser {
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)

        try {
            loader.load(uri)
        } catch (e: IllegalDataException) {
            e.printStackTrace()
        }
        val parser = TucaoDanmukuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        danmakuView?.hide()

        val player = super.startWindowFullscreen(context, actionBar, statusBar) as DanmuVideoPlayer
        player.speed = speed

        danmuUri?.let {
            player.showDanmu(isShowDanmu)
            player.danmuSizeProgress = danmuSizeProgress
            player.danmuOpacityProgress = danmuOpacityProgress
            player.setUpDanmu(it)
            player.configDanmuStyle()
        }

        return player
    }

    override fun resolveFullVideoShow(context: Context?, gsyVideoPlayer: GSYBaseVideoPlayer?, frameLayout: FrameLayout?) {
        (gsyVideoPlayer as DanmuVideoPlayer).setOrientationUtils(mOrientationUtils)
        super.resolveFullVideoShow(context, gsyVideoPlayer, frameLayout)
    }

    override fun getLayoutId(): Int {
        if (mIfCurrentIsFullscreen) {
            return R.layout.danmu_video_land
        }
        return R.layout.danmu_video
    }

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val imageView = mStartButton as ImageView
            if (mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector)
            } else if (mCurrentState == GSYVideoPlayer.CURRENT_STATE_ERROR) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector)
            } else {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector)
            }
        } else {
            super.updateStartImage()
        }
    }

    override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        gsyVideoPlayer?.let {
            (it as DanmuVideoPlayer)
            showDanmu(it.isShowDanmu)

            danmuSizeProgress = it.danmuSizeProgress
            danmuOpacityProgress = it.danmuOpacityProgress

            configDanmuStyle()

            speed = it.speed
        }
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
    }

    override fun onClickUiToggle() {
        super.onClickUiToggle()
        if (mIfCurrentIsFullscreen) {
            if (mBottomContainer.visibility != View.GONE) {
                if (settingLayout.visibility == View.VISIBLE) {
                    hideSetting()
                }
                if (sendDanmuLinear.visibility == View.VISIBLE) {
                    sendDanmuLinear.visibility = View.GONE
                }
            }
        }
    }

    override fun hideAllWidget() {
        super.hideAllWidget()

        if (mIfCurrentIsFullscreen) {
            sendDanmuLinear.visibility = View.GONE
            if (mBottomContainer.visibility != View.GONE && settingLayout.visibility == View.VISIBLE) {
                hideSetting()
            }
        }
    }

    private fun hideSoftKeyBoard() {
            val imm = context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
            if (mIfCurrentIsFullscreen) {
                CommonUtil.hideNavKey(context)
            }
    }

    fun onVideoPause(isPlay: Boolean, isComplete: Boolean = false) {
        onVideoPause()
        if (isPlay) {
            // 在这里保存播放进度
            if (isComplete) {
                (context as DanmuPlayerHolder).onSavePlayHistory(0)
            } else {
                (context as DanmuPlayerHolder).onSavePlayHistory(currentPositionWhenPlaying)
            }
        }
    }

    override fun onVideoPause() {
        super.onVideoPause()
        if (danmakuView != null && danmakuView!!.isPrepared) {
            danmakuView!!.pause()
        }
    }

    override fun onVideoResume() {
        super.onVideoResume()
        if (mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING && danmakuView != null && danmakuView!!.isPrepared && danmakuView!!.isPaused) {
            danmakuView!!.resume()
        }
    }

    fun onVideoDestroy() {
        danmakuView?.release()
    }

    /*
     * 在这里更新状态
     * 每隔300ms刷新一次
     */
    override fun setTextAndProgress() {
        super.setTextAndProgress()
        if (needCorrectDanmu) {
            needCorrectDanmu = false
            seekDanmu()
        }
        when (mCurrentState) {
            GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START, GSYVideoPlayer.CURRENT_STATE_PAUSE -> {
                if (mLastState == GSYVideoPlayer.CURRENT_STATE_PLAYING || mLastState == -1) {
                    pauseDanmu()
                }
            }
            GSYVideoPlayer.CURRENT_STATE_PLAYING  -> {
                if (mLastState == GSYVideoPlayer.CURRENT_STATE_PAUSE || mLastState == GSYVideoPlayer.CURRENT_STATE_PLAYING_BUFFERING_START || mLastState == -1) {
                    resumeDanmu()
                }
            }
        }
        mLastState = mCurrentState
    }

    override fun onPrepared() {
        super.onPrepared()
        // 隐藏状态栏
        CommonUtil.hideSupportActionBar(context, true, true)
        // 隐藏LoadText
        loadText?.let {
            it.visibility = View.GONE
        }
    }

    fun resumeDanmu() {
        if (danmakuView != null && danmakuView!!.isPrepared) {
            danmakuView!!.resume()
        }
    }

    fun pauseDanmu() {
        if (danmakuView != null && danmakuView!!.isPrepared) {
            danmakuView!!.pause()
        }
    }

    fun seekDanmu() {
        if (danmakuView != null && danmakuView!!.isPrepared) {
            "seekDanmu".logD()
            danmakuView!!.seekTo(currentPositionWhenPlaying.toLong())
        }
    }

    override fun onSeekComplete() {
        super.onSeekComplete()
        needCorrectDanmu = true
    }

    interface DanmuPlayerHolder {
        fun onSendDanmu(stime: Float, message: String)
        fun onSavePlayHistory(position: Int)
    }
}
