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
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.download.model.Part
import me.sweetll.tucao.business.video.adapter.DownloadPartAdapter
import me.sweetll.tucao.business.video.fragment.VideoInfoFragment
import me.sweetll.tucao.extension.DownloadHelpers
import me.sweetll.tucao.extension.HistoryHelpers
import me.sweetll.tucao.model.json.Result
import me.sweetll.tucao.widget.CustomBottomSheetDialog

class VideoInfoViewModel(val videoInfoFragment: VideoInfoFragment): BaseViewModel() {
    val result: ObservableField<Result> = ObservableField()
    val isStar = ObservableBoolean()

    fun bindResult(result: Result) {
        this.result.set(result)
        this.isStar.set(checkStar(result))
    }

    fun checkStar(result: Result): Boolean = HistoryHelpers.loadStar()
            .any { it.hid == result.hid }

    fun onClickDownload(view: View) {
        if (result.get() == null) return
        val dialog = CustomBottomSheetDialog(videoInfoFragment.activity)
        val dialogView = LayoutInflater.from(videoInfoFragment.activity).inflate(R.layout.dialog_pick_download_video, null)
        dialog.setContentView(dialogView)

        dialogView.findViewById(R.id.img_close).setOnClickListener {
            dialog.dismiss()
        }

        val partRecycler = dialogView.findViewById(R.id.recycler_part) as RecyclerView
        val partAdapter = DownloadPartAdapter(
                videoInfoFragment.parts
                        .map {
                            it.copy().apply { checked = false }
                        }
                        .toMutableList()
        )

        val startDownloadButton = dialog.findViewById(R.id.btn_start_download) as Button
        startDownloadButton.setOnClickListener {
            view ->
            val checkedParts = partAdapter.data.filter(Part::checked)
            DownloadHelpers.startDownload(videoInfoFragment.activity, result.get().copy().apply {
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

}
