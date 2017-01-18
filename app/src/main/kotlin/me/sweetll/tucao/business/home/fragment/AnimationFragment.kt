package me.sweetll.tucao.business.home.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.business.home.adapter.AnimationAdapter
import me.sweetll.tucao.business.home.viewmodel.AnimationViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentAnimationBinding
import me.sweetll.tucao.databinding.HeaderAnimationBinding
import me.sweetll.tucao.model.raw.Animation


class AnimationFragment : Fragment() {
lateinit var binding: FragmentAnimationBinding

    val viewModel = AnimationViewModel(this)

    val animationAdapter = AnimationAdapter(null)

    var isLoad = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_animation, container, false)
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
        val headerBinding: HeaderAnimationBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.header_animation, binding.root as ViewGroup, false)
        headerBinding.viewModel = viewModel
        animationAdapter.addHeaderView(headerBinding.root)

        binding.gameRecycler.layoutManager = LinearLayoutManager(activity)
        binding.gameRecycler.adapter = animationAdapter

        binding.gameRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
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

    fun loadAnimation(animation: Animation) {
        isLoad = true
        animationAdapter.setNewData(animation.recommends)
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }
}