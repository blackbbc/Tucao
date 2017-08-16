package me.sweetll.tucao.business.video.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import me.sweetll.tucao.R
import me.sweetll.tucao.base.BaseFragment
import me.sweetll.tucao.widget.DanmuVideoPlayer

class SettingBlockFragment(val player: DanmuVideoPlayer): BaseFragment() {

    lateinit var blockSwitch: SwitchCompat
    lateinit var keywordEdit: EditText
    lateinit var addKeywordBtn: Button
    lateinit var blockListRecycler: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setting_block, container, false)
        blockSwitch = view.findViewById(R.id.switch_block)
        keywordEdit = view.findViewById(R.id.edit_keyword)
        blockListRecycler = view.findViewById(R.id.recycler_block_list)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
