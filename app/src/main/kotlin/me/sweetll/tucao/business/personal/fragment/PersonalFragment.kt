package me.sweetll.tucao.business.personal.fragment

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.business.personal.viewmodel.PersonalViewModel
import me.sweetll.tucao.databinding.FragmentPersonalBinding


class PersonalFragment: BaseFragment() {

    private lateinit var binding: FragmentPersonalBinding
    private lateinit var viewModel: PersonalViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal, container, false)
        viewModel = PersonalViewModel(activity as PersonalActivity)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}