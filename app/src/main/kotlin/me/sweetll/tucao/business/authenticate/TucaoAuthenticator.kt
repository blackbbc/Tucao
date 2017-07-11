package me.sweetll.tucao.business.authenticate

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.di.service.RawApiService
import javax.inject.Inject

class TucaoAuthenticator(val context: Context): AbstractAccountAuthenticator(context) {

    override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle? {
        val intent = Intent(context, AuthenticatorActivity::class.java)
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType)
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
        intent.putExtra(AuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account, authTokenType: String?, options: Bundle?): Bundle? {
        val am = AccountManager.get(context)

        val authToken = am.peekAuthToken(account, authTokenType)

        if (!authToken.isNullOrEmpty()) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        val intent = Intent(context, AuthenticatorActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_NAME, account.name)
        intent.putExtra(AuthenticatorActivity.ARG_ACCOUNT_TYPE, account.type)
        intent.putExtra(AuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String): String? {
        if (authTokenType == AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS) {
            return AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL
        } else {
            return "$authTokenType (Label)"
        }
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle? {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        return result
    }

    override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle? {
        return null
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle? {
        return null
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
        return null
    }

}