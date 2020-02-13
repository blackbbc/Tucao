package me.sweetll.tucao.business.download

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
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
        const val ACTION_DOWNLOADED = "downloaded"
        const val ACTION_DOWNLOADING = "downloading"

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

        intent.action?.let {
            when (it) {
                ACTION_DOWNLOADING -> {
                    binding.viewPager.currentItem = 1
                }
                ACTION_DOWNLOADED -> {
                    binding.viewPager.currentItem = 0
                }
            }
        }
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "离线缓存"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_download, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_setting -> {
                DownloadSettingActivity.intentTo(this)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun openContextMenu(contextMenuCallback: ContextMenuCallback, showUpdate: Boolean) {
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
        if (showUpdate) {
            binding.divider1.visibility = View.VISIBLE
            binding.updateBtn.visibility = View.VISIBLE
            binding.updateBtn.setOnClickListener {
                currentContextMenuCallback?.onClickUpdate()
                currentActionMode?.finish()
            }
        } else {
            binding.divider1.visibility = View.GONE
            binding.updateBtn.visibility = View.GONE
        }
        binding.deleteBtn.setOnClickListener {
            currentContextMenuCallback?.onClickDelete()
            currentActionMode?.finish()
        }
    }

    fun updateBottomMenu(deleteEnabled: Boolean, isPickAll: Boolean) {
        binding.deleteBtn.isEnabled = deleteEnabled
        binding.updateBtn.isEnabled = deleteEnabled
        if (isPickAll) {
            binding.pickAllBtn.text = "取消全选"
        } else {
            binding.pickAllBtn.text = "选择全部"
        }
    }

    interface ContextMenuCallback {
        fun onDestroyContextMenu()

        fun onClickDelete()

        fun onClickUpdate() {}

        fun onClickPickAll(): Boolean
    }
}
