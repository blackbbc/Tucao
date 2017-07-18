package me.sweetll.tucao.business.login

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.viewmodel.LoginViewModel
import me.sweetll.tucao.databinding.ActivityLoginBinding
import me.sweetll.tucao.extension.hideSoftKeyboard
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.transition.FabTransform

class LoginActivity : BaseActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: LoginViewModel

    private lateinit var accountManager: AccountManager

    companion object {
        fun intentTo(context: Context, requestCode: Int = 1, options: Bundle?) {
            val intent = Intent(context, LoginActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                FabTransform.addExtras(intent, ContextCompat.getColor(context, R.color.colorAccent), R.drawable.default_avatar)
            }
            ActivityCompat.startActivityForResult(context as Activity, intent, requestCode, options)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = LoginViewModel(this)
        binding.viewModel = viewModel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            FabTransform.setup(this, binding.container)
        }

        setupAccountAutocomplete()

        val validEmail = RxTextView.textChanges(binding.emailEdit)
                .map { text -> Patterns.EMAIL_ADDRESS.matcher(text).matches() }
        val validPassword = RxTextView.textChanges(binding.passwordEdit)
                .map { text -> text.isNotEmpty() }
        val validCode = RxTextView.textChanges(binding.codeEdit)
                .map { text -> text.isNotEmpty() }
        Observables.combineLatest(validEmail, validPassword, validCode, {a, b, c -> a and b and c})
                .distinctUntilChanged()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    enable ->
                    binding.signInBtn.isEnabled = enable
                }
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

    fun showLoading() {
        TransitionManager.beginDelayedTransition(binding.container)
        viewModel.container.set(View.GONE)
        viewModel.progress.set(View.VISIBLE)
    }

    fun showLogin() {
        TransitionManager.beginDelayedTransition(binding.container)
        viewModel.container.set(View.VISIBLE)
        viewModel.progress.set(View.GONE)
    }
}
