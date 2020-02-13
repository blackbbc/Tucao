package me.sweetll.tucao.business.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import me.sweetll.tucao.BuildConfig
import me.sweetll.tucao.R
import me.sweetll.tucao.business.browser.BrowserActivity

import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutActivity : AppCompatActivity() {

    companion object {
        fun intentTo(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val aboutPage = AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.logo)
                .setDescription("自制第三方吐槽客户端，如果在使用的过程中遇到任何问题，欢迎通过以下任一联系方式向我反馈，我会及时修复。如果你觉得界面有可以改善的地方，也欢迎向我砸设计稿。该项目仅供交流学习使用，如果该项目有侵犯吐槽版权问题，本人会及时删除整个项目。")
                .addItem(Element().setTitle("版本: ${BuildConfig.VERSION_NAME}"))
                .addItem(Element().setTitle("更新历史").setOnClickListener { BrowserActivity.intentTo(this, "https://github.com/blackbbc/Tucao/blob/master/changelog.md") })
                .addItem(Element().setTitle("常见问题").setOnClickListener { BrowserActivity.intentTo(this, "https://github.com/blackbbc/Tucao/blob/master/FAQ.md") })
                .addEmail("505968815@qq.com", "反馈")
                .addWebsite("https://github.com/blackbbc/tucao", "开源地址")
                .create()

        setContentView(aboutPage)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}
