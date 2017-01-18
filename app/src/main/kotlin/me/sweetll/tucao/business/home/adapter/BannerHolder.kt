package me.sweetll.tucao.business.home.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bigkoo.convenientbanner.holder.Holder
import me.sweetll.tucao.R
import me.sweetll.tucao.business.browser.BrowserActivity
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.widget.TopCropImageView

class BannerHolder: Holder<Banner> {
    lateinit var rootView: View

    override fun createView(context: Context): View {
        rootView = LayoutInflater.from(context).inflate(R.layout.item_banner, null)
        return rootView
    }

    override fun UpdateUI(context: Context, position: Int, banner: Banner) {
        val bannerImg = rootView.findViewById(R.id.img_banner) as ImageView
        bannerImg.load(banner.imgUrl)
        bannerImg.setOnClickListener {
            view ->
            if (banner.hid != null) {
                VideoActivity.intentTo(context, banner.hid)
            } else {
                BrowserActivity.intentTo(context, banner.linkUrl)
            }
        }
        banner.title?.let {
            val titleLinear = rootView.findViewById(R.id.linear_title)
            titleLinear.visibility = View.VISIBLE
            val titleText = rootView.findViewById(R.id.text_title) as TextView
            titleText.text = it
        }
    }

}
