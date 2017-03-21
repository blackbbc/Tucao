package me.sweetll.tucao.business.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.format.DateFormat
import android.transition.*
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.shuyu.gsyvideoplayer.GSYPreViewManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.business.video.adapter.PartAdapter
import me.sweetll.tucao.business.video.adapter.StandardVideoAllCallBackAdapter
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.ActivityVideoBinding
import me.sweetll.tucao.extension.*
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.Durl
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import zlc.season.rxdownload2.entity.DownloadFlag
import java.util.*

class VideoActivity : BaseActivity() {
    lateinit var viewModel: VideoViewModel
    lateinit var binding: ActivityVideoBinding

    lateinit var orientationUtils: OrientationUtils

    var isPlay = false
    var isPause = false

    var parts: MutableList<Part>? = null
    var selectedPart: Part? = null

    lateinit var partAdapter: PartAdapter

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

        if (hid != null) {
            viewModel = VideoViewModel(this)
            viewModel.queryResult(hid)
        } else {
            val result: Result = intent.getParcelableExtra(ARG_RESULT)
            viewModel = VideoViewModel(this, result)
            loadResult(result)
        }

        if (!cover.isNullOrEmpty()) {
            binding.thumbImg.load(cover)
            initTransition()
        } else {
            // 5.0以下加载
        }

        binding.viewModel = viewModel

        orientationUtils = OrientationUtils(this)
        binding.player.setOrientationUtils(orientationUtils)
    }

    fun setupRecyclerView(result: Result) {
        binding.partRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                selectedPart = helper.getItem(position) as Part
                if (!selectedPart!!.checked) {
                    binding.player.loadText?.let {
                        binding.player.startButton.visibility = View.GONE
                        it.visibility = View.VISIBLE
                        it.text = "播放器初始化...[完成]\n获取视频信息...[完成]\n解析视频地址...\n全舰弹幕装填..."
                    }
                    partAdapter.data.forEach { it.checked = false }
                    selectedPart!!.checked = true
                    partAdapter.notifyDataSetChanged()

                    if (selectedPart!!.vid.isNotEmpty()) {
                        binding.player.onVideoPause()
                        // TODO: 隐藏播放按钮
                        viewModel.queryPlayUrls(result.hid, selectedPart!!)
                    } else {
                        "所选视频已失效".toast()
                    }
                }
            }
        })
        binding.partRecycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.partRecycler.adapter = partAdapter
    }

    fun initTransition() {
        val changeBounds = ChangeBounds()
        val arcMotion = ArcMotion()
        changeBounds.pathMotion = arcMotion

        window.sharedElementReturnTransition = null
        window.sharedElementExitTransition = changeBounds

        window.returnTransition = Explode()

        window.sharedElementEnterTransition.addListener(object: Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                val slideTopAnimator = ObjectAnimator.ofFloat(binding.mainLinear, "translationY", 50f.dp2px(), 0f)
                val fadeIn1Animator = ObjectAnimator.ofFloat(binding.mainLinear, "alpha", 0f, 1f)
                val fadeIn2Animator = ObjectAnimator.ofFloat(binding.player, "alpha", 0f, 1f)
                val enterAnimatorSet = AnimatorSet()
                enterAnimatorSet.addListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        binding.player.visibility = View.VISIBLE
                        binding.mainLinear.visibility = View.VISIBLE
                    }
                })

                val cx = (binding.player.left + binding.player.right) / 2
                val cy = (binding.player.top + binding.player.bottom)  / 2
                val radius = maxOf(binding.player.width, binding.player.height)
                val revealAnimator = ViewAnimationUtils.createCircularReveal(binding.player, cx, cy, 0f, radius.toFloat())

                enterAnimatorSet.playTogether(slideTopAnimator, fadeIn1Animator, fadeIn2Animator, revealAnimator)
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
    }

    fun loadResult(result: Result) {
        result.video.forEachIndexed {
            index, video ->
            video.order = index
        }

        val downloadParts = DownloadHelpers.loadDownloadVideos()
                .flatMap { (it as Video).subItems }
        val videoHistory = HistoryHelpers.loadPlayHistory().find { it.hid == result.hid }

        parts = result.video.map {
            video ->
            val part = downloadParts.find { it.vid == video.vid } ?: Part(video.title, video.order, video.vid, video.type)
            // 解决直传的问题
            if (part.durls.isEmpty() && video.file.isNotEmpty()) {
                part.vid = "${result.hid}${part.order}"
                part.durls.add(Durl(url = video.file))
            }
            part
        }.map {
            it.checked = false
            if (videoHistory != null) {
                it.hasPlay = videoHistory.video.any {
                    v ->
                    v.vid == it.vid
                }
            }
            it
        }.toMutableList()
        parts!![0].checked = true
        selectedPart = parts!![0]

        partAdapter = PartAdapter(parts)

        binding.player.loadText?.let {
            it.text = it.text.replace("获取视频信息...".toRegex(), "获取视频信息...[完成]")
        }

        GSYVideoManager.instance().optionModelList = mutableListOf(
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "concat,file,subfile,http,https,tls,rtp,tcp,udp,crypto"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user-agent", "ijk")
        )

        // 是否可以滑动界面改变进度，声音
        binding.player.setIsTouchWiget(true)
        //关闭自动旋转
        binding.player.isLockLand = false
        binding.player.isNeedLockFull = true
        binding.player.isOpenPreView = true
        binding.player.fullscreenButton.setOnClickListener {
            view ->
            //直接横屏
            orientationUtils.backToLand()
        }

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(p0: String?, vararg p1: Any?) {
                super.onPrepared(p0, *p1)
                isPlay = true
            }

            override fun onClickStartIcon(p0: String?, vararg p1: Any?) {
                super.onClickStartIcon(p0, *p1)
                HistoryHelpers.savePlayHistory(
                        result.copy(create = DateFormat.format("yyyy-MM-dd hh:mm:ss", Date()).toString())
                                .apply {
                                    video = video.filter { it.order == selectedPart!!.order }.toMutableList()
                                }
                )
            }
        })

        if (selectedPart!!.vid.isNotEmpty()) {
            viewModel.queryPlayUrls(result.hid, selectedPart!!)
        } else {
            "所选视频已失效".toast()
        }

        setupRecyclerView(result)
    }

    fun loadDuals(durls: MutableList<Durl>?) {
        durls?.isNotEmpty().let {
            binding.player.loadText?.let {
                it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[完成]")
                binding.player.startButton.visibility = View.VISIBLE
            }
            if (durls!!.size == 1) {
                binding.player.setUp(if (selectedPart!!.flag == DownloadFlag.COMPLETED) durls[0].getCacheAbsolutePath() else durls[0].url, true, null)
            } else {
                binding.player.setUp(durls, selectedPart!!.flag == DownloadFlag.COMPLETED)
            }
        }
    }

    fun loadDanmuUri(uri: String) {
        binding.player.setUpDanmu(uri)
    }

    override fun onPause() {
        super.onPause()
        binding.player.onVideoPause()
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
//        if (isPlay && !isPause) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!binding.player.isIfCurrentIsFullscreen) {
                    binding.player.startWindowFullscreen(this, true, true)
                }
            } /* else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (binding.player.isIfCurrentIsFullscreen) {
                    StandardGSYVideoPlayer.backFromWindowFull(this)
                }
            } */
//        }
    }
}