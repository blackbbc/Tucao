package me.sweetll.tucao.business.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.TargetApi
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.Toolbar
import android.text.format.DateFormat
import android.transition.*
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.shuyu.gsyvideoplayer.GSYPreViewManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.utils.PlayerConfig
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.video.adapter.StandardVideoAllCallBackAdapter
import me.sweetll.tucao.business.video.adapter.VideoPagerAdapter
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.ActivityVideoBinding
import me.sweetll.tucao.extension.*
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.rxdownload.entity.DownloadStatus
import me.sweetll.tucao.widget.DanmuVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

class VideoActivity : BaseActivity(), DanmuVideoPlayer.DanmuPlayerHolder {
    lateinit var viewModel: VideoViewModel
    lateinit var binding: ActivityVideoBinding

    lateinit var orientationUtils: OrientationUtils

    lateinit var videoPagerAdapter: VideoPagerAdapter

    var isPlay = false
    var isPause = false

    var transitionIn = true

    lateinit var result: Result
    lateinit var selectedPart: Part
    var firstPlay = true

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        private val ARG_RESULT = "result"
        private val ARG_HID = "hid"
        private val ARG_COVER = "cover"

        fun intentTo(context: Context, result: Result) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_RESULT, result)
            context.startActivity(intent)
        }

        fun intentTo(context: Context, result: Result, cover: String, bundle: Bundle) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_RESULT, result)
            intent.putExtra(ARG_COVER, cover)
            context.startActivity(intent, bundle)
        }

        fun intentTo(context: Context, hid: String) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_HID, hid)
            context.startActivity(intent)
        }

        fun intentTo(context: Context, hid: String, cover: String, bundle: Bundle) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_HID, hid)
            intent.putExtra(ARG_COVER, cover)
            context.startActivity(intent, bundle)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        val hid = intent.getStringExtra(ARG_HID)
        val cover = intent.getStringExtra(ARG_COVER)

        videoPagerAdapter = VideoPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = videoPagerAdapter
        binding.viewPager.offscreenPageLimit = 3
        binding.tab.setupWithViewPager(binding.viewPager)

        if (hid != null) {
            viewModel = VideoViewModel(this)
            viewModel.queryResult(hid)
        } else {
            val result: Result = intent.getParcelableExtra(ARG_RESULT)
            viewModel = VideoViewModel(this, result)
            loadResult(result)
        }

        if (!cover.isNullOrEmpty()) {
            val thumbImg = ImageView(this)
            thumbImg.scaleType = ImageView.ScaleType.FIT_XY
            ViewCompat.setTransitionName(binding.appBar, "cover")
            binding.player.setThumbImageView(thumbImg)

            ViewCompat.setTransitionName(binding.mainContainer, "bg")

            initTransition()
            supportPostponeEnterTransition()
            thumbImg.load(this, cover, {
                supportStartPostponedEnterTransition()
            })
        } else {
            // 5.0以下加载
            binding.player.visibility = View.VISIBLE
            binding.tab.alpha = 1f
            binding.viewPager.alpha = 1f
        }

        binding.viewModel = viewModel

        orientationUtils = OrientationUtils(this)
        binding.player.setOrientationUtils(orientationUtils)

        binding.playBtn.setOnClickListener {
            binding.appBar.setExpanded(true)
        }
        binding.appBar.addOnOffsetChangedListener(object: AppBarLayout.OnOffsetChangedListener {
            val EXPANDED = 1 shl 0
            val COLLAPSED = 1 shl 1
            val IDLE = 1 shl 2

            var currentState = IDLE

            override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
                if (i == 0) {
                    if (currentState != EXPANDED) {

                    }
                    currentState = EXPANDED
                } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
                    if (currentState != COLLAPSED) {
                        binding.playBtn.visibility = View.VISIBLE
                    }
                    currentState = COLLAPSED
                } else {
                    if (currentState != IDLE) {
                        binding.playBtn.visibility = View.GONE
                    }
                    currentState = IDLE
                }
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun initTransition() {
        val changeBounds = ChangeBounds()

        window.sharedElementReturnTransition = null
        window.sharedElementExitTransition = changeBounds

        window.sharedElementEnterTransition.interpolator = FastOutSlowInInterpolator()

        val slideUp = Slide(Gravity.TOP)
        slideUp.addTarget(binding.appBar)
        val slideDown = Slide(Gravity.BOTTOM)
        slideDown.addTarget(binding.mainContainer)
        val slideAll = TransitionSet()
        slideAll.addTransition(slideUp)
        slideAll.addTransition(slideDown)
        slideAll.ordering = TransitionSet.ORDERING_TOGETHER

        window.returnTransition = slideAll

        window.sharedElementEnterTransition.addListener(object: Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                val slideBottomAnimator = ObjectAnimator.ofFloat(binding.viewPager, "translationY", -50f.dp2px(), 0f)
                val fadeIn1Animator = ObjectAnimator.ofFloat(binding.viewPager, "alpha", 0f, 1f)
                val fadeIn2Animator = ObjectAnimator.ofFloat(binding.tab, "alpha", 0f, 1f)
                fadeIn2Animator.startDelay = 150
                val enterAnimatorSet = AnimatorSet()
                enterAnimatorSet.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        binding.tab.alpha = 0f
                    }
                })

                enterAnimatorSet.playTogether(slideBottomAnimator, fadeIn1Animator, fadeIn2Animator)
                enterAnimatorSet.start()
            }

            override fun onTransitionResume(transition: Transition?) {

            }

            override fun onTransitionPause(transition: Transition?) {

            }

            override fun onTransitionCancel(transition: Transition?) {

            }

            override fun onTransitionStart(transition: Transition?) {

            }

        })

        setEnterSharedElementCallback(object: SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                if (transitionIn) {
                    transitionIn = false
                } else {
                    sharedElements?.run {
                        remove("cover")
                        remove("bg")
                    }
                }
            }
        })
    }

    fun loadResult(result: Result) {
        this.result = result
        setupPlayer()
        videoPagerAdapter.bindResult(result)
    }

    fun setupPlayer() {
        GSYVideoManager.instance().optionModelList = mutableListOf(
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "concat,file,subfile,http,https,tls,rtp,tcp,udp,crypto,async"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", "ijk"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 4096),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 2000000),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 15 * 1024 * 1024),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "r", "29.97"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "async-forwards-capacity", 15 * 1024 * 1024),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "async-backwards-capacity", 15 * 1024 * 1024)

        )

        // 是否可以滑动界面改变进度，声音
        binding.player.setIsTouchWiget(true)
        //关闭自动旋转
        binding.player.isLockLand = false
        binding.player.isNeedLockFull = true
        binding.player.isOpenPreView = true
        binding.player.fullscreenButton.setOnClickListener {
            //直接横屏
            orientationUtils.backToLand()
        }

        if (PlayerConfig.loadHardCodec()) {
            GSYVideoType.enableMediaCodec() // 开启硬解
        }

        binding.player.speed = 1f

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(p0: String?) {
                super.onPrepared(p0)
            }

            override fun onClickStartIcon(p0: String?) {
                super.onClickStartIcon(p0)
                isPlay = true
                if (firstPlay) {
                    firstPlay = false
                    if (selectedPart.lastPlayPosition != 0) {
                        binding.player.showJump(selectedPart.lastPlayPosition)
                    }
                }
            }

            // 播放完了
            override fun onAutoComplete(p0: String?) {
                super.onAutoComplete(p0)
                isPlay = false
                binding.player.onVideoPause(true, true)
            }
        })
    }

    fun loadDurls(durls: MutableList<Durl>?) {
        durls?.isNotEmpty().let {
            binding.player.loadText?.let {
                it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[完成]")
                binding.player.startButton.visibility = View.VISIBLE
            }
            if (durls!!.size == 1) {
                binding.player.setUp(if (selectedPart.flag == DownloadStatus.COMPLETED) durls[0].getCacheAbsolutePath() else durls[0].url)
            } else {
                binding.player.setUp(durls, selectedPart.flag == DownloadStatus.COMPLETED)
            }
            firstPlay = true
        }
    }

    fun loadDanmuUri(uri: String) {
        binding.player.setUpDanmu(uri)
    }

    fun selectPart(selectedPart: Part) {
        isPlay = false
        this.selectedPart = selectedPart
        binding.player.onVideoPause()
        binding.player.loadText?.let {
            binding.player.startButton.visibility = View.GONE
            it.visibility = View.VISIBLE
            it.text = "播放器初始化...[完成]\n获取视频信息...[完成]\n解析视频地址...\n全舰弹幕装填..."
        }
        if (selectedPart.vid.isNotEmpty()) {
            viewModel.queryPlayUrls(result.hid, selectedPart)
        } else {
            "所选视频已失效".toast()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.player.onVideoPause(isPlay)
        isPause = true
    }

    override fun onResume() {
        super.onResume()
        binding.player.onVideoResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoPlayer.releaseAllVideos()
        GSYPreViewManager.instance().releaseMediaPlayer()
        binding.player.onVideoDestroy()
    }

    override fun onBackPressed() {
        orientationUtils.backToPort()
        if (StandardGSYVideoPlayer.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!binding.player.isIfCurrentIsFullscreen) {
                    binding.player.startWindowFullscreen(this, true, true)
                }
            }
    }

    override fun onSendDanmu(stime: Float, message: String) {
        viewModel.sendDanmu(stime, message)
    }

    override fun onSavePlayHistory(position: Int) {
        HistoryHelpers.savePlayHistory(
                result.copy(create = DateFormat.format("yyyy-MM-dd hh:mm:ss", Date()).toString())
                        .apply {
                            video = video.filter {
                                it.vid == selectedPart.vid
                            }.map {
                                it.lastPlayPosition = position
                                it
                            }.toMutableList()
                        }
        )
    }
}