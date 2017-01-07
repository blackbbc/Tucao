package me.sweetll.tucao.business.channel.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trello.rxlifecycle2.kotlin.bindUntilEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.business.channel.adapter.VideoAdapter
import me.sweetll.tucao.databinding.FragmentChannelDetailBinding
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.extension.toast
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
        binding.videoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.videoRecycler.adapter = videoAdapter

        loadData()
    }

    fun loadData() {
        pageIndex++
        jsonApiService.list(tid, pageIndex, pageSize, null)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if ("200" == response.code) {
                        Observable.just(response.result)
                    } else {
                        Observable.error(Throwable(response.msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
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
                .subscribeOn(Schedulers.io())
                .flatMap {
                    response ->
                    if ("200" == response.code) {
                        Observable.just(response.result)
                    } else {
                        Observable.error(Throwable(response.msg))
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    data ->
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