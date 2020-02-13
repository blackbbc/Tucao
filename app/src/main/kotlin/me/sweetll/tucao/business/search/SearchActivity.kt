package me.sweetll.tucao.business.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import android.transition.*
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.TransitionRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.Const
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.channel.adapter.VideoAdapter
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.search.adapter.SearchHistoryAdapter
import me.sweetll.tucao.business.search.viewmodel.SearchViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.ActivitySearchBinding
import me.sweetll.tucao.extension.HistoryHelpers
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.transition.CircularReveal
import me.sweetll.tucao.util.TransitionUtils
import me.sweetll.tucao.widget.DisEditText
import me.sweetll.tucao.widget.HorizontalDividerBuilder

class SearchActivity : BaseActivity() {
    lateinit var viewModel: SearchViewModel
    lateinit var binding: ActivitySearchBinding

    val searchHistoryAdapter = SearchHistoryAdapter(null)
    val videoAdapter = VideoAdapter(null)

    private val transitions: SparseArray<Transition> = SparseArray()

    companion object {
        val ARG_KEYWORD = "keyword"
        val ARG_TID = "tid"

        fun intentTo(context: Context, keyword: String? = null, tid: Int? = null, options: Bundle? = null) {
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra(ARG_KEYWORD, keyword)
            intent.putExtra(ARG_TID, tid)
            context.startActivity(intent, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val keyword = intent.getStringExtra(ARG_KEYWORD)
        var tid: Int? = intent.getIntExtra(ARG_TID, 0)
        if (tid == 0) tid = null

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        viewModel = SearchViewModel(this, keyword, tid)
        binding.viewModel = viewModel

        binding.searchEdit.setOnEditorActionListener {
            view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.onClickSearch(view)
                view.clearFocus()
                true
            }
            false
        }

        binding.searchEdit.dismissListener = object : DisEditText.KeyboardDismissListener {
            override fun onKeyboardDismiss() {
                binding.searchEdit.clearFocus()
            }
        }

        setupRecyclerView()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initTransition()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initTransition() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(sharedElementNames: MutableList<String>?, sharedElements: MutableList<View>?, sharedElementSnapshots: MutableList<View>?) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    val searchIcon = sharedElements[0]
                    if (searchIcon.id != R.id.backImg) return
                    val centerX = (searchIcon.left + searchIcon.right) / 2
                    val hideResults = TransitionUtils.findTransition(
                            (this@SearchActivity).window.returnTransition as TransitionSet,
                            CircularReveal::class.java, R.id.resultsContainer)
                    if (hideResults != null) {
                        (hideResults as CircularReveal).setCenter(Point(centerX, 0))
                    }
                }
            }
        })

        window.sharedElementEnterTransition.addListener(object: Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition?) {
                binding.searchEdit.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchEdit, 0)

                TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
                binding.searchResults.visibility = View.VISIBLE
            }

            override fun onTransitionResume(transition: Transition?) {

            }

            override fun onTransitionPause(transition: Transition?) {

            }

            override fun onTransitionCancel(transition: Transition?) {

            }

            override fun onTransitionStart(transition: Transition?) {

            }

        })
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun getTransition(@TransitionRes transitionId: Int): Transition {
        var transition = transitions.get(transitionId)
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId)
            transitions.put(transitionId, transition)
        }
        return transition
    }

    fun setupRecyclerView() {
        videoAdapter.setOnLoadMoreListener ({
            viewModel.loadMoreData()
        }, binding.searchRecycler)

        videoAdapter.setOnItemClickListener {
            helper, view, position ->
                val video = helper.getItem(position) as Video
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val coverImg = view.findViewById<ImageView>(R.id.img_thumb)
                    val titleText = view.findViewById<View>(R.id.text_title)
                    val p1: Pair<View, String> = Pair.create(coverImg, "cover")
                    val cover = titleText.tag as String
                    val options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(this@SearchActivity, p1)
                    VideoActivity.intentTo(this@SearchActivity, video, cover, options.toBundle())
                } else {
                    VideoActivity.intentTo(this@SearchActivity, video)
                }
        }

        binding.searchRecycler.layoutManager = LinearLayoutManager(this)
        binding.searchRecycler.adapter = videoAdapter
        binding.searchRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )

        binding.historyRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val video = helper.getItem(position) as Video
                viewModel.searchText.set(video.title)
                viewModel.onClickSearch(view)
            }
        })
        binding.historyRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                if (view.id == R.id.img_delete) {
                    val result = helper.getItem(position) as Video
                    val removedIndex = HistoryHelpers.removeSearchHistory(result)
                    searchHistoryAdapter.remove(removedIndex)
                }
            }
        })

        binding.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.adapter = searchHistoryAdapter
        binding.historyRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )
    }

    fun clearData() {
        videoAdapter.setNewData(mutableListOf())
    }

    fun loadData(data: MutableList<Video>) {
        videoAdapter.setNewData(data)
        if (data.size < viewModel.pageSize) {
            videoAdapter.setEnableLoadMore(false)
        }
        if (data.isEmpty()) {
            "什么也没有找到~".toast()
        }
    }

    fun loadMoreData(data: MutableList<Video>?, flag: Int) {
        when (flag) {
            Const.LOAD_MORE_COMPLETE -> {
                videoAdapter.addData(data!!)
                videoAdapter.loadMoreComplete()
            }
            Const.LOAD_MORE_END -> {
                videoAdapter.addData(data!!)
                videoAdapter.loadMoreEnd()
            }
            Const.LOAD_MORE_FAIL -> {
                videoAdapter.loadMoreFail()
            }
        }
    }

    fun loadHistory(histories: MutableList<Video>) {
        searchHistoryAdapter.setNewData(histories)
    }

    fun setRefreshing(refreshing: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val autoTransition = AutoTransition()
            autoTransition.interpolator = FastOutSlowInInterpolator()
            autoTransition.duration = 300
            TransitionManager.beginDelayedTransition(
                    binding.root as ViewGroup, autoTransition
            )
        }
        if (refreshing) {
            binding.progress.visibility = View.VISIBLE
            binding.searchResults.visibility = View.GONE
        } else {
            binding.progress.visibility = View.GONE
            binding.searchResults.visibility = View.VISIBLE
        }
    }

    fun showDropDownList(view: View) {
        binding.maskView.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        binding.maskView.visibility = View.VISIBLE
                    }
                })
                .start()

        val scaleIn = ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f)
        scaleIn.duration = 200L
        scaleIn.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {
                view.visibility = View.VISIBLE
            }
        })
        view.startAnimation(scaleIn)
    }

    fun hideDropDownList(view: View) {
        binding.maskView.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        binding.maskView.visibility = View.INVISIBLE
                    }
                })
                .start()

        val scaleOut = ScaleAnimation(1f, 1f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f)
        scaleOut.duration = 200L
        scaleOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
        view.startAnimation(scaleOut)
    }

    fun showChannelDropDownList() {
        showDropDownList(binding.channelDropLinear)
    }

    fun hideChannelDropDownList() {
        hideDropDownList(binding.channelDropLinear)
    }

    fun showOrderDropDownList() {
        showDropDownList(binding.orderDropLinear)
    }

    fun hideOrderDropDownList() {
        hideDropDownList(binding.orderDropLinear)
    }

}
