package me.sweetll.tucao.business.showtimes

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.showtimes.adapter.ShowtimeAdapter
import me.sweetll.tucao.business.showtimes.viewmodel.ShowtimeViewModel
import me.sweetll.tucao.databinding.ActivityShowtimeBinding
import me.sweetll.tucao.model.raw.ShowtimeSection

class ShowtimeActivity : BaseActivity() {
    lateinit var binding: ActivityShowtimeBinding
    val viewModel: ShowtimeViewModel by lazy { ShowtimeViewModel(this) }

    val showtimeAdapter = ShowtimeAdapter(null)

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

        setupRecyclerView()
    }

    fun setupRecyclerView() {
        // Bug: code.google.com/p/android/issues/detail?id=230295
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.isItemPrefetchEnabled = false
        binding.showtimeRecycler.layoutManager = layoutManager
        binding.showtimeRecycler.adapter = showtimeAdapter
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
    }

}
