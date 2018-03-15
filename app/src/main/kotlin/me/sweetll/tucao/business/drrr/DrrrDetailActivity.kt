package me.sweetll.tucao.business.drrr

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.github.piasy.biv.view.BigImageView
import me.sweetll.tucao.AppApplication
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

    var thumb: String = ""
    var source: String = ""

    private fun bigImageViewerDialog(): Dialog {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_big_image_viewer, null)
        val bigImageView = view.findViewById<BigImageView>(R.id.bigImage)
        bigImageView.setProgressIndicator(ProgressPieIndicator())

        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        dialog.setOnShowListener {
            bigImageView.showImage(Uri.parse(thumb), Uri.parse(source))
        }

        return dialog
    }

    companion object {
        const val REQUEST_NEW_REPLY = 1

        private const val ARG_POST = "post"

        fun intentTo(context: Context, post: Post) {
            val intent = Intent(context, DrrrDetailActivity::class.java)
            intent.putExtra(ARG_POST, post)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initBigImageViewer()

        post = intent.getParcelableExtra(ARG_POST)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_detail)
        viewModel = DrrrDetailViewModel(this)
        binding.viewModel = viewModel

        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }

        setupRecycler()
    }

    private fun initBigImageViewer() {
        BigImageViewer.initialize(GlideImageLoader.with(AppApplication.get()))
    }

    private fun setupRecycler() {
        val data = mutableListOf(MultipleItem(post))
        data.add(MultipleItem(post.replyNum))

        adapter = ReplyAdapter(this, data)
        adapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.replyRecycler)

        adapter.setEnableLoadMore(false)

        binding.replyRecycler.adapter = adapter
        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
    }

    fun setRefreshing(refreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = refreshing
    }

    fun loadData(data: MutableList<MultipleItem>, total: Int) {
        adapter.data.subList(2, adapter.data.size).clear()
        adapter.data[1].replyNum(total)
        adapter.data.addAll(data)
        adapter.notifyDataSetChanged()

        if (data.size < viewModel.size) {
            adapter.setEnableLoadMore(false)
        } else {
            adapter.setEnableLoadMore(true)
        }
    }

    fun loadMoreData(data: MutableList<MultipleItem>?, total: Int, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                adapter.data[1].replyNum(total)
                adapter.notifyItemChanged(1)
                adapter.addData(data!!)
                adapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                adapter.data[1].replyNum(total)
                adapter.notifyItemChanged(1)
                adapter.addData(data!!)
                adapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                adapter.loadMoreFail()
            }
        }
    }

    fun showSourceImage(thumb: String, source: String) {
        this.thumb = thumb
        this.source = source
        bigImageViewerDialog().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_NEW_REPLY && resultCode == Activity.RESULT_OK) {
            viewModel.loadData()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
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
