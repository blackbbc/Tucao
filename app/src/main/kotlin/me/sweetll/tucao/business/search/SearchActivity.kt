package me.sweetll.tucao.business.search

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
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

}
