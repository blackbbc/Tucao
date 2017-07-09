package me.sweetll.tucao.business.login

import android.app.Service
import android.content.Intent
import android.os.IBinder

class TucaoAuthenticatorService: Service() {
    override fun onBind(intent: Intent?): IBinder {
        val authenticator = TucaoAuthenticator(this)
        return authenticator.iBinder
    }
}
