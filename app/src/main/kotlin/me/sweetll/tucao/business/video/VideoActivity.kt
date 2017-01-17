package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
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
import me.sweetll.tucao.business.video.adapter.PartAdapter
import me.sweetll.tucao.business.video.adapter.StandardVideoAllCallBackAdapter
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.ActivityVideoBinding
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.setUp
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.xml.Durl
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.InputStream

class VideoActivity : BaseActivity() {
    lateinit var viewModel: VideoViewModel
    lateinit var binding: ActivityVideoBinding

    lateinit var orientationUtils: OrientationUtils

    var isPlay = false
    var isPause = false

    lateinit var partAdapter: PartAdapter

    companion object {
        private val ARG_RESULT = "result"
        private val ARG_HID = "hid"

        fun intentTo(context: Context, result: Result) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_RESULT, result)
            context.startActivity(intent)
        }

        fun intentTo(context: Context, hid: String) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_HID, hid)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        val hid = intent.getStringExtra(ARG_HID)
        if (hid != null) {
            viewModel = VideoViewModel(this, hid)
        } else {
            val result: Result = intent.getParcelableExtra(ARG_RESULT)
            viewModel = VideoViewModel(this, result)
            loadResult(result)
        }
        binding.viewModel = viewModel
    }

    fun setupRecyclerView(result: Result) {
        result.video[0].checked = true
        partAdapter = PartAdapter(result.video)

        binding.partRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val selectedVideo = helper.getItem(position) as Video
                if (!selectedVideo.checked) {
                    partAdapter.data.forEach { it.checked = false }
                    selectedVideo.checked = true
                    partAdapter.notifyDataSetChanged()

                    if (selectedVideo.vid != null) {
                        viewModel.queryPlayUrls(result.hid, position, selectedVideo.type, selectedVideo.vid)
                    } else {
                        "所选视频已失效".toast()
                    }
                }
            }
        })
        binding.partRecycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.partRecycler.adapter = partAdapter
    }

    fun loadResult(result: Result) {

        binding.player.loadText?.let {
            it.text = it.text.replace("获取视频信息...".toRegex(), "获取视频信息...[完成]")
        }

        orientationUtils = OrientationUtils(this, binding.player)
        orientationUtils.isEnable = false

        GSYVideoManager.instance().optionModelList = mutableListOf(
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "concat,file,subfile,http,https,tls,rtp,tcp,udp,crypto"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user-agent", "ijk")
        )

        // 是否可以滑动界面改变进度，声音
        binding.player.setIsTouchWiget(true)
        //关闭自动旋转
        binding.player.isRotateViewAuto = false
        binding.player.isLockLand = false
        binding.player.isShowFullAnimation = false
        binding.player.isNeedLockFull = true
        binding.player.isOpenPreView = true
        binding.player.fullscreenButton.setOnClickListener {
            view ->
            //直接横屏
            orientationUtils.resolveByClick()
        }

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(p0: String?, vararg p1: Any?) {
                super.onPrepared(p0, *p1)
                isPlay = true
            }

            override fun onQuitFullscreen(p0: String?, vararg p1: Any?) {
                super.onQuitFullscreen(p0, *p1)
                orientationUtils.backToProtVideo()
            }

        })

        binding.player.setLockClickListener {
            view, lock ->
            orientationUtils.isEnable = !lock
        }

        val firstVid = result.video[0].vid
        if (firstVid != null) {
//             载入第一个视频
            viewModel.queryPlayUrls(result.hid, 0, result.video[0].type, firstVid)
        } else {
            "所选视频已失效".toast()
        }

        setupRecyclerView(result)
    }

    fun loadDuals(durls: MutableList<Durl>?) {
        durls?.isNotEmpty().let {
            if (durls!!.size == 1) {
                binding.player.setUp(durls[0].url, true, null)
            } else {
                binding.player.setUp(durls, true)
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
        orientationUtils.releaseListener()
        binding.player.onVideoDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isPlay && !isPause) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!binding.player.isIfCurrentIsFullscreen) {
                    binding.player.startWindowFullscreen(this, true, true)
                }
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (binding.player.isIfCurrentIsFullscreen) {
                    StandardGSYVideoPlayer.backFromWindowFull(this)
                }
            }
        }
    }
}