package me.sweetll.tucao.business.video.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jakewharton.rxbinding2.widget.RxTextView
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.home.event.RefreshPersonalEvent
import me.sweetll.tucao.business.login.LoginActivity
import me.sweetll.tucao.business.video.ReplyActivity
import me.sweetll.tucao.business.video.adapter.CommentAdapter
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.databinding.FragmentVideoCommentsBinding
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.other.User
import me.sweetll.tucao.transition.FabTransform
import me.sweetll.tucao.widget.HorizontalDividerBuilder
import org.greenrobot.eventbus.EventBus
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class VideoCommentsFragment: BaseFragment() {
    lateinit var binding: FragmentVideoCommentsBinding
    lateinit var video: Video

    val commentAdapter = CommentAdapter(null)

    var commentId = ""

    var page = 1
    val pageSize = 20
    var maxPage = 0

    var canInit = 0

    @Inject
    lateinit var user: User

    @Inject
    lateinit var rawApiService: RawApiService

    companion object {
        const val REQUEST_LOGIN = 1
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_comments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        canInit = canInit or 1
        checkInit()
    }

    fun bindVideo(video: Video) {
        this.video = video
        commentId = "content_${video.typeid}-${video.hid}-1"
        canInit = canInit or 2
        checkInit()
    }

    private fun checkInit() {
        if (canInit != 3) {
            return
        }

        commentAdapter.setOnLoadMoreListener ({
            loadMoreData()
        }, binding.commentRecycler)

        binding.commentRecycler.layoutManager = LinearLayoutManager(context)
        binding.commentRecycler.adapter = commentAdapter
        binding.commentRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(context!!)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )

        commentAdapter.setOnItemClickListener{
            _, view, position ->
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!,
                androidx.core.util.Pair.create(view, "transition_background"),
                androidx.core.util.Pair.create(view, "transition_comment"))
            val comment = commentAdapter.getItem(position)!!
            ReplyActivity.intentTo(activity!!, commentId, comment, options.toBundle())
        }
        commentAdapter.setOnItemChildClickListener {
            adapter, view, position ->
            if (view.id == R.id.linear_thumb_up) {
                val comment = commentAdapter.getItem(position)!!
                if (!comment.support) {
                    comment.support = true
                    comment.thumbUp += 1
                    adapter.notifyItemChanged(position)
                    rawApiService.support(commentId, comment.id)
                            .sanitizeHtml {
                                Object()
                            }
                            .subscribe({
                                // Ignored
                            }, {
                                error ->
                                error.printStackTrace()
                            })
                }
            }
        }
        (binding.commentRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        binding.clickToLoadImg.setOnClickListener {
            binding.clickToLoadImg.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
            binding.commentFab.show()
            loadData()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.commentFab.setOnClickListener {
            if (user.isValid()) {
                startFabTransform()
            } else {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity!!, binding.commentFab, "transition_login"
                ).toBundle()
                val intent = Intent(activity, LoginActivity::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    FabTransform.addExtras(intent, ContextCompat.getColor(activity!!, R.color.colorPrimary), R.drawable.ic_comment_white)
                }
                startActivityForResult(intent, REQUEST_LOGIN, options)
            }
        }

        RxTextView.textChanges(binding.commentEdit)
                .map { text -> text.isNotEmpty() }
                .distinctUntilChanged()
                .subscribe {
                    enable ->
                    binding.sendCommentBtn.isEnabled = enable
                }

        binding.sendCommentBtn.setOnClickListener {
            binding.commentEdit.isEnabled = false
            binding.sendCommentBtn.isEnabled = false
            binding.sendCommentBtn.text = "发射中"
            val commentInfo = binding.commentEdit.text.toString()
            val lastFloor: Int = commentAdapter.data.getOrNull(0)?.lch?.replace("[\\D]".toRegex(), "")?.toInt() ?: 0
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val currentDateTime = sdf.format(Date())
            commentAdapter.addData(0, Comment(user.avatar, "lv${user.level}", user.name, 0, "${lastFloor + 1}楼", currentDateTime, commentInfo, "", 0, false))
            binding.commentRecycler.smoothScrollToPosition(0)
            rawApiService.sendComment(commentId, commentInfo)
                    .bindToLifecycle(this)
                    .sanitizeHtml {
                        parseSendCommentResult(this)
                    }
                    .map {
                        (code, msg) ->
                        if (code == 0) {
                            Object()
                        } else {
                            throw Error(msg)
                        }
                    }
                    .doAfterTerminate {
                        binding.commentEdit.isEnabled = true
                        binding.sendCommentBtn.isEnabled = true
                        binding.sendCommentBtn.text = "发射"
                    }
                    .subscribe({
                        // 成功
                        binding.commentEdit.setText("")
                        commentAdapter.data[0].hasSend = true
                        commentAdapter.notifyItemChanged(0)
                    }, {
                        error ->
                        // 失败
                        commentAdapter.remove(0)
                        error.printStackTrace()
                        "发送失败，请检查网络".toast()
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN && Activity.RESULT_OK == resultCode) {
            EventBus.getDefault().post(RefreshPersonalEvent())
        }
    }

    fun startFabTransform() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.commentFab.visibility = View.GONE
            binding.commentContainer.visibility = View.VISIBLE

            val startBounds = Rect(binding.commentFab.left, binding.commentFab.top, binding.commentFab.right, binding.commentFab.bottom)
            val endBounds = Rect(binding.commentContainer.left, binding.commentContainer.top, binding.commentContainer.right, binding.commentContainer.bottom)

            val fabColor = ColorDrawable(ContextCompat.getColor(activity!!, R.color.pink_300))
            fabColor.setBounds(0, 0, endBounds.width(), endBounds.height())
            binding.commentContainer.overlay.add(fabColor)

            val circularReveal = ViewAnimationUtils.createCircularReveal(
                    binding.commentContainer, binding.commentContainer.width / 2, binding.commentContainer.height / 2,
                    binding.commentFab.width / 2f, binding.commentContainer.width / 2f)
            val pathMotion = ArcMotion()
            circularReveal.interpolator = FastOutSlowInInterpolator()
            circularReveal.duration = 240

            val translate = ObjectAnimator.ofFloat(binding.commentContainer, View.TRANSLATION_X, View.TRANSLATION_Y,
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
                    binding.commentContainer.overlay.clear()
                }
            })

            transition.start()
        } else {
            binding.commentFab.hide()
            binding.commentContainer.visibility = View.VISIBLE
        }
    }

    fun loadData() {
        if (!binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = true
        }
        page = 1
        rawApiService.comment(commentId, page)
                .bindToLifecycle(this)
                .sanitizeHtml {
                    val comments = parseComments(this)
                    maxPage = parseMaxPage(this)
                    comments
                }
                .doAfterTerminate { binding.swipeRefresh.isRefreshing = false }
                .subscribe({
                    comments ->
                    page++
                    commentAdapter.setNewData(comments)
                    if (comments.isEmpty()) {
                        val emptyView = LayoutInflater.from(context).inflate(R.layout.layout_empty_comment, null)
                        commentAdapter.emptyView = emptyView
                    }
                    if (page > maxPage) {
                        commentAdapter.loadMoreEnd()
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun loadMoreData() {
        rawApiService.comment(commentId, page)
            .bindToLifecycle(this)
            .sanitizeHtml {
                val comments = parseComments(this)
                comments
            }
            .subscribe({
                comments ->
                page++
                commentAdapter.addData(comments)
                if (page <= maxPage) {
                    commentAdapter.loadMoreComplete()
                } else {
                    commentAdapter.loadMoreEnd()
                }
            }, {
                error ->
                error.printStackTrace()
                error.message?.toast()
            })
    }

    private fun parseSendCommentResult(doc: Document): Pair<Int, String> {
        val result = doc.body().text()
        if ("成功" in result) {
            return Pair(0, "")
        } else {
            return Pair(1, result)
        }
    }

    private fun parseComments(doc: Document): List<Comment> {
        val comments = mutableListOf<Comment>()

        val comment_trs = doc.select("table.comment_list>tbody>tr")
        for (index in 0..comment_trs.size / 2 - 1) {
            val tr1 = comment_trs[2 * index]
            val tr2 = comment_trs[2 * index + 1]

            val td_user = tr1.child(0)

            val avatar = td_user.child(0).child(0).attr("src")
            val nickname = td_user.child(1).text()
            val level = td_user.child(2).attr("class").substring(3)

            val info = tr1.child(1).text()

            val lch = tr2.select("em.lch").first().text()
            val time = tr2.select("em.time").first().text()

            val thumbUp = tr2.select("a.digg").first().child(0).text().toInt()

            val commentId = tr2.select("a.digg>em").first().attr("id").substring(8)

            val replyNum = tr2.select("div.replys").firstOrNull()?.attr("replys")?.toInt() ?: 0

            val comment = Comment(avatar, level, nickname, thumbUp, lch, time, info, commentId, replyNum)
            comments.add(comment)
        }

        return comments
    }

    private fun parseMaxPage(doc: Document): Int {
        val pages = doc.select("div.pages").first()
        if (pages == null) {
            return 1
        } else {
            val a = pages.children()
            val lastPageA = a[a.size - 2]
            val maxPage = lastPageA.text().toInt()
            return maxPage
        }
    }
}
