package me.sweetll.tucao.business.login

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import com.tbruyelle.rxpermissions2.RxPermissions

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.viewmodel.LoginViewModel
import me.sweetll.tucao.databinding.ActivityLoginBinding
import me.sweetll.tucao.extension.toast

class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: LoginViewModel

    private lateinit var accountManager: AccountManager

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = LoginViewModel(this)
        binding.viewModel = viewModel

        setupAccountAutocomplete()
    }

    private fun setupAccountAutocomplete() {
        accountManager = AccountManager.get(this)
        RxPermissions(this)
                .request(Manifest.permission.GET_ACCOUNTS)
                .subscribe {
                    granted ->
                    if (granted) {
                        val accounts = accountManager.accounts
                        val emailSet = accounts.fold(HashSet<String>()) {
                            total, account ->
                            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                                total.add(account.name)
                            }
                            total
                        }
                        binding.emailEdit.setAdapter(
                                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, emailSet.toList())
                        )
                    } else {
                        "请给予相应权限".toast()
                        finish()
                    }
                }
    }
}
