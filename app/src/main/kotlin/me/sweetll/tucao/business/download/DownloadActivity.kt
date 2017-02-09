package me.sweetll.tucao.business.download

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.download.adapter.DownloadPagerAdapter
import me.sweetll.tucao.databinding.ActivityDownloadBinding

class DownloadActivity : BaseActivity() {
    lateinit var binding: ActivityDownloadBinding

    var currentActionMode: ActionMode? = null
    var currentContextMenuCallback: ContextMenuCallback? = null

    val modeCallback = object: ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            mode.menuInflater.inflate(R.menu.context_menu_download, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            binding.bottomLinear.visibility = View.VISIBLE
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.menu_done -> {
                    currentActionMode?.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            binding.bottomLinear.visibility = View.GONE
            currentActionMode = null
            currentContextMenuCallback?.onDestroyContextMenu()
            currentContextMenuCallback = null
        }

    }

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, DownloadActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_download)

        binding.viewPager.adapter = DownloadPagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 2
        binding.tab.setupWithViewPager(binding.viewPager)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "离线缓存"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun openContextMenu(contextMenuCallback: ContextMenuCallback) {
        currentActionMode = startActionMode(modeCallback)
        currentContextMenuCallback = contextMenuCallback
        binding.pickAllBtn.setOnClickListener {
            val isPickAll = currentContextMenuCallback?.onClickPickAll() ?: false
            if (isPickAll) {
                binding.pickAllBtn.text = "取消全选"
            } else {
                binding.pickAllBtn.text = "选择全部"
            }
        }
        binding.deleteBtn.setOnClickListener {
            currentContextMenuCallback?.onClickDelete()
            currentActionMode?.finish()
        }
    }

    fun updateBottomMenu(deleteEnabled: Boolean, isPickAll: Boolean) {
        binding.deleteBtn.isEnabled = deleteEnabled
        if (isPickAll) {
            binding.pickAllBtn.text = "取消全选"
        } else {
            binding.pickAllBtn.text = "选择全部"
        }
    }

    interface ContextMenuCallback {
        fun onDestroyContextMenu()

        fun onClickDelete()

        fun onClickPickAll(): Boolean
    }
}
