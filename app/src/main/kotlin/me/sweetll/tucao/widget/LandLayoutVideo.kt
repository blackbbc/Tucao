package me.sweetll.tucao.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.shuyu.gsyvideoplayer.GSYVideoPlayer

import com.shuyu.gsyvideoplayer.video.CustomGSYVideoPlayer
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView

import me.sweetll.tucao.R

class LandLayoutVideo : CustomGSYVideoPlayer {
    lateinit var danmakuView: DanmakuView
    lateinit var danmakuContext: DanmakuContext

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

        val maxLinespair = mapOf(BaseDanmaku.TYPE_SCROLL_RL to 5)
        val overlappingEnablePair = mapOf(
                BaseDanmaku.TYPE_SCROLL_RL to true,
                BaseDanmaku.TYPE_FIX_TOP to true
        )

        danmakuContext = DanmakuContext.create()
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3f)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                .setMaximumLines(maxLinespair)
                .preventOverlapping(overlappingEnablePair)
    }

    fun setUpDanmu(url: String) {
        val parser = createParser(url)
        danmakuView.prepare(parser, danmakuContext)
        danmakuView.enableDanmakuDrawingCache(true)
    }

    private fun createParser(url: String): BaseDanmakuParser {
        val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)

        try {
            loader.load(url)
        } catch (e: IllegalDataException) {
            e.printStackTrace()
        }
        val parser = TucaoDanmukuParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        return parser
    }

    //这个必须配置最上面的构造才能生效
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
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector)
            } else if (mCurrentState == GSYVideoPlayer.CURRENT_STATE_ERROR) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector)
            } else {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector)
            }
        } else {
            super.updateStartImage()
        }
    }

}
