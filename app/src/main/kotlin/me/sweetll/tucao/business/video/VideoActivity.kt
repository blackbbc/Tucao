package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.shuyu.gsyvideoplayer.GSYPreViewManager
import com.shuyu.gsyvideoplayer.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.ActivityVideoBinding
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.Durl

class VideoActivity : BaseActivity() {
    val viewModel = VideoViewModel(this)
    lateinit var binding: ActivityVideoBinding

    lateinit var orientationUtils: OrientationUtils

    lateinit var result: Result

    var isPlay = false
    var isPause = false

    companion object {
        private val ARG_RESULT = "result"

        fun intentTo(context: Context, result: Result) {
            val intent = Intent(context, VideoActivity::class.java)
            intent.putExtra(ARG_RESULT, result)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        binding.viewModel = viewModel

        result = intent.getParcelableExtra(ARG_RESULT)

        orientationUtils = OrientationUtils(this, binding.player)
        orientationUtils.isEnable = false

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

            //第一个true是否需要隐藏ActionBar，第二个true是否需要隐藏StatusBar
            binding.player.startWindowFullscreen(this, true, true)
        }

        binding.player.setStandardVideoAllCallBack(object: StandardVideoAllCallBackAdapter() {
            override fun onPrepared(p0: String?, vararg p1: Any?) {
                super.onPrepared(p0, *p1)
                orientationUtils.isEnable = true
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

        // TODO：判断vid是否为null
//        viewModel.queryPlayUrls(result.video[0].type, result.video[0].vid)
    }

    fun loadDuals(durls: MutableList<Durl>?) {
        durls?.isNotEmpty().let {
            binding.player.setUp(durls!![0].url, true, null)
            "载入完成".toast()
        }
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoPlayer.releaseAllVideos()
        GSYPreViewManager.instance().releaseMediaPlayer()
        orientationUtils.releaseListener()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isPlay && !isPause) {
            if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_USER) {
                if (!binding.player.isIfCurrentIsFullscreen) {
                    binding.player.startWindowFullscreen(this, true, true)
                }
            } else {
                if (binding.player.isIfCurrentIsFullscreen) {
                    StandardGSYVideoPlayer.backFromWindowFull(this);
                }
                orientationUtils.isEnable = true
            }
        }
    }
}