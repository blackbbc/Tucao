package me.sweetll.tucao.business.search.viewmodel

import android.databinding.ObservableField
import android.view.View
import me.sweetll.tucao.Const
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.search.SearchActivity
import me.sweetll.tucao.extension.hideSoftKeyboard
import me.sweetll.tucao.extension.sanitizeJsonList
import me.sweetll.tucao.extension.toast

class SearchViewModel(val activity: SearchActivity): BaseViewModel()  {
    val searchText = ObservableField<String>()
    var lastKeyword = ""

    var pageIndex = 1
    var pageSize = 10

    fun loadData() {
        pageIndex = 1
        jsonApiService.search(null, pageIndex, pageSize, null, lastKeyword)
                .sanitizeJsonList()
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
        jsonApiService.search(null, pageIndex, pageSize, null, lastKeyword)
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
}
