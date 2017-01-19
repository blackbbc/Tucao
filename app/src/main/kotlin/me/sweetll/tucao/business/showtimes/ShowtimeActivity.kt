package me.sweetll.tucao.business.showtimes

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.showtimes.adapter.ShowtimeAdapter
import me.sweetll.tucao.business.showtimes.viewmodel.ShowtimeViewModel
import me.sweetll.tucao.databinding.ActivityShowtimeBinding
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.model.raw.ShowtimeSection
import java.util.*

class ShowtimeActivity : BaseActivity() {
    lateinit var binding: ActivityShowtimeBinding
    val viewModel: ShowtimeViewModel by lazy { ShowtimeViewModel(this) }

    val showtimeAdapter = ShowtimeAdapter(null)
    val headerPositions: MutableList<Int> = mutableListOf()

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View = binding.statusBar

    companion object {
        fun intentTo(context: Context) {
            context.startActivity(Intent(context, ShowtimeActivity::class.java))
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_showtime)
        binding.viewModel = viewModel

        binding.indicatorFrame.viewTreeObserver.addOnGlobalLayoutListener( object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.indicatorFrame.viewTreeObserver.removeOnGlobalLayoutListener(this)
                var dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
                if (dayOfWeek < 0) dayOfWeek = 6
                val targetView = binding.weekLinear.getChildAt(dayOfWeek)
                moveIndicatorView(targetView)
            }
        })

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        // Bug: code.google.com/p/android/issues/detail?id=230295
        val staggeredLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        staggeredLayoutManager.isItemPrefetchEnabled = false
        binding.showtimeRecycler.layoutManager = staggeredLayoutManager
        binding.showtimeRecycler.adapter = showtimeAdapter

        binding.showtimeRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dy.toString().logD()
            }
        })
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "本季新番"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isEnabled = isRefreshing
        binding.swipeRefresh.isRefreshing = isRefreshing
    }

    fun loadShowtime(data: MutableList<ShowtimeSection>) {
        showtimeAdapter.setNewData(data)
        headerPositions.clear()
        data.forEachIndexed {
            position, showtimeSection ->
            if (showtimeSection.isHeader) {
                headerPositions.add(position)
            }
        }

        var dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        if (dayOfWeek < 0) dayOfWeek = 6
        showWeek(dayOfWeek)

        binding.indicatorFrame.visibility = View.VISIBLE
    }

    fun moveIndicatorView(targetView: View) {
        binding.indicatorView.animate()
                .x(targetView.x + targetView.width / 2f - binding.indicatorView.width / 2f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(200)
                .start()
    }

    fun showWeek(week: Int, view: View? = null) {
        view?.let {
            moveIndicatorView(it)
        }
        if (headerPositions.isNotEmpty()) {
            binding.showtimeRecycler.smoothScrollToPosition(headerPositions[week])
        }
    }

}
