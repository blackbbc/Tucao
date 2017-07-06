package me.sweetll.tucao.business.home

import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.databinding.ActivityMainBinding
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.di.service.JsonApiService
import me.sweetll.tucao.extension.toast
import javax.inject.Inject

class MainActivity : BaseActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var drawerToggle: ActionBarDrawerToggle

    @Inject
    lateinit var jsonApiService: JsonApiService

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View? = binding.statusBar

    override fun initView(savedInstanceState: Bundle?) {
        AppApplication.get()
                .getApiComponent()
                .inject(this)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.adapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 6
        binding.tab.setupWithViewPager(binding.viewPager)
    }

    override fun initToolbar() {
        super.initToolbar()
        setupDrawer()
    }

    fun setupDrawer() {
        binding.navigation.setNavigationItemSelectedListener({
            menuItem ->
            when (menuItem.itemId) {
                R.id.nav_star -> {
                    StarActivity.intentTo(this)
                }
                R.id.nav_play_history -> {
                    PlayHistoryActivity.intentTo(this)
                }
                R.id.nav_download -> {
                    DownloadActivity.intentTo(this)
                }
                R.id.nav_upgrade -> {
                    "检查更新中...".toast()
                    checkUpdate()
//                    Snackbar.make(binding.root, "请前往百度网盘查看是否有新版本", Snackbar.LENGTH_LONG)
//                            .setAction("打开百度网盘", {
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pan.baidu.com/s/1bptILyR"))
//                                startActivity(intent)
//                            })
//                            .show()
                }
                R.id.nav_setting -> {
                    "没什么好设置的啦( ﾟ∀ﾟ)".toast()
                }
                R.id.nav_about -> {
                    AboutActivity.intentTo(this)
                }
            }
            binding.drawer.closeDrawers()
            true
        })
        drawerToggle = ActionBarDrawerToggle(this, binding.drawer, binding.toolbar, R.string.drawer_open, R.string.drawer_close)
        binding.drawer.addDrawerListener(drawerToggle)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        } else {
            when (item.itemId) {
                android.R.id.home -> {
                    binding.drawer.openDrawer(GravityCompat.START)
                    return true
                }
                R.id.action_search -> {
                    val searchView = getToolbar().findViewById(R.id.action_search)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, searchView,
                            "transition_search_back").toBundle()
                    SearchActivity.intentTo(this, options = options)
                    return true
                }
                else -> {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
    }

    fun checkUpdate() {
        jsonApiService.update("lalala", "lalala", BuildConfig.VERSION_CODE)
                .subscribeOn(Schedulers.io())
                .retryWhen(ApiConfig.RetryWithDelay())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {
                    error ->
                    error.printStackTrace()
                    "服务器异常，请稍后再试".toast()
                })
    }
}
