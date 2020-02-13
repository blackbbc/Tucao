package me.sweetll.tucao.business.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import android.transition.ArcMotion
import android.transition.Transition
import android.view.View
import android.view.ViewAnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.Const

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.business.video.adapter.ReplyAdapter
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.business.video.model.Reply
import me.sweetll.tucao.business.video.viewmodel.ReplyViewModel
import me.sweetll.tucao.databinding.ActivityReplyBinding
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.transition.FabTransform
import me.sweetll.tucao.transition.TransitionListenerAdapter
import me.sweetll.tucao.util.RelativeDateFormat
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class ReplyActivity : BaseActivity() {
    lateinit var binding: ActivityReplyBinding
    lateinit var viewModel: ReplyViewModel

    override fun getToolbar(): Toolbar = binding.toolbar

    lateinit var commentId: String
    lateinit var comment: Comment

    val replyAdapter: ReplyAdapter = ReplyAdapter(null)

    companion object {
        private const val REQUEST_LOGIN = 1

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
        binding.viewModel = viewModel

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

        RxTextView.textChanges(binding.replyEdit)
                .map { text -> text.isNotEmpty() }
                .distinctUntilChanged()
                .subscribe {
                    enable ->
                    binding.sendReplyBtn.isEnabled = enable
                }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initTransition()
        } else {
            binding.replyFab.show()
        }

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        replyAdapter.setOnLoadMoreListener({
            viewModel.loadMoreData()
        }, binding.replyRecycler)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initTransition() {
        window.sharedElementEnterTransition.addListener(object: TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition?) {
                binding.replyFab.show()
            }
        })
    }

    @SuppressLint("RestrictedApi")
    fun requestLogin() {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, binding.replyFab, "transition_login"
        ).toBundle()
        val intent = Intent(this, LoginActivity::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FabTransform.addExtras(intent, ContextCompat.getColor(this, R.color.colorPrimary), R.drawable.ic_comment_white)
        }
        startActivityForResult(intent, REQUEST_LOGIN, options)
    }

    fun startFabTransform() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.replyFab.visibility = View.GONE
            binding.replyContainer.visibility = View.VISIBLE

            val startBounds = Rect(binding.replyFab.left, binding.replyFab.top, binding.replyFab.right, binding.replyFab.bottom)
            val endBounds = Rect(binding.replyContainer.left, binding.replyContainer.top, binding.replyContainer.right, binding.replyContainer.bottom)

            val fabColor = ColorDrawable(ContextCompat.getColor(this, R.color.pink_300))
            fabColor.setBounds(0, 0, endBounds.width(), endBounds.height())
            binding.replyContainer.overlay.add(fabColor)

            val circularReveal = ViewAnimationUtils.createCircularReveal(
                    binding.replyContainer, binding.replyContainer.width / 2, binding.replyContainer.height / 2,
                    binding.replyFab.width / 2f, binding.replyContainer.width / 2f)
            val pathMotion = ArcMotion()
            circularReveal.interpolator = FastOutSlowInInterpolator()
            circularReveal.duration = 240

            val translate = ObjectAnimator.ofFloat(binding.replyContainer, View.TRANSLATION_X, View.TRANSLATION_Y,
                    pathMotion.getPath((startBounds.centerX() - endBounds.centerX()).toFloat(), (startBounds.centerY() - endBounds.centerY()).toFloat(), 0f, 0f))
            translate.interpolator = LinearOutSlowInInterpolator()
            translate.duration = 240

            val colorFade = ObjectAnimator.ofInt(fabColor, "alpha", 0)
            colorFade.duration = 120
            colorFade.interpolator = FastOutSlowInInterpolator()

            val transition = AnimatorSet()
            transition.duration = 240
            transition.playTogether(circularReveal, translate, colorFade)
            transition.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    binding.replyContainer.overlay.clear()
                }
            })

            transition.start()
        } else {
            binding.replyFab.hide()
            binding.replyContainer.visibility = View.VISIBLE
        }
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

    fun loadMoreData(data: MutableList<Reply>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                replyAdapter.addData(data!!)
                replyAdapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                replyAdapter.addData(data!!)
                replyAdapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                replyAdapter.loadMoreFail()
            }
        }
    }

    fun startSendingReply(reply: Reply) {
        binding.replyEdit.isEnabled = false
        binding.sendReplyBtn.isEnabled = false
        binding.sendReplyBtn.text = "发射中"
        replyAdapter.addData(0, reply)
        binding.replyRecycler.smoothScrollToPosition(0)
    }

    fun endSendingReply(success: Boolean) {
        binding.replyEdit.isEnabled = true
        binding.sendReplyBtn.isEnabled = true
        binding.sendReplyBtn.text = "发射"
        if (success) {
            binding.replyEdit.setText("")
            replyAdapter.data[0].hasSend = true
            replyAdapter.notifyItemChanged(0)
        } else {
            replyAdapter.remove(0)
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
