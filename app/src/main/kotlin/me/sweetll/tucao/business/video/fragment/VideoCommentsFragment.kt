package me.sweetll.tucao.business.video.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.databinding.FragmentVideoCommentsBinding
import me.sweetll.tucao.model.json.Result

class VideoCommentsFragment: Fragment() {
    lateinit var binding: FragmentVideoCommentsBinding
    lateinit var result: Result

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_comments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun bindResult(result: Result) {

    }
}
