package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.shuyu.gsyvideoplayer.GSYPreViewManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.utils.CommonUtil

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.business.video.adapter.StandardVideoAllCallBackAdapter
import me.sweetll.tucao.databinding.ActivityCachedVideoBinding
import me.sweetll.tucao.extension.setUp
import me.sweetll.tucao.widget.DanmuVideoPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class CachedVideoActivity : BaseActivity(), DanmuVideoPlayer.DanmuPlayerHolder {
    lateinit var binding: ActivityCachedVideoBinding

    var isPlay = false
    var isPause = false

    companion object {
        private val ARG_PART = "part"

        fun intentTo(context: Context, part: Part) {
            val intent = Intent(context, CachedVideoActivity::class.java)
            intent.putExtra(ARG_PART, part)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cached_video)
        val part: Part = intent.getParcelableExtra(ARG_PART)
        setupPlayer()
        loadPart(part)
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

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(p0: String?, vararg p1: Any?) {
                super.onPrepared(p0, *p1)
                isPlay = true
            }
        })

        binding.player.fullscreenButton.visibility = View.GONE
//        CommonUtil.hideNavKey(this)
    }

    fun loadPart(part: Part) {

        val durls = part.durls
        durls.isNotEmpty().let {
            binding.player.loadText?.let {
                it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[完成]")
                binding.player.startButton.visibility = View.VISIBLE
            }
            if (durls.size == 1) {
                binding.player.setUp(durls[0].getCacheAbsolutePath(), true, null)
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
        binding.player.onVideoDestroy()
    }

    override fun onSendDanmu(stime: Float, message: String) {
        // Do nothing
    }

    override fun onSavePlayHistory(position: Int) {
        // DO nothing
    }
}
