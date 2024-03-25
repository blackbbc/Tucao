package me.sweetll.tucao.business.home

import android.app.Activity
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.callback.ItemDragAndSwipeCallback
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.home.adapter.StarAdapter
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.ActivityStarBinding
import me.sweetll.tucao.extension.HistoryHelpers
import me.sweetll.tucao.extension.startActivity
import me.sweetll.tucao.model.json.Video

class StarActivity : BaseActivity() {
    lateinit var binding: ActivityStarBinding

    val starAdapter = StarAdapter(HistoryHelpers.loadStar())

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(activity: Activity) {
            activity.startActivity<StarActivity>()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_star)
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        val itemDragAndSwipeCallback = ItemDragAndSwipeCallback(starAdapter)
        val itemTouchHelper = ItemTouchHelper(itemDragAndSwipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.starRecycler)

        starAdapter.enableSwipeItem()
        starAdapter.setOnItemSwipeListener(object: OnItemSwipeListener {
            override fun clearView(p0: RecyclerView.ViewHolder?, p1: Int) {

            }

            override fun onItemSwipeStart(p0: RecyclerView.ViewHolder?, p1: Int) {

            }

            override fun onItemSwiped(p0: RecyclerView.ViewHolder?, position: Int) {
                val result = starAdapter.getItem(position)!!
                HistoryHelpers.removePlayHistory(result)
            }

            override fun onItemSwipeMoving(p0: Canvas?, p1: RecyclerView.ViewHolder?, p2: Float, p3: Float, p4: Boolean) {

            }

        })
        binding.starRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View?, position: Int) {
                val video = helper.getItem(position) as Video
                VideoActivity.intentTo(this@StarActivity, video)
            }
        })
        binding.starRecycler.layoutManager = LinearLayoutManager(this)
        binding.starRecycler.adapter = starAdapter
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "收藏夹"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

}
