package me.sweetll.tucao.business.channel.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import me.sweetll.tucao.R
import me.sweetll.tucao.business.channel.model.ChannelFilter

class ChannelFilterAdapter(val context: Context, val data: List<ChannelFilter>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_channel_filter, parent, false)

        val item = data[position]
        view.findViewById<TextView>(R.id.text_title).text = item.title
        view.findViewById<TextView>(R.id.text_subtitle).text = item.subtitle

        return view
    }

    override fun getItem(position: Int): Any = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = data.size

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.simple_spinner_dropdown_item, parent, false)

        val item = data[position]
        view.findViewById<TextView>(android.R.id.text1).text = item.subtitle

        return view
    }
}