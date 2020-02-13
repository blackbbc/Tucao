package me.sweetll.tucao.business.video.fragment

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.jakewharton.rxbinding2.widget.RxTextView
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.R
import me.sweetll.tucao.business.video.adapter.BlockListAdapter
import me.sweetll.tucao.extension.BlockListHelpers
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingBlockViewFactory() {

    companion object {

        fun create(player: DanmuVideoPlayer, container: ViewGroup): View {
            val view = LayoutInflater.from(player.context).inflate(R.layout.fragment_setting_block, container, false)

            val blockSwitch: SwitchCompat = view.findViewById(R.id.switch_block)
            val keywordEdit: EditText = view.findViewById(R.id.edit_keyword)
            val addKeywordBtn: Button = view.findViewById(R.id.btn_add_keyword)
            val blockListRecycler: RecyclerView = view.findViewById(R.id.recycler_block_list)

            val blockListAdapter = BlockListAdapter(BlockListHelpers.loadBlockList())

            blockSwitch.isChecked = BlockListHelpers.isEnabled()

            RxTextView.textChanges(keywordEdit)
                    .map { text -> text.isNotEmpty() }
                    .distinctUntilChanged()
                    .subscribe {
                        enable ->
                        addKeywordBtn.isEnabled = enable
                    }

            blockSwitch.setOnCheckedChangeListener {
                _, checked ->
                BlockListHelpers.setEnabled(checked)
                "重新播放后生效".toast()
            }

            addKeywordBtn.setOnClickListener {
                val keyword = keywordEdit.text.toString()
                keywordEdit.setText("")
                BlockListHelpers.add(keyword)
                blockListAdapter.addData(0, keyword)
            }

            blockListRecycler.adapter = blockListAdapter
            blockListRecycler.layoutManager = LinearLayoutManager(view.context)

            blockListRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
                override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                    if (view.id == R.id.img_close) {
                        val keyword = blockListAdapter.getItem(position)!!
                        BlockListHelpers.remove(keyword)
                        blockListAdapter.remove(position)
                    }
                }
            })

            return view
        }
    }
}
