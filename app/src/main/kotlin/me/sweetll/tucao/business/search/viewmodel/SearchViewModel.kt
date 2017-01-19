package me.sweetll.tucao.business.search.viewmodel

import android.databinding.ObservableField
import android.view.View
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.extension.hideSoftKeyboard
import me.sweetll.tucao.extension.sanitizeJsonList
import me.sweetll.tucao.extension.toast

class SearchViewModel(val activity: SearchActivity, keyword: String? = null, var tid: Int? = null): BaseViewModel()  {
    val searchText = ObservableField<String>()
    var lastKeyword = ""

    var pageIndex = 1
    var pageSize = 10
    var order = "date"

    init {
        keyword?.let {
            lastKeyword = it
            loadData()
        }
    }

    fun loadData() {
        activity.setRefreshing(true)
        pageIndex = 1
        jsonApiService.search(tid, pageIndex, pageSize, order, lastKeyword)
                .bindToLifecycle(activity)
                .sanitizeJsonList()
                .doAfterTerminate { activity.setRefreshing(false) }
                .subscribe({
                    data ->
                    pageIndex++
                    activity.loadData(data)
                }, {
                    error ->
                    error.message?.toast()
                })
    }

    fun loadMoreData() {
        jsonApiService.search(tid, pageIndex, pageSize, order, lastKeyword)
                .bindToLifecycle(activity)
                .sanitizeJsonList()
                .subscribe({
                    data ->
                    if (data!!.size < pageSize) {
                        activity.loadMoreData(data, Const.LOAD_MORE_END)
                    } else {
                        activity.loadMoreData(data, Const.LOAD_MORE_COMPLETE)
                    }
                }, {
                    error ->
                    activity.loadMoreData(null, Const.LOAD_MORE_FAIL)
                    error.message?.toast()
                })
    }

    fun onClickBack(view: View) {
        activity.onBackPressed()
    }

    fun onClickSearch(view: View) {
        if (searchText.get().isNotEmpty()) {
            activity.hideSoftKeyboard()
            lastKeyword = searchText.get()
            loadData()
        }
    }

    fun onFilterChannel(view: View) {

    }

    fun onFilterOrder(view: View) {

    }
}
