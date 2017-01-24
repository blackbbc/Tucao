package me.sweetll.tucao.business.home

import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var drawerToggle: ActionBarDrawerToggle

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
        setupDrawer()
    }

    fun setupDrawer() {
        binding.navigation.setNavigationItemSelectedListener({
            menuItem ->
            when (menuItem.itemId) {
                R.id.nav_play_history -> {
                    PlayHistoryActivity.intentTo(this)
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
                    SearchActivity.intentTo(this)
                    return true
                }
                else -> {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
    }
}
