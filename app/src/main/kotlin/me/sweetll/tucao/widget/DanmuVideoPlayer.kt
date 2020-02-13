package me.sweetll.tucao.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.video.adapter.SettingPagerAdapter
import me.sweetll.tucao.extension.dp2px
import me.sweetll.tucao.extension.formatDanmuOpacityToFloat
import me.sweetll.tucao.extension.formatDanmuSizeToFloat
import me.sweetll.tucao.extension.formatDanmuSpeedToFloat

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
    lateinit var settingTab: TabLayout
    lateinit var settingPager: ViewPager

    lateinit var closeImg: ImageView
    lateinit var sendDanmuText: TextView
    lateinit var sendDanmuLinear: LinearLayout
    lateinit var danmuEdit: EditText
    lateinit var sendDanmuImg: ImageView

    lateinit var jumpLinear: LinearLayout
    lateinit var closeJumpImg: ImageView
    lateinit var jumpTimeText: TextView
    lateinit var jumpText: TextView

    var mLastState = -1
    var needCorrectDanmu = false
    var isShowDanmu = true

    companion object {
        const val TAP = 1

        const val DOUBLE_TAP_TIMEOUT = 250L
        const val DOUBLE_TAP_MIN_TIME = 40L
        const val DOUBLE_TAP_SLOP = 100L
        const val DOUBLE_TAP_SLOP_SQUARE = DOUBLE_TAP_SLOP * DOUBLE_TAP_SLOP
    }

    private var isDoubleTapping = false
    private var isStillDown = false
    private var deferConfirmSingleTap = false
    private var currentDownEvent: MotionEvent? = null
    private var previousUpEvent: MotionEvent? = null

    val gestureHandler = @SuppressLint("HandlerLeak")
    object: Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                TAP -> {
                    if (!isStillDown) {
                        onSingleTapConfirmed()
                    } else {
                        deferConfirmSingleTap = true
                    }
                }
            }
        }
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
                .translationX((280f).dp2px())
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
            .translationX((-280f).dp2px())
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
        danmakuContext?.setDanmakuTransparency(PlayerConfig.loadDanmuOpacity().formatDanmuOpacityToFloat())
        danmakuContext?.setScaleTextSize(PlayerConfig.loadDanmuSize().formatDanmuSizeToFloat())
        danmakuContext?.setScrollSpeedFactor(PlayerConfig.loadDanmuSpeed().formatDanmuSpeedToFloat())
    }

    private fun initView() {
        //初始化弹幕控件
        danmakuContainer = findViewById(R.id.danmaku_container)

        jumpLinear = findViewById(R.id.linear_jump)
        closeJumpImg = findViewById(R.id.img_close_jump)
        jumpTimeText = findViewById(R.id.text_jump_time)
        jumpText = findViewById(R.id.text_jump)
        if (!isIfCurrentIsFullscreen) {
            loadText = findViewById(R.id.text_load)
        } else {
            settingLayout = findViewById(R.id.setting_layout)

            settingButton = findViewById(R.id.btn_setting)
            settingButton.visibility = View.VISIBLE
            settingButton.setOnClickListener {
                showSetting()
            }

            settingPager = findViewById(R.id.pager_setting)
            settingPager.adapter = SettingPagerAdapter(this)
            settingPager.offscreenPageLimit = 3

            settingTab = findViewById(R.id.tab_setting)
            settingTab.setupWithViewPager(settingPager)

            // 顶部发送弹幕栏
            sendDanmuText = findViewById(R.id.text_send_danmu)
            sendDanmuLinear = findViewById(R.id.linear_send_danmu)
            danmuEdit = findViewById(R.id.edit_danmu)
            sendDanmuImg = findViewById(R.id.img_send_danmu)
            closeImg = findViewById(R.id.img_close)

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
                _, _, _ ->
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

        switchDanmu = findViewById<TextView>(R.id.switchDanmu)
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
    }

    fun getOrientationUtils() = mOrientationUtils!!

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull) {
            return super.onTouch(v, event)
        }

        if (v.id == R.id.surface_container) {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val hadTapMessage = gestureHandler.hasMessages(TAP)
                    if (hadTapMessage) {
                        gestureHandler.removeMessages(TAP)
                    }

                    if (currentDownEvent != null && previousUpEvent != null && hadTapMessage &&
                            isConsideredDoubleTap(currentDownEvent!!, previousUpEvent!!, event)) {
                        isDoubleTapping = true
                    } else {
                        gestureHandler.sendEmptyMessageDelayed(TAP, DOUBLE_TAP_TIMEOUT)
                    }
                    currentDownEvent?.recycle()
                    currentDownEvent = MotionEvent.obtain(event)

                    isStillDown = true
                    deferConfirmSingleTap = false
                }
                MotionEvent.ACTION_UP -> {
                    isStillDown = false
                    if (isDoubleTapping) {
                        onDoubleTap()
                    } else if (deferConfirmSingleTap) {
                        onSingleTapConfirmed()
                    }
                    previousUpEvent?.recycle()
                    previousUpEvent = MotionEvent.obtain(event)
                    isDoubleTapping = false
                    deferConfirmSingleTap = false
                }
            }
        }

        return super.onTouch(v, event)
    }

    private fun isConsideredDoubleTap(firstDown: MotionEvent, firstUp: MotionEvent,
                                      secondDown: MotionEvent): Boolean {
        val deltaTime = secondDown.eventTime - firstUp.eventTime
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false
        }

        val deltaX = firstDown.x - secondDown.x
        val deltaY = firstDown.y - secondDown.y
        return (deltaX * deltaX + deltaY * deltaY < DOUBLE_TAP_SLOP_SQUARE)
    }

    private fun onDoubleTap() {
        if (currentState == GSYVideoPlayer.CURRENT_STATE_PLAYING || currentState == GSYVideoPlayer.CURRENT_STATE_PAUSE) {
            mStartButton.performClick()
        }
    }

    private fun onSingleTapConfirmed() {
        startDismissControlViewTimer()
        if (!mChangePosition && !mChangeVolume && !mBrightness) {
            onClickUiToggle()
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
                .setScaleTextSize(PlayerConfig.loadDanmuSize().formatDanmuSizeToFloat())
                .setScrollSpeedFactor(PlayerConfig.loadDanmuSpeed().formatDanmuSpeedToFloat())
                .setDanmakuTransparency(PlayerConfig.loadDanmuOpacity().formatDanmuOpacityToFloat())

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
                    danmakuView!!.pause()
                }
            }

        })
        danmakuView!!.prepare(danmuParser, danmakuContext)
        danmakuView!!.enableDanmakuDrawingCache(true)
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
                if (mLastState == mCurrentState) {
                    pauseDanmu()
                }
            }
            GSYVideoPlayer.CURRENT_STATE_PLAYING  -> {
                if (mLastState != mCurrentState) {
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
