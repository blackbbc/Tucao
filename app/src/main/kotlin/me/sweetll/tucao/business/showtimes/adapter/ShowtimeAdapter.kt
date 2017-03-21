package me.sweetll.tucao.business.showtimes.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import me.sweetll.tucao.R
import me.sweetll.tucao.extension.load
import me.sweetll.tucao.model.raw.ShowtimeSection

class ShowtimeAdapter(data: MutableList<ShowtimeSection>?): BaseSectionQuickAdapter<ShowtimeSection, BaseViewHolder>(R.layout.item_showtime, R.layout.item_showtime_header, data) {
    override fun convertHead(helper: BaseViewHolder, showtimeSection: ShowtimeSection) {
        helper.setText(R.id.text_week, showtimeSection.header)
    }

    override fun convert(helper: BaseViewHolder, showtimeSection: ShowtimeSection) {
        helper.setText(R.id.text_title, showtimeSection.t.title)
        val thumbImg = helper.getView<ImageView>(R.id.img_thumb)
        thumbImg.load(mContext, showtimeSection.t.thumb)
    }
}
