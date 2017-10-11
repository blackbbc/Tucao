package me.sweetll.tucao.business.drrr

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import me.sweetll.tucao.Const
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.drrr.adapter.ReplyAdapter
import me.sweetll.tucao.business.drrr.model.MultipleItem
import me.sweetll.tucao.business.drrr.model.Post
import me.sweetll.tucao.business.drrr.viewmodel.DrrrDetailViewModel
import me.sweetll.tucao.databinding.ActivityDrrrDetailBinding

class DrrrDetailActivity : BaseActivity() {

    lateinit var binding: ActivityDrrrDetailBinding
    lateinit var viewModel: DrrrDetailViewModel

    override fun getStatusBar() = binding.statusBar

    override fun getToolbar() = binding.toolbar

    lateinit var post: Post

    lateinit var adapter: ReplyAdapter

    companion object {

        private const val ARG_POST = "post"

        fun intentTo(context: Context, post: Post) {
            val intent = Intent(context, DrrrDetailActivity::class.java)
            intent.putExtra(ARG_POST, post)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        post = intent.getParcelableExtra(ARG_POST)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_detail)
        viewModel = DrrrDetailViewModel(this)
        binding.viewModel = viewModel

        setupRecycler()
    }

    private fun setupRecycler() {
        val data = mutableListOf(MultipleItem(post))
        data.add(MultipleItem(post.replyNum))

        adapter = ReplyAdapter(data)
        adapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.replyRecycler)

        binding.replyRecycler.adapter = adapter
        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
    }

    fun loadData(data: MutableList<MultipleItem>) {
        adapter.data.subList(2, data.size).clear()
        adapter.data.addAll(data)
        adapter.notifyDataSetChanged()
    }

    fun loadMoreData(data: MutableList<MultipleItem>?, flag: Int) {
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
            it.title = "查看回复"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
