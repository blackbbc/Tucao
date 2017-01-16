package me.sweetll.tucao.business.home.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.bigkoo.convenientbanner.holder.Holder
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.raw.Banner

class BannerHolder: Holder<Banner> {
    lateinit var imageView: ImageView

    override fun createView(context: Context?): View {
        imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        return imageView
    }

    override fun UpdateUI(context: Context?, position: Int, data: Banner) {
        imageView.load(data.imgUrl)
    }

}
