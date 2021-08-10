package com.hearing.scrolltextview.textview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import com.hearing.scrolltextview.utils.SizeUtils

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 * @Desc: 利用 TextView 能自适应文本换行，
 * 不支持高亮部分单独一个字体大小，因为字体大小一变行数也会变化，那么 SpannableStringBuilder 就会失效
 * 字体单位也使用 dp
 */
class ScrollTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr), Runnable {

    // ------------------------------- 参数设置 -------------------------------
    var isScrollRepeatable = false   // 是否可循环滚动
    var highLightColor = Color.RED  // 高亮颜色
        set(value) {
            field = value
            setupHighLightLines(true)
        }

    // ------------------------------- 状态获取 -------------------------------
    var curHighLightLine = 0    // 当前高亮行
        private set
    var curHighLightIndex = 0   // 当前高亮下标
        private set

    // ------------------------------- 内部参数 -------------------------------
    private var scrollSpeed = SizeUtils.dp2px(context, 45f) / FREQUENCY // speed default 45dp/s
    private var refreshRate = (1000 / FREQUENCY).toLong() // 默认 16ms 刷新一次
    private val textLineSpace = SizeUtils.dp2px(context, 10f) // 行间距
    private val defaultTextSize = SizeUtils.dp2px(context, 20f) // 默认字体大小
    private var isUserScroll = false    // 是否在手动滚动
    private var isAutoScroll = false    // 是否在自动滚动
    private var offset = minTopOffset() // 偏移量
    private var lastY = 0f  // 上次手动滚动的 y 轴坐标

    init {
        letterSpacing = BIG_LETTER_SPACING
        setLineSpacing(textLineSpace + defaultTextSize, 0f)
        setTextColor(DEFAULT_TEXT_COLOR)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
    }

    // ------------------------------- public -------------------------------

    /**
     * 开始自动滚动
     */
    fun resume(delay: Long = refreshRate) {
        isAutoScroll = true
        pauseOnly()
        postDelayed(this, delay)
    }

    /**
     * 停止自动滚动
     */
    fun pause() {
        isAutoScroll = false
        removeCallbacks(this)
    }

    /**
     * 不要使用 setText 设置文本
     */
    @SuppressLint("SetTextI18n")
    fun setContentText(text: String, highLightIndex: Int = 0, highLightCallback: (() -> Unit)? = null) {
        setText(text)
        post {
            highLightToIndex(highLightIndex)
            highLightCallback?.invoke()
        }
    }

    /**
     * 不要使用 setTextSize 设置字体大小。单位 [size] dp
     */
    fun setContentSize(size: Int) {
        val pxSize = SizeUtils.dp2px(context, size.toFloat())
        setLineSpacing(textLineSpace + pxSize, 0f)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, pxSize)
        post {
            highLightToIndex(curHighLightIndex)
        }
    }

    /**
     * 设置滚动速度，单位 [speed] dp/s
     */
    fun setSpeed(speed: Float) {
        this.scrollSpeed = SizeUtils.dp2px(context, speed) / FREQUENCY
        if (this.scrollSpeed < 1) {
            this.refreshRate = (1000 / (0.5 * FREQUENCY * this.scrollSpeed)).toLong()
            this.scrollSpeed = 1f
        } else {
            this.refreshRate = (1000 / FREQUENCY).toLong() // 默认 16ms 刷新一次
        }
        Log.i(TAG, "speed: ${this.scrollSpeed}, refreshRate: ${this.refreshRate}")
    }

    /**
     * 定位到指定 index 下标高亮
     */
    fun highLightToIndex(index: Int) {
        val layout = layout
        if (layout == null || index <= 0 || index >= text?.length ?: 0) {
            scrollTo(minTopOffset())
            return
        }
        for (i in 0 until lineCount) {
            val start = layout.getLineStart(i)
            val end = layout.getLineEnd(i)
            if (index in start until end) {
                scrollTo(lineHeight * (i - 1))
                return
            }
        }
        scrollTo(minTopOffset())
    }

    /**
     * 根据当前高亮位置 [curHighLightIndex] 纠正高亮行
     */
    fun correctHighLight() {
        highLightToIndex(curHighLightIndex)
    }

    override fun run() {
        if (!isUserScroll && scroll()) {
            resume()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (text.isNullOrEmpty()) {
            return super.onTouchEvent(event)
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isUserScroll = true
                lastY = event.y
                pauseOnly()
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = event.y - lastY
                lastY = event.y
                if (!scroll(-dy.toInt())) {
                    // 超过边界了
                    scrollBy(0, -dy.toInt())
                    offset -= dy.toInt()
                }
            }
            MotionEvent.ACTION_UP -> {
                isUserScroll = false
                if (correctOffset()) {
                    // 超过边界了，则滚回去
                    scrollTo(offset)
                }
                if (isAutoScroll) {
                    resume(400)
                }
            }
        }
        return true
    }

    // ------------------------------- private -------------------------------

    /**
     * 内部调用，仅停止自动滚动，不设置 [isAutoScroll] 标记为 false
     */
    private fun pauseOnly() {
        removeCallbacks(this)
    }

    /**
     * 滚动到指定偏移 [offsetY] 位置，并高亮行
     */
    private fun scrollTo(offsetY: Int) {
        offset = offsetY
        scrollTo(0, offset)
        setupHighLightLines(true)
    }

    /**
     * 滚动 [speed] 偏移量，并执行高亮逻辑，如已到底则根据 [isScrollRepeatable] 判断是否循环
     */
    private fun scroll(speed: Int = if (scrollSpeed >= 1) scrollSpeed.toInt() else 1): Boolean {
        if (offset >= maxBottomOffset()) {
            if (isScrollRepeatable) {
                if (!isUserScroll) {
                    postDelayed({
                        scrollTo(minTopOffset())
                        resume()
                    }, 1000)
                }
                return false
            }
            isAutoScroll = false
            return false
        } else {
            offset += speed
            scrollBy(0, speed)
            setupHighLightLines()
            return true
        }
    }

    /**
     * 高亮逻辑，[force] 是否强制执行高亮逻辑，否则如当前高亮行未变化则跳过高亮逻辑
     */
    private fun setupHighLightLines(force: Boolean = false) {
        val line = shouldHighLightLine()
        val layout = layout
        if (layout == null || (!force && line == curHighLightLine)) {
            return
        }
        curHighLightLine = line
        if (curHighLightLine < 0) {
            curHighLightLine = 0
            curHighLightIndex = 0
        }
        curHighLightIndex = layout.getLineStart(curHighLightLine)
        val start = layout.getLineStart(curHighLightLine)
        val end = if (curHighLightLine + HIGH_LIGHT_LINE_COUNT > lineCount) {
            layout.getLineStart(lineCount)
        } else {
            layout.getLineStart(curHighLightLine + HIGH_LIGHT_LINE_COUNT)
        }
        val colorText = SpannableStringBuilder(text.toString())
        // 避免粗体导致自动换行
        colorText.setSpan(
            HighLightSpan(Typeface.DEFAULT_BOLD, SMALL_LETTER_SPACING, highLightColor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text = colorText
    }

    /**
     * 根据 [offset] 偏移量计算当前应高亮的行
     */
    private fun shouldHighLightLine(): Int {
        return if (offset < 0) {
            offset / lineHeight
        } else {
            offset / lineHeight + HIGH_LIGHT_LINE_START
        }
    }

    /**
     * 滚动出限制区域后纠正 [offset]
     */
    private fun correctOffset(): Boolean {
        val top = minTopOffset()
        val bottom = maxBottomOffset()
        if (offset < top) {
            offset = top
            return true
        }
        if (offset > bottom) {
            offset = bottom
            return true
        }
        return false
    }

    /**
     * 最顶部的 [offset] 偏移量。默认是第 [HIGH_LIGHT_LINE_START] 行的位置
     */
    private fun minTopOffset(): Int {
        return -lineHeight * HIGH_LIGHT_LINE_START
    }

    /**
     * 最底部的 [offset] 偏移量。默认是第倒数 [HIGH_LIGHT_LINE_START] + 1 行的位置
     */
    private fun maxBottomOffset(): Int {
        val totalHeight = lineCount * lineHeight
        return totalHeight - lineHeight * (HIGH_LIGHT_LINE_START + 1)
    }

    companion object {
        private const val TAG = "ScrollTextView"
        private const val DEFAULT_TEXT_COLOR = Color.GRAY
        private const val FREQUENCY = 60
        private const val HIGH_LIGHT_LINE_START = 1 // 高亮起始行
        private const val HIGH_LIGHT_LINE_COUNT = 2 // 高亮行数
        private const val BIG_LETTER_SPACING = 0.05f // 默认字间距
        private const val SMALL_LETTER_SPACING = 0.03f // 高亮行的字间距，避免粗体导致自动换行
    }
}
