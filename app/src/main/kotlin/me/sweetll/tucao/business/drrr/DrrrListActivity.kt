package me.sweetll.tucao.business.drrr

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.Const
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.drrr.adapter.PostAdapter
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.business.drrr.viewmodel.DrrrListViewModel
import me.sweetll.tucao.databinding.ActivityDrrrListBinding
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class DrrrListActivity : BaseActivity() {

    lateinit var binding: ActivityDrrrListBinding
    lateinit var viewModel: DrrrListViewModel

    lateinit var adapter: PostAdapter

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, DrrrListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_list)
        viewModel = DrrrListViewModel(this)
        binding.viewModel = viewModel

        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(null)

        adapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.postRecycler)

        adapter.setEnableLoadMore(false)

        binding.postRecycler.adapter = adapter
        binding.postRecycler.layoutManager = LinearLayoutManager(this)
        binding.postRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_big)
                        .build()
        )
        binding.postRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val post = adapter.getItem(position)
                DrrrDetailActivity.intentTo(this@DrrrListActivity, post)
            }
        })
    }

    fun setRefreshing(refreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = refreshing
    }

    fun loadData(data: MutableList<Post>) {
        adapter.setNewData(data)
        if (data.size < viewModel.size) {
            adapter.setEnableLoadMore(false)
        } else {
            adapter.setEnableLoadMore(true)
        }
    }

    fun loadMoreData(data: MutableList<Post>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                adapter.addData(data)
                adapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                adapter.addData(data)
                adapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                adapter.loadMoreFail()
            }
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "DOLLARS"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
