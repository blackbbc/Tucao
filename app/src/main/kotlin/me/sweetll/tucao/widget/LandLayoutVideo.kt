package me.sweetll.tucao.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.utils.CommonUtil

import com.shuyu.gsyvideoplayer.video.CustomGSYVideoPlayer
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
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.toast
import java.io.InputStream

class LandLayoutVideo : CustomGSYVideoPlayer {
    lateinit var danmakuView: DanmakuView
    lateinit var danmuStream: InputStream

    var mLastState = -1
    var needCorrectDanmu = false

    constructor(context: Context, fullFlag: Boolean?) : super(context, fullFlag)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun init(context: Context) {
        super.init(context)
        initView()
    }

    private fun initView() {
        //初始化弹幕控件
        danmakuView = findViewById(R.id.danmaku) as DanmakuView
    }

    fun setUpDanmu(inputStream: InputStream) {
        danmuStream = inputStream
        val maxLinesPair = mapOf(BaseDanmaku.TYPE_SCROLL_RL to 5)
        val overlappingEnablePair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to true,
                BaseDanmaku.TYPE_FIX_TOP to true
        )

        val danmakuContext = DanmakuContext.create()
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)

        val parser = createParser(inputStream)
        danmakuView.setCallback(object : DrawHandler.Callback {
            override fun danmakuShown(danmaku: BaseDanmaku?) {

            }

            override fun updateTimer(timer: DanmakuTimer?) {

            }

            override fun drawingFinished() {

            }

            override fun prepared() {
                danmakuView.start(currentPositionWhenPlaying.toLong())
                if (currentState != GSYVideoPlayer.CURRENT_STATE_PLAYING) {
                    danmakuView.postDelayed({
                        danmakuView.pause()
                    }, 10)
                }
            }

        })
        danmakuView.prepare(parser, danmakuContext)
        danmakuView.enableDanmakuDrawingCache(true)
    }

    private fun createParser(inputStream: InputStream): BaseDanmakuParser {
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)

        try {
            loader.load(inputStream)
        } catch (e: IllegalDataException) {
            e.printStackTrace()
        }
        val parser = TucaoDanmukuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    override fun startWindowFullscreen(context: Context?, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer {
        danmakuView.hide()

        val player = super.startWindowFullscreen(context, actionBar, statusBar) as LandLayoutVideo

        val fullScreenDanmakuView = player.findViewById(R.id.danmaku) as DanmakuView

        val overlappingEnablePair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to true,
                BaseDanmaku.TYPE_FIX_TOP to true
        )

        val danmakuContext = DanmakuContext.create()
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                .preventOverlapping(overlappingEnablePair)

        val parser = createParser(danmuStream)

        fullScreenDanmakuView.setCallback(object : DrawHandler.Callback {
            override fun danmakuShown(danmaku: BaseDanmaku?) {

            }

            override fun updateTimer(timer: DanmakuTimer?) {

            }

            override fun drawingFinished() {

            }

            override fun prepared() {
                fullScreenDanmakuView.start(player.currentPositionWhenPlaying.toLong())
                if (currentState != GSYVideoPlayer.CURRENT_STATE_PLAYING) {
                    fullScreenDanmakuView.pause()
                }
            }

        })
        fullScreenDanmakuView.prepare(parser, danmakuContext)
        fullScreenDanmakuView.enableDanmakuDrawingCache(true)
        return player
    }

    override fun getLayoutId(): Int {
        if (mIfCurrentIsFullscreen) {
            return R.layout.danmu_video_land
        }
        return R.layout.danmu_video
    }

    override fun updateStartImage() {
        if (mIfCurrentIsFullscreen) {
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

    override fun clearFullscreenLayout() {
        super.clearFullscreenLayout()
        danmakuView.show()
    }

    override fun onVideoPause() {
        super.onVideoPause()
        if (danmakuView.isPrepared) {
            danmakuView.pause()
        }
    }

    override fun onVideoResume() {
        super.onVideoResume()
        if (mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING && danmakuView.isPrepared && danmakuView.isPaused) {
            danmakuView.resume()
        }
    }

    fun onVideoDestroy() {
        danmakuView.release()
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
    }

    fun resumeDanmu() {
        if (danmakuView.isPrepared) {
            "resumeDanmu".logD()
            danmakuView.resume()
        }
    }

    fun pauseDanmu() {
        if (danmakuView.isPrepared) {
            "pauseDanmu".logD()
            danmakuView.pause()
        }
    }

    fun seekDanmu() {
        if (danmakuView.isPrepared) {
            "seekDanmu".logD()
            danmakuView.seekTo(currentPositionWhenPlaying.toLong())
        }
    }

    override fun onSeekComplete() {
        super.onSeekComplete()
        needCorrectDanmu = true
    }
}
