package me.sweetll.tucao.business.search.viewmodel

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.extension.*

class SearchViewModel(val activity: SearchActivity, keyword: String? = null, var tid: Int? = null, var order: String? = "date"): BaseViewModel()  {
    val searchText = NonNullObservableField("")
    val channelFilterText = NonNullObservableField("全部分类")
    val orderFilterText = NonNullObservableField("发布时间")
    val totalCountVisibility = ObservableInt(View.INVISIBLE) // total_count没有用. Fuck!
    val searchResultVisibility = ObservableInt(View.GONE)
    val searchHistoryVisibility = ObservableInt(View.VISIBLE)
    val totalCount = ObservableInt(0)
    var lastKeyword = ""

    var pageIndex = 1
    var pageSize = 10

    var isShowChannelDropDown = false
    var isShowOrderDropDown = false

    init {
        updateFilterText()
        activity.loadHistory(HistoryHelpers.loadSearchHistory())
        keyword?.let {
            lastKeyword = it
            searchText.set(it)
            loadData()
        }
    }

    fun loadData() {
        if (lastKeyword.isEmpty()) return
        activity.setRefreshing(true)
        pageIndex = 1
        jsonApiService.search(tid, pageIndex, pageSize, order, lastKeyword)
                .bindToLifecycle(activity)
                .sanitizeJsonList()
                .doAfterTerminate {
                    searchResultVisibility.set(View.VISIBLE)
                    searchHistoryVisibility.set(View.GONE)
                    activity.setRefreshing(false)
                }
                .map{
                    response ->
                    totalCount.set(response.totalCount)
                    response.result!!
                }
                .subscribe({
                    data ->
                    pageIndex++
                    activity.loadData(data)
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

    fun loadMoreData() {
        jsonApiService.search(tid, pageIndex, pageSize, order, lastKeyword)
                .bindToLifecycle(activity)
                .sanitizeJsonList()
                .map{
                    response ->
                    response.result!!
                }
                .subscribe({
                    data ->
                    if (data.size < pageSize) {
                        activity.loadMoreData(data, Const.LOAD_MORE_END)
                    } else {
                        pageIndex++
                        activity.loadMoreData(data, Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                    activity.loadMoreData(null, Const.LOAD_MORE_FAIL)
                })
    }

    fun onClickBack(view: View) {
        activity.onBackPressed()
    }

    fun onClickSearch(view: View) {
        if (searchText.get().isNotEmpty()) {
            activity.hideSoftKeyboard()
            lastKeyword = searchText.get()
            HistoryHelpers.saveSearchHistory(Video(title = lastKeyword))
            activity.loadHistory(HistoryHelpers.loadSearchHistory())
            loadData()
        }
    }

    fun onSearchTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty()) {
            activity.clearData()
            searchHistoryVisibility.set(View.VISIBLE)
            searchResultVisibility.set(View.GONE)
        } /*else {
            searchHistoryVisibility.set(View.GONE)
            searchResultVisibility.set(View.VISIBLE)
        } */
    }

    fun hideAllDropDown(): Boolean {
        if (isShowChannelDropDown) {
            isShowChannelDropDown = false
            activity.hideChannelDropDownList()
            return true
        }
        if (isShowOrderDropDown) {
            isShowOrderDropDown = false
            activity.hideOrderDropDownList()
            return true
        }
        return false
    }

    fun onClickMask(view: View) {
        hideAllDropDown()
    }

    fun onToggleChannelFilter(view: View) {
        if (!hideAllDropDown()) {
            isShowChannelDropDown = true
            activity.showChannelDropDownList()
        }
    }

    fun onToggleOrderFilter(view: View) {
        if (!hideAllDropDown()) {
            isShowOrderDropDown = true
            activity.showOrderDropDownList()
        }
    }

    fun onFilterChannel(view: View) {
        if (view.tag != null) {
            tid = (view.tag as String).toInt()
        } else {
            tid = null
        }
        updateFilterText()
        onToggleChannelFilter(view)
        loadData()
    }

    fun onFilterOrder(view: View) {
        order = view.tag as String
        updateFilterText()
        onToggleOrderFilter(view)
        loadData()
    }

    fun updateFilterText() {
        channelFilterText.set(when(tid) {
            19 -> "动画"
            20 -> "音乐"
            21 -> "游戏"
            22 -> "三次元"
            23 -> "影剧"
            24 -> "新番"
            else -> "全部分类"
        })
        orderFilterText.set(when(order) {
            "date" -> "发布时间"
            "mukio" -> "弹幕数"
            else -> "播放数"
        })
    }

}
