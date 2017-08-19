package me.sweetll.tucao.business.showtimes.viewmodel

import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.showtimes.ShowtimeActivity
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.model.raw.ShowtimeSection
import org.jsoup.nodes.Document
import java.util.*

class ShowtimeViewModel(val activity: ShowtimeActivity): BaseViewModel() {

    init {
        loadData()
    }

    fun loadData() {
        activity.setRefreshing(true)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
        calendar.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) / 3 * 3 + 1

        rawApiService.bgm(year, month)
                .bindToLifecycle(activity)
                .sanitizeHtml {
                    parseShowtime(this)
                }
                .doAfterTerminate { activity.setRefreshing(false) }
                .subscribe({
                    showtime ->
                    activity.loadShowtime(showtime)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun parseShowtime(doc: Document): MutableList<ShowtimeSection> {
        val bgm_list = doc.select("div.bgm_list")
                .map { it.child(1) }
        val bgm_list2 = bgm_list.subList(1, 7).plus(bgm_list.first()) // 把周日移到最后一个位置
        val week_day = listOf("月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日")
        val weekZipBgm = week_day zip bgm_list2
        val showtime = weekZipBgm.fold(mutableListOf<ShowtimeSection>()) {
            total, zipElement ->
            val weekDay = zipElement.first
            total.add(ShowtimeSection(weekDay))
            zipElement.second
                    .children()
                    .map { it.child(0) }
                    .forEach {
                        val thumb = it.child(0).attr("src")
                        val title = it.child(1).text()
                        total.add(ShowtimeSection(Video(thumb = thumb, title = title)))
                    }
            total
        }
        return showtime
    }

    fun onClickWeek(view: View) {
        activity.showWeek((view.tag as String).toInt(), view)
    }
}
