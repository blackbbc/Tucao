package me.sweetll.tucao.business.home

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.home.adapter.MessageListAdapter
import me.sweetll.tucao.business.home.model.MessageList
import me.sweetll.tucao.business.home.viewmodel.MessageListViewModel
import me.sweetll.tucao.databinding.ActivityMessageListBinding
import me.sweetll.tucao.extension.startActivity
import me.sweetll.tucao.widget.HorizontalDividerBuilder


class MessageListActivity: BaseActivity() {

    private lateinit var binding: ActivityMessageListBinding

    private lateinit var viewModel: MessageListViewModel

    private lateinit var adapter: MessageListAdapter

    override fun getToolbar() = binding.toolbar

    companion object {
        fun intentTo(activity: Activity) {
            activity.startActivity<MessageListActivity>()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_list)
        viewModel = MessageListViewModel(this)
        binding.viewModel = viewModel

        adapter = MessageListAdapter(null)

        binding.messageListRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val message = adapter.getItem(position)!!
                MessageDetailActivity.intentTo(this@MessageListActivity, message.id, message.username, message.avatar)
            }
        })
        binding.messageListRecycler.addItemDecoration(
                HorizontalDividerBuilder.newInstance(this)
                        .setDivider(R.drawable.divider_small)
                        .build()
        )
        binding.messageListRecycler.layoutManager = LinearLayoutManager(this)
        binding.messageListRecycler.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }

    fun onLoadData(data: MutableList<MessageList>) {
        adapter.setNewData(data)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "短消息"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}