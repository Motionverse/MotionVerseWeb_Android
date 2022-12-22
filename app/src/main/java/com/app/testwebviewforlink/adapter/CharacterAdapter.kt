package com.app.testwebviewforlink.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.app.testwebviewforlink.R
import com.app.testwebviewforlink.http.CharacterBean
import com.app.testwebviewforlink.utils.show
import com.makeramen.roundedimageview.RoundedImageView

class CharacterAdapter(val context: Context, private var list: List<CharacterBean>) :
    BaseAdapter() {

    private var pos: Int = -1

    fun choiceItem(position: Int) {
        this.pos = position
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): CharacterBean {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val v: View
        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_character, null, false)
            viewHolder = ViewHolder(v)
            v.tag = viewHolder
        } else {
            v = convertView
            viewHolder = v.tag as ViewHolder
        }
        viewHolder.tv.text = list[position].name
        viewHolder.iv.show(list[position].img)
        if (position == pos) {
            viewHolder.iv.apply {
                borderColor = Color.parseColor("#66ffcc")
                borderWidth = 8f
            }
            viewHolder.tv.setTextColor(Color.parseColor("#66ffcc"))
        } else {
            viewHolder.iv.apply {
                borderWidth = 0f
            }
            viewHolder.tv.setTextColor(Color.BLACK)
        }

        return v
    }

    class ViewHolder(view: View) {
        var iv: RoundedImageView
        var tv: TextView

        init {
            iv = view.findViewById(R.id.ivItem)
            tv = view.findViewById(R.id.tvNameItem)
        }
    }
}