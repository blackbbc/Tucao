package me.sweetll.tucao.business.home

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.graphics.Canvas
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.home.adapter.PlayHistoryAdapter
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.ActivityPlayHistoryBinding
import me.sweetll.tucao.extension.HistoryHelpers

class PlayHistoryActivity : BaseActivity() {
    lateinit var binding: ActivityPlayHistoryBinding

    val playHistoryAdapter = PlayHistoryAdapter(HistoryHelpers.loadPlayHistory())

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, PlayHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_history)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(playHistoryAdapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.historyRecycler)

        playHistoryAdapter.enableSwipeItem()
        playHistoryAdapter.setOnItemSwipeListener(object: OnItemSwipeListener {
            override fun clearView(p0: RecyclerView.ViewHolder?, p1: Int) {

            }

            override fun onItemSwipeStart(p0: RecyclerView.ViewHolder?, p1: Int) {

            }

            override fun onItemSwiped(p0: RecyclerView.ViewHolder?, position: Int) {
                val result = playHistoryAdapter.getItem(position)!!
                HistoryHelpers.removePlayHistory(result)
            }

            override fun onItemSwipeMoving(p0: Canvas?, p1: RecyclerView.ViewHolder?, p2: Float, p3: Float, p4: Boolean) {

            }

        })
        binding.historyRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View?, position: Int) {
                val video = helper.getItem(position) as Video
                VideoActivity.intentTo(this@PlayHistoryActivity, video.hid)
            }
        })
        binding.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.adapter = playHistoryAdapter
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "历史记录"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
