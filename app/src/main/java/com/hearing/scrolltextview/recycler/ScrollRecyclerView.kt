package com.hearing.scrolltextview.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class ScrollRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        setLayoutManager(layoutManager)
        adapter = LinesAdapter()
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val first = layoutManager.findFirstVisibleItemPosition() + layoutManager.findFirstCompletelyVisibleItemPosition()
                getLinesAdapter()?.setHighLightLines(first / 2 + 1)
            }
        })
    }

    private fun getLinesAdapter(): LinesAdapter? {
        return adapter as? LinesAdapter
    }

    fun setData(data: List<LineWord>) {
        getLinesAdapter()?.setData(data)
    }

    fun setStrData(data: List<String>) {
        setData(data.map {
            LineWord(word = it, highLight = false)
        })
    }
}
