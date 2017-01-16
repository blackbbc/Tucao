package me.sweetll.tucao.business.home.viewmodel

import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.fragment.RecommendFragment
import me.sweetll.tucao.extension.logD
import me.sweetll.tucao.extension.sanitizeHtml
import me.sweetll.tucao.extension.toast

class RecommandViewModel(fragment: RecommendFragment): BaseViewModel() {
    init {
        loadData()
    }

    fun loadData() {
        rawApiService.index()
                .sanitizeHtml({

                })
                .subscribe({
                    doc ->
                    "成功".logD()
                }, {
                    error ->
                    error.printStackTrace()
                    error.message?.toast()
                })
    }

}
