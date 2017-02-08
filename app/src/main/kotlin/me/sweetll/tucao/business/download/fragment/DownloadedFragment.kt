package me.sweetll.tucao.business.download.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.business.download.event.RefreshDownloadedVideoEvent
import me.sweetll.tucao.databinding.FragmentDownloadedBinding
import me.sweetll.tucao.extension.DownloadHelpers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadedFragment: BaseFragment() {
    lateinit var binding: FragmentDownloadedBinding

    val videoAdapter = DownloadedVideoAdapter(DownloadHelpers.loadDownloadedVideos())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloaded, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setupRecyclerView()
    }

    fun setupRecyclerView() {
        binding.videoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.videoRecycler.adapter = videoAdapter
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(event: RefreshDownloadedVideoEvent) {
        videoAdapter.setNewData(DownloadHelpers.loadDownloadedVideos())
    }
}