package me.sweetll.tucao.business.personal.fragment

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.*
import com.google.android.material.snackbar.Snackbar
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.personal.viewmodel.ChangePasswordViewModel
import me.sweetll.tucao.databinding.FragmentChangePasswordBinding
import me.sweetll.tucao.extension.toast

class ChangePasswordFragment: BaseFragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)
        viewModel = ChangePasswordViewModel(this)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_personal, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                viewModel.save()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun saveSuccess() {
        "修改成功！请重新登录".toast()
        activity!!.supportFragmentManager.popBackStack()
    }

    fun saveFailed(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }
}
