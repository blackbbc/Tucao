package me.sweetll.tucao.business.download.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.download.adapter.DownloadingVideoAdapter
import me.sweetll.tucao.business.download.event.RefreshDownloadingVideoEvent
import me.sweetll.tucao.databinding.FragmentDownloadingBinding
import me.sweetll.tucao.extension.DownloadHelpers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadingFragment: BaseFragment() {
    lateinit var binding: FragmentDownloadingBinding

    val videoAdapter = DownloadingVideoAdapter(DownloadHelpers.loadDownloadingVideos())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloading, container, false)
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
    fun onRefreshEvent(event: RefreshDownloadingVideoEvent) {
        videoAdapter.setNewData(DownloadHelpers.loadDownloadingVideos())
    }
}
