package me.sweetll.tucao.business.video.viewmodel

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.business.video.adapter.DownloadPartAdapter
import me.sweetll.tucao.di.service.ApiConfig
import me.sweetll.tucao.extension.*
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.model.xml.Durl
import me.sweetll.tucao.widget.CustomBottomSheetDialog
import zlc.season.rxdownload2.entity.DownloadFlag
import java.io.File
import java.io.FileOutputStream

class VideoViewModel(val activity: VideoActivity): BaseViewModel() {
    val result = ObservableField<Result>()
    val isStar = ObservableBoolean()

    var playUrlDisposable: Disposable? = null
    var danmuDisposable: Disposable? = null

    constructor(activity: VideoActivity, result: Result) : this(activity) {
        this.result.set(result)
        this.isStar.set(checkStar(result))
    }

    fun queryResult(hid: String) {
        /*
        val result = Result("title")
        result.video.addAll(arrayOf(
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123"),
                Video("title1", "189", "123")
        ))
        this.result.set(result)
        activity.loadResult(result)
        */
        jsonApiService.view(hid)
                .bindToLifecycle(activity)
                .sanitizeJson()
                .subscribe({
                    result ->
                    this.result.set(result)
                    this.isStar.set(checkStar(result))
                    activity.loadResult(result)
                }, {
                    error ->
                    error.printStackTrace()
                    activity.binding.player.loadText?.let {
                        it.text = it.text.replace("获取视频信息...".toRegex(), "获取视频信息...[失败]")
                    }
                })
    }

    fun queryPlayUrls(hid: String, part: Part) {
        if (playUrlDisposable != null && !playUrlDisposable!!.isDisposed) {
            playUrlDisposable!!.dispose()
        }
        if (danmuDisposable != null && !danmuDisposable!!.isDisposed) {
            danmuDisposable!!.dispose()
        }

        if (part.flag == DownloadFlag.COMPLETED) {
            activity.loadDuals(part.durls)
        } else {
            playUrlDisposable = xmlApiService.playUrl(part.type, part.vid, System.currentTimeMillis() / 1000)
                    .bindToLifecycle(activity)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        response ->
                        if ("succ" == response.result) {
                            Observable.just(response.durls)
                        } else {
                            Observable.error(Throwable("请求视频接口出错"))
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        duals ->
                        activity.loadDuals(duals)
                    }, {
                        error ->
                        error.printStackTrace()
                        activity.binding.player.loadText?.let {
                            it.text = it.text.replace("解析视频地址...".toRegex(), "解析视频地址...[失败]")
                        }
                    })
        }

        danmuDisposable = rawApiService.danmu(ApiConfig.generatePlayerId(hid, part.order), System.currentTimeMillis() / 1000)
                .bindToLifecycle(activity)
                .subscribeOn(Schedulers.io())
                .map({
                    responseBody ->
                    val outputFile = File.createTempFile("tucao", ".xml", AppApplication.get().cacheDir)
                    val outputStream = FileOutputStream(outputFile)

                    outputStream.write(responseBody.bytes())
                    outputStream.flush()
                    outputStream.close()
                    outputFile.absolutePath
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uri ->
                    activity.loadDanmuUri(uri)
                }, {
                    error ->
                    error.printStackTrace()
                    activity.binding.player.loadText?.let {
                        it.text = it.text.replace("全舰弹幕装填...".toRegex(), "全舰弹幕装填...[失败]")
                    }
                })
    }

    fun onClickDownload(view: View) {
        if (result.get() == null) return
        val dialog = CustomBottomSheetDialog(activity)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_pick_download_video, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById(R.id.img_close).setOnClickListener {
            dialog.dismiss()
        }

        val partRecycler = dialogView.findViewById(R.id.recycler_part) as RecyclerView
        val partAdapter = DownloadPartAdapter(
                activity.parts!!
                        .map {
                            it.copy().apply { checked = false }
                        }
                        .toMutableList()
        )

        val startDownloadButton = dialog.findViewById(R.id.btn_start_download) as Button
        startDownloadButton.setOnClickListener {
            view ->
            val checkedParts = partAdapter.data.filter(Part::checked)
            DownloadHelpers.startDownload(activity, result.get().copy().apply {
                video = video.filter {
                    v ->
                    checkedParts.any { v.vid == it.vid }
                }.toMutableList()
            })
            dialog.dismiss()
        }

        partRecycler.addOnItemTouchListener(object: OnItemClickListener() {
            override fun onSimpleItemClick(helper: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val part = helper.getItem(position) as Part
                part.checked = !part.checked
                helper.notifyItemChanged(position)
                startDownloadButton.isEnabled = partAdapter.data.any(Part::checked)
            }

        })

        partRecycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        partRecycler.adapter = partAdapter

        val pickAllButton = dialog.findViewById(R.id.btn_pick_all) as Button
        pickAllButton.setOnClickListener {
            view ->
            if (partAdapter.data.all { it.checked }) {
                // 取消全选
                startDownloadButton.isEnabled = false
                pickAllButton.text = "全部选择"
                partAdapter.data.forEach {
                    item ->
                    item.checked = false
                }
            } else {
                // 全选
                startDownloadButton.isEnabled = true
                pickAllButton.text = "取消全选"
                partAdapter.data.forEach {
                    item ->
                    item.checked = true
                }
            }
            partAdapter.notifyDataSetChanged()
        }

        dialog.show()
    }

    fun onClickStar(view: View) {
        if (result.get() == null) return
        if (isStar.get()) {
            HistoryHelpers.removeStar(result.get())
            isStar.set(false)
        } else {
            HistoryHelpers.saveStar(result.get())
            isStar.set(true)
        }
    }

    fun checkStar(result: Result): Boolean = HistoryHelpers.loadStar()
            .any { it.hid == result.hid }

}
