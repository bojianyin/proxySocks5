package com.xiaobo.proxysocks5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class LineAdapter(val dataList:List<Line>) : RecyclerView.Adapter<LineHolder>(){
    var listener: MyOnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_line,parent,false)
        return LineHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: LineHolder, position: Int) {
        holder.vLineName.text = dataList[position].name
        holder.vSpeed.text = dataList[position].speed?.let {
             "$`it`ms"
        } ?: "--"

        val drawable = ContextCompat.getDrawable(holder.itemView.context,if(dataList[position].isCheck) android.R.drawable.checkbox_on_background else android.R.drawable.checkbox_off_background)
        holder.vRadio.setImageDrawable(drawable)
        holder.itemView.setOnClickListener{
            listener?.click(position)
        }
    }

    interface MyOnClickListener{
        fun click(position:Int)
    }

}

class LineHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val vLineName: TextView by lazy { itemView.findViewById(R.id.tv_line_name)}
    val vSpeed: TextView by lazy { itemView.findViewById(R.id.tv_speed)}
    val vRadio: ImageView by lazy { itemView.findViewById(R.id.rb_item)}

}