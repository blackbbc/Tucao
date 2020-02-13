package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import com.shuyu.gsyvideoplayer.GSYPreViewManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.utils.PlayerConfig

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.business.video.adapter.StandardVideoAllCallBackAdapter
import me.sweetll.tucao.databinding.ActivityCachedVideoBinding
import me.sweetll.tucao.extension.HistoryHelpers
import me.sweetll.tucao.extension.setUp
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.widget.DanmuVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.File
import java.util.*

class CachedVideoActivity : BaseActivity(), DanmuVideoPlayer.DanmuPlayerHolder {
    lateinit var binding: ActivityCachedVideoBinding

    lateinit var orientationUtils: OrientationUtils

    var isPlay = false
    var isPause = false

    var firstPlay = true

    lateinit var video: Video
    lateinit var selectedPart: Part

    companion object {
        private val ARG_VIDEO = "video"

        fun intentTo(context: Context, video: Video) {
            val intent = Intent(context, CachedVideoActivity::class.java)
            intent.putExtra(ARG_VIDEO, video)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cached_video)
        video = intent.getParcelableExtra(ARG_VIDEO)
        selectedPart = video.parts[0]

        HistoryHelpers.loadPlayHistory()
                .flatMap { it.parts }
                .find { it.vid == selectedPart.vid }
                ?.let {
                    selectedPart.hadPlay = true
                    selectedPart.lastPlayPosition = it.lastPlayPosition
                }


        setupPlayer()
        loadPart(selectedPart)

        val danmuFile = File(selectedPart.durls[0].cacheFolderPath, "danmu.xml")
        if (danmuFile.exists()) {
            loadDanmuUri(danmuFile.absolutePath)
        } else {
            "未发现弹幕文件，请更新弹幕".toast()
        }

        binding.player.startButton.performClick()

        orientationUtils = OrientationUtils(this)
        binding.player.setOrientationUtils(orientationUtils)
    }

    fun setupPlayer() {
        binding.player.loadText?.let {
            it.text = it.text.replace("获取视频信息...".toRegex(), "获取视频信息...[完成]")
            it.text = it.text.replace("全舰弹幕装填...".toRegex(), "")
        }

        GSYVideoManager.instance().optionModelList = mutableListOf(
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "safe", 0),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "concat,file,subfile,http,https,tls,rtp,tcp,udp,crypto"),
                VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", "ijk")
        )
        binding.player.isLockLand = true
        binding.player.isNeedLockFull = true
        binding.player.isOpenPreView = true
        binding.player.isNeedShowWifiTip = false

        if (PlayerConfig.loadHardCodec()) {
            GSYVideoType.enableMediaCodec() // 开启硬解
        }

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(url: String?) {
                super.onPrepared(url)
                isPlay = true
                if (firstPlay) {
                    firstPlay = false
                    if (selectedPart.lastPlayPosition != 0) {
                        binding.player.showJump(selectedPart.lastPlayPosition)
                    }
                }
            }

        })

        binding.player.fullscreenButton.visibility = View.GONE
        binding.player.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    fun loadPart(part: Part) {

        val durls = part.durls
        durls.isNotEmpty().let {
            binding.player.loadText?.let {
                it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[完成]")
                binding.player.startButton.visibility = View.VISIBLE
            }
            if (durls.size == 1) {
                binding.player.setUp(durls[0].getCacheAbsolutePath())
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

    override fun onSendDanmu(stime: Float, message: String) {
        // Do nothing
    }

    override fun onSavePlayHistory(position: Int) {
        HistoryHelpers.savePlayHistory(
        video.copy(create = DateFormat.format("yyyy-MM-dd hh:mm:ss", Date()).toString())
                .also {
                    it.parts = video.parts.filter {
                        it.vid == selectedPart.vid
                    }.map {
                        it.lastPlayPosition = position
                        it
                    }.toMutableList()
                }
        )
    }
}
