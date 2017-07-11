package me.sweetll.tucao.business.authenticate

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View

import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.authenticate.viewmodel.AuthenticatorViewModel
import me.sweetll.tucao.databinding.ActivityAuthenticatorBinding

class AuthenticatorActivity : BaseActivity() {
    lateinit var binding: ActivityAuthenticatorBinding
    lateinit var viewModel: AuthenticatorViewModel

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    private lateinit var accountManager: AccountManager
    private lateinit var authTokenType: String

    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    fun setAccountAuthenticatorResult(result: Bundle) {
        resultBundle = result
    }

    companion object {
        const val ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE"
        const val ARG_AUTH_TYPE = "AUTH_TYPE"
        const val ARG_ACCOUNT_NAME = "ACCOUNT_NAME"
        const val ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT"

        const val PARAM_USER_PASS = "USER_PASS"

        fun intentTo(context: Context) {
            val intent = Intent(context, AuthenticatorActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authenticator)
        viewModel = AuthenticatorViewModel(this, intent.getStringExtra(ARG_ACCOUNT_NAME), intent.getStringExtra(ARG_ACCOUNT_TYPE))
        binding.viewModel = viewModel

        accountManager = AccountManager.get(this)
        authTokenType = intent.getStringExtra(ARG_AUTH_TYPE)

        accountAuthenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "登陆"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        accountAuthenticatorResponse?.let {
            // send the result bundle back if set, otherwise send an error.
            if (resultBundle != null) {
                it.onResult(resultBundle)
            } else {
                it.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            }
            accountAuthenticatorResponse = null
        }
        super.finish()
    }

    fun finishLogin(res: Intent) {
        val accountName = res.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountPassword = res.getStringExtra(PARAM_USER_PASS)
        val account = Account(accountName, res.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
        if (intent.getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            val authToken = res.getStringExtra(AccountManager.KEY_AUTHTOKEN)
            accountManager.addAccountExplicitly(account, accountPassword, null)
            accountManager.setAuthToken(account, authTokenType, authToken)
        } else {
            accountManager.setPassword(account, accountPassword)
        }
        setAccountAuthenticatorResult(res.extras)
        setResult(Activity.RESULT_OK, res)
        finish()
    }

}
