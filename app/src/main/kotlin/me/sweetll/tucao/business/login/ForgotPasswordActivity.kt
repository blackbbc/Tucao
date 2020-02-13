package me.sweetll.tucao.business.login

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.View
import com.google.android.material.snackbar.Snackbar

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.viewmodel.ForgotPasswordViewModel
import me.sweetll.tucao.databinding.ActivityForgotPasswordBinding
import me.sweetll.tucao.extension.toast

class ForgotPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, ForgotPasswordActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        viewModel = ForgotPasswordViewModel(this)
        binding.viewModel = viewModel
    }

    fun resetSuccess() {
        "请前往邮箱查看新密码".toast()
        finish()
    }


    fun resetFailed(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = ""
        }
    }
}
