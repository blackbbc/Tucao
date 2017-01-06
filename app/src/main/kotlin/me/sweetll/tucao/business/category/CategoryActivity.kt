package me.sweetll.tucao.business.category

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import me.sweetll.tucao.base.BaseActivity

class CategoryActivity : BaseActivity() {

    companion object {
        val ARG_TID = "arg_tid"

        fun intentTo(context: Context, tid: Int) {
            val intent = Intent(context, CategoryActivity::class.java)
            intent.putExtra(ARG_TID, tid)
            context.startActivity(intent)
        }
    }

    override fun getToolbar(): Toolbar {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initView(savedInstanceState: Bundle?) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
