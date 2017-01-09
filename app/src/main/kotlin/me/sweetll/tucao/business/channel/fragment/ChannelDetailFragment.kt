package me.sweetll.tucao.business.channel.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.business.channel.adapter.VideoAdapter
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentChannelDetailBinding
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.extension.sanitizeList
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.Result
import javax.inject.Inject

class ChannelDetailFragment : Fragment() {
    lateinit var binding: FragmentChannelDetailBinding
    var tid = 0

    val videoAdapter = VideoAdapter(null)

    var pageIndex = 1
    val pageSize = 10

    @Inject
    lateinit var jsonApiService: JsonApiService

    companion object {
        private val ARG_TID = "tid"

        fun newInstance(tid: Int) : ChannelDetailFragment {
            val fragment = ChannelDetailFragment()
            val args = Bundle()
            args.putInt(ARG_TID, tid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tid = arguments.getInt(ARG_TID, 0)

        AppApplication.get()
                .getApiComponent()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoAdapter.setOnLoadMoreListener {
            loadMoreData()
        }
        binding.videoRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val result: Result = helper.getItem(position) as Result
                VideoActivity.intentTo(activity, result)
            }
        })
        binding.videoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.videoRecycler.adapter = videoAdapter
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.swipeRefresh.isRefreshing = true
        loadData()
    }

    fun loadData() {
        pageIndex = 1
        jsonApiService.list(tid, pageIndex, pageSize, null)
                .sanitizeList()
                .doAfterTerminate { binding.swipeRefresh.isRefreshing = false }
                .subscribe({
                    data ->
                    pageIndex++
                    videoAdapter.setNewData(data)
                }, {
                    error ->
                    error.message?.toast()
                })
    }

    fun loadMoreData() {
        jsonApiService.list(tid, pageIndex, pageSize, null)
                .sanitizeList()
                .subscribe({
                    data ->
                    pageIndex++
                    videoAdapter.addData(data)
                    if (data!!.size < pageSize) {
                        videoAdapter.loadMoreEnd()
                    } else {
                        videoAdapter.loadMoreComplete()
                    }
                }, {
                    error ->
                    error.message?.toast()
                    videoAdapter.loadMoreFail()
                })
    }
}

