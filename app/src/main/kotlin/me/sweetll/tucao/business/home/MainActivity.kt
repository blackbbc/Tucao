package me.sweetll.tucao.business.home

import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.DownloadActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.databinding.ActivityMainBinding
import me.sweetll.tucao.extension.toast

class MainActivity : BaseActivity() {

    lateinit var binding : ActivityMainBinding

    override fun getToolbar(): Toolbar = binding.toolbar

    override fun getStatusBar(): View? = binding.statusBar

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.adapter = HomePagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 6
        binding.tab.setupWithViewPager(binding.viewPager)
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawer.toggleMenu()
        }
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
                    Snackbar.make(binding.root, "请前往百度网盘查看是否有新版本", Snackbar.LENGTH_LONG)
                            .setAction("打开百度网盘", {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pan.baidu.com/s/1bptILyR"))
                                startActivity(intent)
                            })
                            .show()
                }
                R.id.nav_setting -> {
                    "没什么好设置的啦( ﾟ∀ﾟ)".toast()
                }
                R.id.nav_about -> {
                    AboutActivity.intentTo(this)
                }
            }
            binding.drawer.closeMenu()
            true
        })
        binding.drawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
        binding.coordinator.foreground = ContextCompat.getDrawable(AppApplication.get(), R.drawable.window_dim)
        binding.coordinator.foreground.alpha = 0
        binding.drawer.setOnDrawerStateChangeListener(object: ElasticDrawer.OnDrawerStateChangeListener {
            override fun onDrawerStateChange(oldState: Int, newState: Int) {

            }

            override fun onDrawerSlide(openRatio: Float, offsetPixels: Int) {
                binding.coordinator.foreground.alpha = offsetPixels / 10
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawer.toggleMenu()
                return true
            }
            R.id.action_search -> {
                SearchActivity.intentTo(this)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
