package me.sweetll.tucao.business.rank.fragment

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.rank.adapter.RankVideoAdapter
import me.sweetll.tucao.business.rank.event.ChangeRankFilterEvent
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentRankDetailBinding
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.extension.sanitizeJson
import me.sweetll.tucao.extension.toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class RankDetailFragment : BaseFragment() {
    lateinit var binding: FragmentRankDetailBinding
    var tid = 0
    var date = 0

    val rankVideoAdapter = RankVideoAdapter(null)

    @Inject
    lateinit var jsonApiService: JsonApiService

    companion object {
        private val ARG_TID = "tid"

        fun newInstance(tid: Int) : RankDetailFragment {
            val fragment = RankDetailFragment()
            val args = Bundle()
            args.putInt(ARG_TID, tid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tid = arguments!!.getInt(ARG_TID, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rank_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rankVideoRecycler.addOnItemTouchListener(object : OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val video: Video = helper.getItem(position) as Video
                VideoActivity.intentTo(activity!!, video)
            }
        })
        binding.rankVideoRecycler.layoutManager = LinearLayoutManager(activity)
        binding.rankVideoRecycler.adapter = rankVideoAdapter
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        loadData()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun loadData() {
        if (!binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = true
        }
        jsonApiService.rank(tid, date)
                .bindToLifecycle(this)
                .sanitizeJson()
                .map {
                    response ->
                    response.values.toList()
                }
                .doAfterTerminate { binding.swipeRefresh.isRefreshing = false }
                .subscribe({
                    data ->
                    rankVideoAdapter.setNewData(data)
                }, {
                    error ->
                    error.message?.toast()
                })
    }

    @Subscribe()
    fun onChangeRankFilterEvent(event: ChangeRankFilterEvent) {
        if (date != event.date) {
            date = event.date
            loadData()
        }
    }


}

