package me.sweetll.tucao.business.personal

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.personal.viewmodel.PersonalViewModel
import me.sweetll.tucao.databinding.ActivityPersonalBinding

class PersonalActivity : BaseActivity() {

    private lateinit var binding: ActivityPersonalBinding
    private lateinit var viewModel: PersonalViewModel

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, PersonalActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_personal)
        viewModel = PersonalViewModel(this)
        binding.viewModel = viewModel
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.title = "帐号资料"
            it.setDisplayHomeAsUpEnabled(true)
        }
    }
}
