package me.sweetll.tucao.business.video

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import kotlinx.android.synthetic.main.danmu_video_land.*

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.databinding.ActivityCommentsBinding
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.util.RelativeDateFormat

class CommentsActivity : BaseActivity() {
    lateinit var binding: ActivityCommentsBinding

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    lateinit var commentId: String
    lateinit var comment: Comment

    companion object {
        private const val ARG_COMMENT_ID = "comment_id"
        private const val ARG_COMMENT = "comment"

        fun intentTo(context: Context, commentId: String, comment: Comment, options: Bundle?) {
            val intent = Intent(context, CommentsActivity::class.java)
            intent.putExtra(ARG_COMMENT_ID, commentId)
            intent.putExtra(ARG_COMMENT, comment)
            context.startActivity(intent, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comments)
        commentId = intent.getStringExtra(ARG_COMMENT_ID)
        comment = intent.getParcelableExtra(ARG_COMMENT)

        binding.imgAvatar.load(this, comment.avatar, R.drawable.default_avatar)
        binding.textLevel.text = comment.level
        binding.textNickname.text = comment.nickname
        binding.textLch.text = comment.lch
        binding.textTime.text = RelativeDateFormat.format(comment.time)
        binding.textThumbUp.text = "${comment.thumbUp}"
        binding.textInfo.text = comment.info
        binding.textReplyNum.text = "${comment.replyNum}"
        binding.linearThumbUp.setOnClickListener {
            // TODO: 点赞
        }
        if (comment.support) {
            binding.imgThumbUp.setColorFilter(ContextCompat.getColor(this, R.color.pink_500))
        } else {
            binding.imgThumbUp.setColorFilter(ContextCompat.getColor(this, R.color.grey_600))
        }
        binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.primary_text))
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "AC1234567"
        }
    }
}
