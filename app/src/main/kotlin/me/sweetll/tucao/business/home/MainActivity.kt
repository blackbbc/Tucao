package me.sweetll.tucao.business.home

import android.databinding.DataBindingUtil
import android.os.Bundle
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.home.adapter.HomePagerAdapter
import me.sweetll.tucao.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        binding.viewPager.adapter = HomePagerAdapter(supportFragmentManager)
        binding.tab.setupWithViewPager(binding.viewPager)
    }
}
