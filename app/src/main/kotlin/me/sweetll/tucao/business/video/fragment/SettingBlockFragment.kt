package me.sweetll.tucao.business.video.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.jakewharton.rxbinding2.widget.RxTextView
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.business.video.adapter.BlockListAdapter
import me.sweetll.tucao.extension.BlockListHelpers
import me.sweetll.tucao.extension.toast
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingBlockFragment(val player: DanmuVideoPlayer): BaseFragment() {

    lateinit var blockSwitch: SwitchCompat
    lateinit var keywordEdit: EditText
    lateinit var addKeywordBtn: Button
    lateinit var blockListRecycler: RecyclerView

    val blockListAdapter = BlockListAdapter(BlockListHelpers.loadBlockList())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_block, container, false)
        blockSwitch = view.findViewById(R.id.switch_block)
        keywordEdit = view.findViewById(R.id.edit_keyword)
        addKeywordBtn = view.findViewById(R.id.btn_add_keyword)
        blockListRecycler = view.findViewById(R.id.recycler_block_list)

        blockSwitch.isChecked = BlockListHelpers.isEnabled()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            BlockListHelpers.add(keyword)
            blockListAdapter.addData(0, keyword)
        }

        blockListRecycler.adapter = blockListAdapter
        blockListRecycler.layoutManager = LinearLayoutManager(activity)

        blockListRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                if (view.id == R.id.img_close) {
                    val keyword = blockListAdapter.getItem(position)
                    BlockListHelpers.remove(keyword)
                    blockListAdapter.remove(position)
                }
            }
        })
    }
}
