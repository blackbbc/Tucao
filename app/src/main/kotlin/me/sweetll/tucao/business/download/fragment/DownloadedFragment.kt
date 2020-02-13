package me.sweetll.tucao.business.download.fragment

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.download.adapter.DownloadedVideoAdapter
import me.sweetll.tucao.business.download.event.RefreshDownloadedVideoEvent
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.databinding.FragmentDownloadedBinding
import me.sweetll.tucao.extension.DownloadHelpers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadedFragment: BaseFragment(), DownloadActivity.ContextMenuCallback {
    lateinit var binding: FragmentDownloadedBinding

    val videoAdapter: DownloadedVideoAdapter by lazy {
        DownloadedVideoAdapter(activity as DownloadActivity, DownloadHelpers.loadDownloadedVideos())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloaded, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        binding.videoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.videoRecycler.adapter = videoAdapter

        binding.videoRecycler.addOnItemTouchListener(object: OnItemLongClickListener() {
            override fun onSimpleItemLongClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                if ((activity as DownloadActivity).currentActionMode != null) {
                    return
                }
                (activity as DownloadActivity).openContextMenu(this@DownloadedFragment, true)
                videoAdapter.data.forEach {
                    when (it) {
                        is Video -> {
                            it.checkable = true
                            it.checked = false
                            it.subItems.forEach {
                                it.checkable = true
                                it.checked = false
                            }
                        }
                        is Part -> {
                            it.checkable = true
                            it.checked = false
                        }
                    }
                }
                videoAdapter.notifyDataSetChanged()
            }
        })
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

    override fun onDestroyContextMenu() {
        videoAdapter.data.forEach {
            when (it) {
                is Video -> {
                    it.checkable = false
                    it.subItems.forEach { it.checkable = false }
                }
                is Part -> it.checkable = false
            }
        }
        videoAdapter.notifyDataSetChanged()
    }

    override fun onClickDelete() {
        DownloadHelpers.cancelDownload(
                videoAdapter.data.flatMap {
                    when (it) {
                        is Video -> it.subItems
                        else -> listOf(it as Part)
                    }
                }.distinctBy(Part::vid).filter(Part::checked)
        )
    }

    override fun onClickUpdate() {
        DownloadHelpers.updateDanmu(
                videoAdapter.data.flatMap {
                    when (it) {
                        is Video -> it.subItems
                        else -> listOf(it as Part)
                    }
                }.distinctBy(Part::vid).filter(Part::checked)
        )
    }

    override fun onClickPickAll(): Boolean {
        if (videoAdapter.data.all {
            when (it) {
                is Video -> it.checked
                is Part -> it.checked
                else -> false
            }
        }) {
            // 取消全选
            videoAdapter.data.forEach {
                when (it) {
                    is Video -> {
                        it.checked = false
                        it.subItems.forEach { it.checked = false }
                    }
                    is Part -> it.checked = false
                }
            }
            videoAdapter.notifyDataSetChanged()
            return false
        } else {
            // 全选
            videoAdapter.data.forEach {
                when (it) {
                    is Video -> {
                        it.checked = true
                        it.subItems.forEach { it.checked = true }
                    }
                    is Part -> it.checked = true
                }
            }
            videoAdapter.notifyDataSetChanged()
            return true
        }
    }
}