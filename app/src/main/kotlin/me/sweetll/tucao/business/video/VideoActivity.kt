package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.View
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.ActivityVideoBinding
import me.sweetll.tucao.model.json.Result

class VideoActivity : BaseActivity() {
    val viewModel = VideoViewModel(this)
    lateinit var binding: ActivityVideoBinding

    lateinit var orientationUtils: OrientationUtils

    lateinit var result: Result

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
    }

}