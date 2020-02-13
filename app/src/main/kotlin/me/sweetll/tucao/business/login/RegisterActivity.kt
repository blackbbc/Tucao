package me.sweetll.tucao.business.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewTreeObserver
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.snackbar.Snackbar
import io.codetail.animation.ViewAnimationUtils
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.business.login.viewmodel.RegisterViewModel
import me.sweetll.tucao.databinding.ActivityRegisterBinding
import me.sweetll.tucao.extension.dp2px
import me.sweetll.tucao.extension.logD
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
        "开始注册".logD()
        morphToCircle(binding.registerBtn, 500)
    }

    fun registerSuccess() {
        "注册成功".logD()
        val cx = binding.registerBtn.left + binding.registerBtn.width / 2
        val cy = binding.registerBtn.top + binding.registerBtn.height / 2 + binding.statusBar.height + binding.toolbar.height
        val startRadius = buttonHeight / 2f
        val finalRadius = Math.hypot(cy.toDouble(), cx.toDouble()) + 10f.dp2px()
        val animator = ViewAnimationUtils.createCircularReveal(binding.revealView, cx, cy, startRadius, finalRadius.toFloat())
        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = 300
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.revealView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })
        animator.start()
    }

    fun registerFailed(msg: String) {
        morphToSquare(binding.registerBtn, 500)
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
            it.title = "注册帐号"
        }
    }
}
