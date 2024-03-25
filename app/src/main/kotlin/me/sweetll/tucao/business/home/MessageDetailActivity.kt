package me.sweetll.tucao.business.home

import android.app.Activity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.home.adapter.MessageDetailAdapter
import me.sweetll.tucao.business.home.model.MessageDetail
import me.sweetll.tucao.business.home.viewmodel.MessageDetailViewModel
import me.sweetll.tucao.databinding.ActivityMessageDetailBinding
import me.sweetll.tucao.extension.startActivity

class MessageDetailActivity: BaseActivity() {

    private lateinit var binding: ActivityMessageDetailBinding

    private lateinit var viewModel: MessageDetailViewModel

    private lateinit var adapter: MessageDetailAdapter

    override fun getToolbar() = binding.toolbar

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_USERNAME = "username"
        private const val ARG_AVATAR = "avatar"

        fun intentTo(activity: Activity, id: String, username: String, avatar: String) {
            activity.startActivity<MessageDetailActivity>(
                    ARG_ID to id,
                    ARG_USERNAME to username,
                    ARG_AVATAR to avatar
            )
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_message_detail)
        viewModel = MessageDetailViewModel(this,
                intent.getStringExtra(ARG_ID),
                intent.getStringExtra(ARG_USERNAME),
                intent.getStringExtra(ARG_AVATAR))
        binding.viewModel = viewModel

        adapter = MessageDetailAdapter(null)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.messageDetailRecycler.layoutManager = layoutManager
        binding.messageDetailRecycler.adapter = adapter

        viewModel.loadData()
    }

    fun onLoadData(data: MutableList<MessageDetail>) {
        adapter.setNewData(data)
        binding.messageDetailRecycler.scrollToPosition(data.size - 1)
    }

    fun addMessage(message: MessageDetail) {
        adapter.addData(message)
        binding.messageDetailRecycler.scrollToPosition(adapter.data.size - 1)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = intent.getStringExtra(ARG_USERNAME)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

}