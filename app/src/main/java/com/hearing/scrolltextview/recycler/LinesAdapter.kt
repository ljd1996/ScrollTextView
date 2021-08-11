package com.hearing.scrolltextview.recycler

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hearing.scrolltextview.R

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class LinesAdapter : RecyclerView.Adapter<LinesHolder>() {
    private val data = mutableListOf<LineWord>()
    // todo
    private val highLightCount = 1
    private val highLightStart = 1

    fun setData(data: List<LineWord>) {
        this.data.clear()
        this.data.addAll(data)
        setupData()
        notifyDataSetChanged()
    }

    fun setHighLightLines(highLightLineStart: Int) {
        data.forEachIndexed { index, lines ->
            lines.highLight = isHighLightLine(index, highLightLineStart)
        }
        // todo 优化
        notifyDataSetChanged()
    }

    private fun isHighLightLine(index: Int, highLightLineStart: Int): Boolean {
        for (i in 0 until highLightCount) {
            if (highLightLineStart + i == index - highLightStart) {
                return true
            }
        }
        return false
    }

    private fun setupData() {
        data.forEachIndexed { index, lineWord ->
            lineWord.highLight = isHighLightLine(index, highLightStart)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_line_item, parent, false)
        return LinesHolder(view)
    }

    override fun onBindViewHolder(holder: LinesHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int = data.size
}

class LinesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val linesTV = itemView.findViewById<TextView>(R.id.line_tv)

    fun bindData(lineWord: LineWord) {
        linesTV.text = lineWord.word
        if (lineWord.highLight) {
            linesTV.setTextColor(Color.BLUE)
            linesTV.typeface = Typeface.DEFAULT_BOLD
        } else {
            linesTV.setTextColor(Color.BLACK)
            linesTV.typeface = Typeface.DEFAULT
        }
    }
}
