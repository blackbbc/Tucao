package me.sweetll.tucao.business.home.fragment

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.home.viewmodel.ChannelListViewModel
import me.sweetll.tucao.databinding.FragmentChannelListBinding


class ChannelListFragment : BaseFragment() {
    val listViewModel: ChannelListViewModel by lazy { ChannelListViewModel(this) }
    lateinit var binding: FragmentChannelListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_channel_list, container, false)
        binding.viewModel = listViewModel
        return binding.root
    }

}