package me.sweetll.tucao.business.home.adapter

import android.app.Activity
import androidx.core.util.Pair
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityOptionsCompat
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

class BannerHolder: Holder<Banner> {
    lateinit var rootView: View

    override fun createView(context: Context): View {
        rootView = LayoutInflater.from(context).inflate(R.layout.item_banner, null)
        return rootView
    }

    override fun UpdateUI(context: Context, position: Int, banner: Banner) {
        val bannerImg = rootView.findViewById<ImageView>(R.id.img_banner)
        val bg = rootView.findViewById<View>(R.id.bg)
        bannerImg.load(context, banner.imgUrl)
        bannerImg.setOnClickListener {
            if (banner.hid != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val p1: Pair<View, String> = Pair.create(bannerImg, "cover")
                    val p2: Pair<View, String> = Pair.create(bg, "bg")
                    val options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(context as Activity, p1, p2)
                    VideoActivity.intentTo(context, banner.hid, banner.imgUrl, options.toBundle())
                } else {
                    VideoActivity.intentTo(context, banner.hid)
                }
            } else {
                BrowserActivity.intentTo(context, banner.linkUrl)
            }
        }
        banner.title?.let {
            val titleLinear = rootView.findViewById<View>(R.id.linear_title)
            titleLinear.visibility = View.VISIBLE
            val titleText = rootView.findViewById<TextView>(R.id.text_title)
            titleText.text = it
        }
    }

}
