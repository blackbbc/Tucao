package me.sweetll.tucao.business.home.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bigkoo.convenientbanner.ConvenientBanner
import me.sweetll.tucao.R
import me.sweetll.tucao.business.home.adapter.BannerHolder
import me.sweetll.tucao.business.home.adapter.RecommendAdapter
import me.sweetll.tucao.business.home.viewmodel.RecommendViewModel
import me.sweetll.tucao.databinding.FragmentRecommendBinding
import me.sweetll.tucao.model.raw.Index
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class RecommendFragment : Fragment() {
    lateinit var binding: FragmentRecommendBinding
    val viewModel: RecommendViewModel by lazy { RecommendViewModel(this) }

    val recommendAdapter = RecommendAdapter(null)

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
    }

    fun setupRecyclerView() {
        binding.recommendRecycler.layoutManager = LinearLayoutManager(activity)
        binding.recommendRecycler.adapter = recommendAdapter
        binding.recommendRecycler.addItemDecoration(HorizontalDividerBuilder.newInstance(activity)
                .setDivider(R.drawable.divider_big)
                .build())
    }

    fun loadIndex(index: Index) {
        binding.banner.setPages({ BannerHolder() }, index.banners)
                .setPageIndicator(intArrayOf(R.drawable.indicator_white_circle, R.drawable.indicator_pink_circle))
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .startTurning(3000)

        recommendAdapter.setNewData(index.recommends)
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }
}