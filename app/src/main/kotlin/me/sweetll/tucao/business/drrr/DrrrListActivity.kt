package me.sweetll.tucao.business.drrr

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.BuildConfig
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

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, DrrrListActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drrr_list)
        viewModel = DrrrListViewModel(this)
        binding.viewModel = viewModel

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val mockData = mutableListOf(
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false),
                Post("1", "1", 1, 1, Build.BRAND, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, false, System.currentTimeMillis(), System.currentTimeMillis(), false)
        )
        adapter = PostAdapter(mockData)

        binding.swipeRefresh.isEnabled = false

        binding.postRecycler.adapter = adapter
        binding.postRecycler.layoutManager = LinearLayoutManager(this)
        binding.postRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_big)
                        .build()
        )
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "DOLLARS"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
