package me.sweetll.tucao.business.home.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bigkoo.convenientbanner.ConvenientBanner
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.business.home.adapter.BangumiAdapter
import me.sweetll.tucao.business.home.adapter.BannerHolder
import me.sweetll.tucao.business.home.viewmodel.BangumiViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentBangumiBinding
import me.sweetll.tucao.databinding.HeaderBangumiBinding
import me.sweetll.tucao.model.raw.Bangumi
import me.sweetll.tucao.model.raw.Banner


class BangumiFragment : BaseFragment() {
    lateinit var binding: FragmentBangumiBinding
    lateinit var headerBinding: HeaderBangumiBinding

    val viewModel = BangumiViewModel(this)

    val bangumiAdapter = BangumiAdapter(null)

    var isLoad = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bangumi, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        setupRecyclerView()
        loadWhenNeed()
    }

    fun setupRecyclerView() {
        headerBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.header_bangumi, binding.root as ViewGroup, false)
        headerBinding.viewModel = viewModel
        bangumiAdapter.addHeaderView(headerBinding.root)

        binding.bangumiRecycler.layoutManager = LinearLayoutManager(activity)
        binding.bangumiRecycler.adapter = bangumiAdapter

        binding.bangumiRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
                    R.id.text_more -> {
                        ChannelDetailActivity.intentTo(activity, view.tag as Int)
                    }
                    R.id.linear1, R.id.linear2, R.id.linear3, R.id.linear4 -> {
                        VideoActivity.intentTo(activity, view.tag as String)
                    }
                }
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        loadWhenNeed()
    }

    fun loadWhenNeed() {
        if (isVisible && userVisibleHint && !isLoad && !binding.swipeRefresh.isRefreshing) {
            viewModel.loadData()
        }
    }

    fun loadBangumi(bangumi: Bangumi) {
        isLoad = true
        bangumiAdapter.setNewData(bangumi.recommends)
        headerBinding.banner.setPages({ BannerHolder() }, bangumi.banners)
                .setPageIndicator(intArrayOf(R.drawable.indicator_white_circle, R.drawable.indicator_pink_circle))
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                .startTurning(3000)
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }
}