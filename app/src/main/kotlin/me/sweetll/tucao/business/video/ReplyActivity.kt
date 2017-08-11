package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.Const

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.video.adapter.ReplyAdapter
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.business.video.model.Reply
import me.sweetll.tucao.business.video.viewmodel.ReplyViewModel
import me.sweetll.tucao.databinding.ActivityReplyBinding
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.util.RelativeDateFormat
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class ReplyActivity : BaseActivity() {
    lateinit var binding: ActivityReplyBinding
    lateinit var viewModel: ReplyViewModel

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    lateinit var commentId: String
    lateinit var comment: Comment

    val replyAdapter: ReplyAdapter = ReplyAdapter(null)

    companion object {
        private const val ARG_COMMENT_ID = "comment_id"
        private const val ARG_COMMENT = "comment"

        fun intentTo(context: Context, commentId: String, comment: Comment, options: Bundle?) {
            val intent = Intent(context, ReplyActivity::class.java)
            intent.putExtra(ARG_COMMENT_ID, commentId)
            intent.putExtra(ARG_COMMENT, comment)
            context.startActivity(intent, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        commentId = intent.getStringExtra(ARG_COMMENT_ID)
        comment = intent.getParcelableExtra(ARG_COMMENT)

        viewModel = ReplyViewModel(this, commentId, comment.id)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reply)

        binding.imgAvatar.load(this, comment.avatar, R.drawable.default_avatar)
        binding.textLevel.text = comment.level
        binding.textNickname.text = comment.nickname
        binding.textLch.text = comment.lch
        binding.textTime.text = RelativeDateFormat.format(comment.time)
        binding.textThumbUp.text = "${comment.thumbUp}"
        binding.textInfo.text = comment.info
        binding.textReplyNum.text = "${comment.replyNum}"
        binding.textReplyNumDivider.text = "共${comment.replyNum}条回复"
        binding.linearThumbUp.setOnClickListener {
            comment.thumbUp += 1
            comment.support = true
            binding.textThumbUp.text = "${comment.thumbUp}"
            binding.imgThumbUp.setColorFilter(ContextCompat.getColor(this, R.color.pink_500))
            viewModel.support(comment.id)
        }
        if (comment.support) {
            binding.imgThumbUp.setColorFilter(ContextCompat.getColor(this, R.color.pink_500))
        } else {
            binding.imgThumbUp.setColorFilter(ContextCompat.getColor(this, R.color.grey_600))
        }
        binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.primary_text))

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        replyAdapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.replyRecycler)
    }

    fun loadData(data: MutableList<Reply>) {
        replyAdapter.setNewData(data)
        if (data.size < viewModel.pageSize) {
            replyAdapter.setEnableLoadMore(false)
        }

        binding.replyRecycler.layoutManager = LinearLayoutManager(this)
        binding.replyRecycler.adapter = replyAdapter
        binding.replyRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )
    }

    fun loadMoreData(newData: MutableList<Reply>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                replyAdapter.addData(newData)
                replyAdapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                replyAdapter.addData(newData)
                replyAdapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                replyAdapter.loadMoreFail()
            }
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "回复"
        }
    }
}
