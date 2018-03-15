package me.sweetll.tucao.business.drrr

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.github.piasy.biv.BigImageViewer
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator
import com.github.piasy.biv.loader.glide.GlideImageLoader
import com.github.piasy.biv.view.BigImageView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import me.sweetll.tucao.AppApplication
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
        const val REQUEST_NEW_POST = 1

        fun intentTo(context: Context) {
            val intent = Intent(context, DrrrListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        initBigImageViewer()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_list)
        viewModel = DrrrListViewModel(this)
        binding.viewModel = viewModel

        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }

        setupRecyclerView()
    }

    private fun initBigImageViewer() {
        BigImageViewer.initialize(GlideImageLoader.with(AppApplication.get()))
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(this, null)

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
        adapter.setOnItemClickListener {
            _, _, position ->
            val post = adapter.getItem(position)!!
            DrrrDetailActivity.intentTo(this, post)
        }
        adapter.setOnItemChildClickListener {
            _, view, position ->
            val post = adapter.getItem(position)!!
            if (!post.vote) {
                post.vote = true
                post.voteNum++
                viewModel.vote(post)
                view.findViewById<TextView>(R.id.text_vote_num).text = "${post.voteNum}"
                view.findViewById<ImageView>(R.id.img_thumb_up).setColorFilter(ContextCompat.getColor(this, R.color.pink_500))
            }
        }
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
                adapter.addData(data!!)
                adapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
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
        if (requestCode == REQUEST_NEW_POST && resultCode == Activity.RESULT_OK) {
            viewModel.loadData()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "留言板"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
