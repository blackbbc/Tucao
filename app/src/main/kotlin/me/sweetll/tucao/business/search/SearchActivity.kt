package me.sweetll.tucao.business.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.Const
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.channel.adapter.VideoAdapter
import me.sweetll.tucao.business.search.viewmodel.SearchViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.ActivitySearchBinding
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result

class SearchActivity : BaseActivity() {
    val viewModel = SearchViewModel(this)
    lateinit var binding: ActivitySearchBinding

    val videoAdapter = VideoAdapter(null)

    companion object {
        val ARG_KEYWORD = "keyword"
        val ARG_TID = "tid"

        fun intentTo(context: Context, keyword: String? = null, tid: Int? = null) {
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(ARG_KEYWORD, keyword)
            intent.putExtra(ARG_TID, tid)
            context.startActivity(intent)
        }
    }

    override fun getStatusBar(): View = binding.statusBar

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        binding.viewModel = viewModel

        binding.searchEdit.setOnEditorActionListener {
            view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.onClickSearch(view)
                true
            }
            false
        }

        videoAdapter.setOnLoadMoreListener {
            viewModel.loadMoreData()
        }

        binding.swipeRefresh.isEnabled = false

        binding.searchRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val result = helper.getItem(position) as Result
                VideoActivity.intentTo(this@SearchActivity, result)
            }
        })
        binding.searchRecycler.layoutManager = LinearLayoutManager(this)
        binding.searchRecycler.adapter = videoAdapter
    }

    fun loadData(data: MutableList<Result>) {
        videoAdapter.setNewData(data)
        if (data.size < viewModel.pageSize) {
            videoAdapter.setEnableLoadMore(false)
        }
        if (data.isEmpty()) {
            "什么也没有找到~".toast()
        }
    }

    fun loadMoreData(data: MutableList<Result>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                videoAdapter.addData(data)
                videoAdapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                videoAdapter.addData(data)
                videoAdapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                videoAdapter.loadMoreFail()
            }
        }
    }

    fun setRefreshing(refreshing: Boolean) {
            binding.swipeRefresh.isEnabled = refreshing
            binding.swipeRefresh.isRefreshing = refreshing
    }

    fun showDropDownList(view: View) {
        binding.maskView.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        binding.maskView.visibility = View.VISIBLE
                    }
                })
                .start()

        val scaleIn = ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f)
        scaleIn.duration = 200L
        scaleIn.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                view.visibility = View.VISIBLE
            }
        })
        view.startAnimation(scaleIn)
    }

    fun hideDropDownList(view: View) {
        binding.maskView.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.maskView.visibility = View.INVISIBLE
                    }
                })
                .start()

        val scaleOut = ScaleAnimation(1f, 1f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f)
        scaleOut.duration = 200L
        scaleOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        view.startAnimation(scaleOut)
    }

    fun showChannelDropDownList() {
        showDropDownList(binding.channelDropLinear)
    }

    fun hideChannelDropDownList() {
        hideDropDownList(binding.channelDropLinear)
    }

    fun showOrderDropDownList() {

    }

    fun hideOrderDropDownList() {

    }

}
