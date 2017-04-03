package me.sweetll.tucao.business.video.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.download.model.Video
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.business.video.adapter.PartAdapter
import me.sweetll.tucao.business.video.viewmodel.VideoInfoViewModel
import me.sweetll.tucao.business.video.viewmodel.VideoViewModel
import me.sweetll.tucao.databinding.FragmentVideoInfoBinding
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.HistoryHelpers
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.Durl


class VideoInfoFragment: Fragment() {
    lateinit var binding: FragmentVideoInfoBinding
    lateinit var viewModel: VideoInfoViewModel
    lateinit var result: Result

    lateinit var parts: MutableList<Part>
    lateinit var selectedPart: Part

    lateinit var partAdapter: PartAdapter

    var canInit = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_info, container, false)
        viewModel = VideoInfoViewModel(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        canInit = canInit or 1
        checkInit()
    }

    fun bindResult(result: Result) {
        this.result = result
        canInit = canInit or 2
        checkInit()
    }

    private fun checkInit() {
        if (canInit != 3) {
            return
        }
        viewModel.bindResult(result)
        result.video.forEachIndexed {
            index, video ->
            video.order = index
            // 解决直传的问题
            if (video.durls.isEmpty() && video.file.isNotEmpty()) {
                video.vid = "${result.hid}${video.order}"
                video.durls.add(Durl(url = video.file))
            }
        }

        val downloadParts = DownloadHelpers.loadDownloadVideos()
                .flatMap { (it as Video).subItems }
        val videoHistory = HistoryHelpers.loadPlayHistory().find { it.hid == result.hid }

        parts = result.video.map {
            video ->
            downloadParts.find { it.vid == video.vid } ?: Part(video.title, video.order, video.vid, video.type, durls = video.durls)
        }.map {
            it.checked = false
            if (videoHistory != null) {
                val historyVideo = videoHistory.video.find { v -> v.vid == it.vid }
                if (historyVideo != null) {
                    it.hasPlay = true
                    it.lastPlayPosition = historyVideo.lastPlayPosition
                } else {
                    it.hasPlay = false
                    it.lastPlayPosition = 0
                }
            }
            it
        }.toMutableList()
        parts[0].checked = true
        parts[0].hasPlay = true
        selectedPart = parts[0]

        partAdapter = PartAdapter(parts)
        (activity as VideoActivity).selectPart(selectedPart)

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        binding.partRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                selectedPart = helper.getItem(position) as Part
                if (!selectedPart.checked) {
                    partAdapter.data.forEach { it.checked = false }
                    selectedPart.hasPlay = true
                    selectedPart.checked = true
                    partAdapter.notifyDataSetChanged()

                    (activity as VideoActivity).selectPart(selectedPart)
                }
            }
        })
        binding.partRecycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.partRecycler.adapter = partAdapter
    }
}