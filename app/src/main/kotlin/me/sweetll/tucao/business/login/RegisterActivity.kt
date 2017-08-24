package me.sweetll.tucao.business.login

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewTreeObserver
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.viewmodel.RegisterViewModel
import me.sweetll.tucao.databinding.ActivityRegisterBinding
import me.sweetll.tucao.extension.dp2px
import me.sweetll.tucao.widget.MorphingButton

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun getStatusBar(): View = binding.statusBar

    override fun getToolbar(): Toolbar = binding.toolbar

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, RegisterActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var buttonHeight: Int = 0
    private var buttonWidth: Int = 0

    override fun initView(savedInstanceState: Bundle?) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        viewModel = RegisterViewModel(this)
        binding.viewModel = viewModel

        binding.registerBtn.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.registerBtn.viewTreeObserver.removeOnGlobalLayoutListener(this)
                buttonWidth = binding.registerBtn.width
                buttonHeight = binding.registerBtn.height
                morphToSquare(binding.registerBtn, 0)
            }
        })
    }

    fun startRegister() {
        morphToCircle(binding.registerBtn, 500)
    }

    fun registerSuccess() {
        morphToSquare(binding.registerBtn, 500)
    }

    fun registerFailed(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun morphToSquare(morphBtn: MorphingButton, duration: Int) {
        val square = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(20f.dp2px().toInt())
                .width(buttonWidth)
                .height(buttonHeight)
                .color(ContextCompat.getColor(this, R.color.pink_400))
                .colorPressed(ContextCompat.getColor(this, R.color.pink_700))
                .text("创建新用户")
        morphBtn.morph(square)
    }

    private fun morphToCircle(morphBtn: MorphingButton, duration: Int) {
        val circle = MorphingButton.Params.create()
                .duration(duration)
                .cornerRadius(buttonHeight)
                .width(buttonHeight)
                .height(buttonHeight)
                .color(ContextCompat.getColor(this, R.color.pink_400))
                .colorPressed(ContextCompat.getColor(this, R.color.pink_700))
                .icon(R.drawable.ic_code)
        morphBtn.morph(circle)
    }

    override fun initToolbar() {
        super.initToolbar()
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = ""
        }
    }
}
