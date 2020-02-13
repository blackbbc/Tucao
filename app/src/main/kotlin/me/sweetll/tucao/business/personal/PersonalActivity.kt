package me.sweetll.tucao.business.personal

import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.transition.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentManager
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.personal.fragment.ChangeInformationFragment
import me.sweetll.tucao.business.personal.fragment.ChangePasswordFragment
import me.sweetll.tucao.business.personal.fragment.PersonalFragment
import me.sweetll.tucao.databinding.ActivityPersonalBinding

class PersonalActivity : BaseActivity() {

    private lateinit var binding: ActivityPersonalBinding

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    lateinit var fm: FragmentManager

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, PersonalActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_personal)
        fm = supportFragmentManager
        loadInitialFragment()
    }


    private fun loadInitialFragment() {
        val personalFragment = PersonalFragment()
        fm.beginTransaction()
                .replace(R.id.fragment_container, personalFragment)
                .commit()
    }

    fun transitionToChangeInformation() {
        if (isDestroyed) return

        val changeInformationFragment = ChangeInformationFragment()

        val ft = fm.beginTransaction()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val enterTransitionSet = ChangeBounds()
            changeInformationFragment.sharedElementEnterTransition = enterTransitionSet

            val enterTransition = Fade()
            changeInformationFragment.enterTransition = enterTransition

            val nickname = findViewById<View>(R.id.nicknameContainer)
            val signature = findViewById<View>(R.id.signatureContainer)
            ft.addSharedElement(nickname, nickname.transitionName)
            ft.addSharedElement(signature, signature.transitionName)
        }

        ft.replace(R.id.fragment_container, changeInformationFragment)
                .addToBackStack("changeInformation")
                .commit()
    }

    fun transitionToChangePassword() {
        if (isDestroyed) return

        val changePasswordFragment = ChangePasswordFragment()

        fm.beginTransaction()
                .replace(R.id.fragment_container, changePasswordFragment)
                .addToBackStack("changePassword")
                .commit()
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "帐号资料"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
