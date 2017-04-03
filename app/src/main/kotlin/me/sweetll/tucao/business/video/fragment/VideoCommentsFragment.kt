package me.sweetll.tucao.business.video.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.video.adapter.CommentAdapter
import me.sweetll.tucao.business.video.model.Comment
import me.sweetll.tucao.databinding.FragmentVideoCommentsBinding
import me.sweetll.tucao.di.service.RawApiService
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.widget.HorizontalDividerBuilder
import org.jsoup.nodes.Document
import javax.inject.Inject

class VideoCommentsFragment: BaseFragment() {
    lateinit var binding: FragmentVideoCommentsBinding
    lateinit var result: Result

    val commentAdapter = CommentAdapter(null)

    var commentId = ""

    var page = 1
    val pageSize = 20
    var maxPage = 0

    var canInit = 0

    @Inject
    lateinit var rawApiService: RawApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppApplication.get()
                .getApiComponent()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_comments, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        canInit = canInit or 1
        checkInit()
    }

    fun bindResult(result: Result) {
        this.result = result
        commentId = "content_${result.typeid}-${result.hid}-1"
        canInit = canInit or 2
        checkInit()
    }

    private fun checkInit() {
        if (canInit != 3) {
            return
        }

        commentAdapter.setOnLoadMoreListener {
            loadMoreData()
        }

        binding.commentRecycler.layoutManager = LinearLayoutManager(activity)
        binding.commentRecycler.adapter = commentAdapter
        binding.commentRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(context)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )

        binding.clickToLoadImg.setOnClickListener {
            binding.clickToLoadImg.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
            loadData()
        }
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
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

            val comment = Comment(avatar, level, nickname, thumbUp, lch, time, info)
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
