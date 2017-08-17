package me.sweetll.tucao.business.personal.fragment

import android.app.Fragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.personal.PersonalActivity
import me.sweetll.tucao.business.personal.viewmodel.ChangeInformationViewModel
import me.sweetll.tucao.databinding.FragmentChangeInformationBinding

class ChangeInformationFragment : BaseFragment() {
    private lateinit var binding: FragmentChangeInformationBinding
    private lateinit var viewModel: ChangeInformationViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_information, container, false)
        viewModel = ChangeInformationViewModel(activity as PersonalActivity)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
