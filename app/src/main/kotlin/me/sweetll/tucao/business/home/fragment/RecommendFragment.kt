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
import me.sweetll.tucao.business.home.adapter.BannerHolder
import me.sweetll.tucao.business.home.adapter.RecommendAdapter
import me.sweetll.tucao.business.home.viewmodel.RecommendViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentRecommendBinding
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.model.raw.Index

class RecommendFragment : BaseFragment() {
    lateinit var binding: FragmentRecommendBinding
    lateinit var headerView: View
    val viewModel = RecommendViewModel(this)

    val recommendAdapter = RecommendAdapter(null)

    var isLoad = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recommend, container, false)
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
        headerView = LayoutInflater.from(activity).inflate(R.layout.header_banner, binding.root as ViewGroup, false)
        recommendAdapter.addHeaderView(headerView)

        binding.recommendRecycler.layoutManager = LinearLayoutManager(activity)
        binding.recommendRecycler.adapter = recommendAdapter

        // Item子控件点击
        binding.recommendRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
                    R.id.img_rank -> {
                        viewModel.onClickRank(view)
                    }
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

    fun loadIndex(index: Index) {
        isLoad = true
        (headerView as ConvenientBanner<Banner>).setPages({ BannerHolder() }, index.banners)
                .setPageIndicator(intArrayOf(R.drawable.indicator_white_circle, R.drawable.indicator_pink_circle))
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .startTurning(3000)

        recommendAdapter.setNewData(index.recommends)
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }
}