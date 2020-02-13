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
import me.sweetll.tucao.business.download.adapter.DownloadingVideoAdapter
import me.sweetll.tucao.business.download.event.RefreshDownloadingVideoEvent
import me.sweetll.tucao.model.json.Part
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.databinding.FragmentDownloadingBinding
import me.sweetll.tucao.extension.DownloadHelpers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class DownloadingFragment: BaseFragment(), DownloadActivity.ContextMenuCallback {
    lateinit var binding: FragmentDownloadingBinding

    val videoAdapter: DownloadingVideoAdapter by lazy {
        DownloadingVideoAdapter(activity as DownloadActivity, DownloadHelpers.loadDownloadingVideos())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloading, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        binding.videoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.videoRecycler.adapter = videoAdapter
        binding.videoRecycler.recycledViewPool.setMaxRecycledViews(DownloadingVideoAdapter.TYPE_PART, 0)
        binding.videoRecycler.recycledViewPool.setMaxRecycledViews(DownloadingVideoAdapter.TYPE_VIDEO, 0)

        binding.videoRecycler.addOnItemTouchListener(object: OnItemLongClickListener() {
            override fun onSimpleItemLongClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                if ((activity as DownloadActivity).currentActionMode != null) {
                    return
                }
                (activity as DownloadActivity).openContextMenu(this@DownloadingFragment, false)
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
    fun onRefreshEvent(event: RefreshDownloadingVideoEvent) {
        videoAdapter.setNewData(DownloadHelpers.loadDownloadingVideos())
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
