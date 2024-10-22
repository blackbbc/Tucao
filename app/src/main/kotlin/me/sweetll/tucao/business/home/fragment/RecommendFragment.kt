package me.sweetll.tucao.business.home.fragment

import android.os.Bundle
import android.transition.ArcMotion
import android.transition.ChangeBounds
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
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
        binding.loading.show()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.isEnabled = false
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        binding.errorStub.setOnInflateListener {
            _, inflated ->
            inflated.setOnClickListener {
                viewModel.loadData()
            }
        }
        setupRecyclerView()
        loadWhenNeed()

        initTransition()
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
                    R.id.card_more -> {
                        ChannelDetailActivity.intentTo(activity!!, view.tag as Int)
                    }
                    R.id.card1, R.id.card2, R.id.card3, R.id.card4 -> {
                        val coverImg = (((view as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(0)
                        val titleText = (view.getChildAt(0) as ViewGroup).getChildAt(1)
                        val p1: Pair<View, String> = Pair.create(coverImg, "cover")
                        val p2: Pair<View, String> = Pair.create(titleText, "bg")
                        val cover = titleText.tag as String
                        val options = ActivityOptionsCompat
                                .makeSceneTransitionAnimation(activity!!, p1, p2)
                        VideoActivity.intentTo(activity!!, view.tag as String, cover, options.toBundle())
                    }
                }
            }
        })
    }

    fun initTransition() {
        val changeBounds = ChangeBounds()

        val arcMotion = ArcMotion()
        changeBounds.pathMotion = arcMotion

        activity!!.window.sharedElementExitTransition = changeBounds
        activity!!.window.sharedElementReenterTransition = null
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
        if (!isLoad) {
            isLoad = true
            TransitionManager.beginDelayedTransition(binding.swipeRefresh)
            binding.swipeRefresh.isEnabled = true
            binding.loading.visibility = View.GONE
            if (binding.errorStub.isInflated) {
                binding.errorStub.root.visibility = View.GONE
            }
            binding.recommendRecycler.visibility = View.VISIBLE
        }

        (headerView as ConvenientBanner<Banner>).setPages({ BannerHolder() }, index.banners)
                .setPageIndicator(intArrayOf(R.drawable.indicator_white_circle, R.drawable.indicator_pink_circle))
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .startTurning(3000)

        recommendAdapter.setNewData(index.recommends)
    }

    fun loadError() {
        if (!isLoad) {
            TransitionManager.beginDelayedTransition(binding.swipeRefresh)
            binding.loading.visibility = View.GONE
            if (!binding.errorStub.isInflated) {
                binding.errorStub.viewStub!!.visibility = View.VISIBLE
            } else {
                binding.errorStub.root.visibility = View.VISIBLE
            }
        }
    }

    fun setRefreshing(isRefreshing: Boolean) {
        if (isLoad) {
            binding.swipeRefresh.isRefreshing = isRefreshing
        } else {
            TransitionManager.beginDelayedTransition(binding.swipeRefresh)
            binding.loading.visibility = if (isRefreshing) View.VISIBLE else View.GONE
            if (isRefreshing) {
                if (!binding.errorStub.isInflated) {
                    binding.errorStub.viewStub!!.visibility = View.GONE
                } else {
                    binding.errorStub.root.visibility = View.GONE
                }
            }
        }
    }
}