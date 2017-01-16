package me.sweetll.tucao.business.channel.viewmodel

import android.view.View
import android.widget.PopupMenu
import me.sweetll.tucao.base.BaseViewModel
import android.widget.Toast
import me.sweetll.tucao.R
import me.sweetll.tucao.business.channel.ChannelDetailActivity
import me.sweetll.tucao.business.channel.event.ChangeChannelFilterEvent
import org.greenrobot.eventbus.EventBus


class ChannelDetailViewModel(val activity: ChannelDetailActivity) : BaseViewModel() {
    init {

    }

    fun onClickOrderFilters(view: View) {
        showFilterPopup(view)
    }

    private fun showFilterPopup(view: View) {
        val popup = PopupMenu(activity, view)
        popup.menuInflater.inflate(R.menu.popup_order_filter, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_date -> {
                    activity.getToolbar().subtitle = "最近发布"
                    EventBus.getDefault().post(ChangeChannelFilterEvent("date"))
                    true
                }
                R.id.menu_views -> {
                    activity.getToolbar().subtitle = "人气最旺"
                    EventBus.getDefault().post(ChangeChannelFilterEvent("views"))
                    true
                }
                R.id.menu_mukio -> {
                    activity.getToolbar().subtitle = "弹幕最多"
                    EventBus.getDefault().post(ChangeChannelFilterEvent("mukio"))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

}
