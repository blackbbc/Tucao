package me.sweetll.tucao.business.home.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bigkoo.convenientbanner.holder.Holder
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.raw.Banner
import me.sweetll.tucao.widget.TopCropImageView

class BannerHolder: Holder<Banner> {
    lateinit var imageView: ImageView

    override fun createView(context: Context): View {
        imageView = TopCropImageView(context)
        return imageView
    }

    override fun UpdateUI(context: Context, position: Int, banner: Banner) {
        imageView.load(banner.imgUrl)
        imageView.setOnClickListener {
            view ->
            if (banner.hid != null) {
                VideoActivity.intentTo(context, banner.hid)
            } else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.linkUrl))
                context.startActivity(intent)
            }
        }
    }

}
