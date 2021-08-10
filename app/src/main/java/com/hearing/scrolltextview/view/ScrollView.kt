package com.hearing.scrolltextview.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.hearing.scrolltextview.utils.SizeUtils

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 * @Desc: 本来考虑用 Lines(var word: String = "", var highLight: Boolean = false) 来表示一行文字，
 * 但是发现没有必要，每次都需要遍历列表来修改 highLight 标志位，浪费性能，改成使用一个变量来表示当前高亮行即可。
 * todo: 逻辑不完善，这里如计算行高等逻辑都没写，另外有些逻辑应该是可以简化的~选择了自定义 TextView 方案，懒得改这里的代码了。
 */
class ScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ------------------------------------ 绘制相关 ------------------------------------

    private var offset: Float = 0f
    private var currentLine = 0
    private var isUserScroll = false
    private var lineSpace = SizeUtils.dp2px(context, 15f)   // 向下行距，单位 dp
    var highLightCount = 2  // 默认高亮 2 行
    var isSmoothness = true // 默认平滑滚动

    @ColorInt
    private var normalTextColor = Color.GRAY  // 正常颜色
    private var normalTextSize = SizeUtils.sp2px(context, 20f) // 正常字体，单位 sp

    @ColorInt
    private var highLightTextColor = Color.RED  // 高亮颜色
    private var highLightTextSize = SizeUtils.sp2px(context, 20f) // 高亮字体，单位 sp

    private val textPaint = TextPaint()

    // ------------------------------------ 手动滑动逻辑相关 ------------------------------------

    private var lastMotionY = 0f

    // ------------------------------------ 监听器 ------------------------------------

    private var onLinesViewListener: OnLinesViewListener? = null

    // ------------------------------------ 数据源 ------------------------------------

    private val linesData = mutableListOf<String>()

    // ------------------------------------ 滚动 ------------------------------------

    var repeatable: Boolean = false // 滚动到最后一行后是否回到第一行重新滚动

    var speedLine: Long = 800 // 按行滚动速度，单位 ms/行
    var speedSmooth: Float = SizeUtils.dp2px(context, 45f) // 按像素滚动速度，单位 dp/s

    /**
     * 逐行滚动
     */
    private val scrollByLineRunnable = object : Runnable {
        override fun run() {
            currentLine++
            if (currentLine >= linesData.size) {
                if (repeatable) {
                    scrollToLine(0)
                } else {
                    pauseByLine()
                    return
                }
            } else {
                scrollToLine(currentLine)
            }
            if (isUserScroll) {
                pauseByLine()
            } else {
                ViewCompat.postOnAnimationDelayed(this@ScrollView, this, speedLine)
            }
        }
    }

    /**
     * 平滑滚动
     */
    private val scrollBySmoothRunnable = object : Runnable {
        override fun run() {
            offset += speedSmooth / FREQUENCY
            // 如果滚到了最后
            if (offset > getLineOffsetY(linesData.size - 1)) {
                // 是否循环滚动
                if (repeatable) {
                    offset = 0f
                } else {
                    pauseBySmooth()
                    return
                }
            }
            // 计算当前高亮起始行
            // todo
            currentLine = (offset / (getTextHeight(0) + lineSpace)).toInt()
            invalidateView()
            if (isUserScroll) {
                // 如果在手动滚动则移除自动滚动任务
                pauseBySmooth()
            } else {
                // 发起下次任务
                ViewCompat.postOnAnimationDelayed(this@ScrollView, this, (1000 / FREQUENCY).toLong())
            }
        }
    }

    init {
        textPaint.isAntiAlias = true
        textPaint.textAlign = Paint.Align.LEFT
    }

    // ------------------------------------ setter ------------------------------------

    fun setScrollListener(listener: OnLinesViewListener) {
        onLinesViewListener = listener
    }

    /**
     * 单位 dp
     */
    fun setLineSpace(space: Int) {
        lineSpace = SizeUtils.dp2px(context, space.toFloat())
    }

    /**
     * 设置字体和颜色，单位 sp & 0xFFFFFFFF
     */
    fun setColorAndTextSize(
        @ColorInt normalColor: Int, @ColorInt highLightColor: Int,
        normalSize: Int, highLightSize: Int
    ) {
        this.normalTextColor = normalColor
        this.highLightTextColor = highLightColor
        this.normalTextSize = SizeUtils.sp2px(context, normalSize.toFloat())
        this.highLightTextSize = SizeUtils.sp2px(context, highLightSize.toFloat())
    }

    // ------------------------------------ public ------------------------------------

    fun start() {
        if (isSmoothness) {
            startBySmooth()
        } else {
            startByLine()
        }
    }

    fun pause() {
        if (isSmoothness) {
            pauseBySmooth()
        } else {
            pauseByLine()
        }
    }

    fun setData(data: List<String>) {
        linesData.clear()
        linesData.addAll(data)
    }

    // todo measure and layout?

    // todo 优化 看不见的部分不绘制
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        var y: Float = paddingTop.toFloat() + marginTop
        val x: Float = paddingLeft.toFloat() + marginLeft
        for (i in 0 until linesData.size) {
            y += getTextHeight(i) + lineSpace
            // 当前是否为高亮行，从而设置不同的画笔样式
            setPaintStyle(textPaint, isHighLightLine(i))
            canvas?.drawText(linesData[i], x, y - offset, textPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (linesData.isEmpty()) {
            return super.onTouchEvent(event)
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                pause()
                lastMotionY = event.y
                isUserScroll = true
            }
            MotionEvent.ACTION_MOVE -> {
                offset -= (event.y - lastMotionY)
                lastMotionY = event.y
                currentLine = (offset / (getTextHeight(0) + lineSpace)).toInt()
                invalidateView()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isUserScroll = false
                start()
                if (offset < 0) {
                    currentLine = 0
                    scrollToLine(0)
                }
                if (offset > getLineOffsetY(linesData.size - 1)) {
                    currentLine = linesData.size - 1
                    scrollToLine(linesData.size - 1)
                }
            }
        }
        return true
    }

    // ------------------------------------ private ------------------------------------

    /**
     * [line] 是否是高亮行
     */
    private fun isHighLightLine(line: Int): Boolean {
        for (i in 0 until highLightCount) {
            if (currentLine + i == line) {
                return true
            }
        }
        return false
    }

    /**
     * 设置画笔字体
     */
    private fun setPaintStyle(paint: Paint, highLight: Boolean) {
        if (highLight) {
            paint.textSize = highLightTextSize
            paint.color = highLightTextColor
            paint.isFakeBoldText = true
        } else {
            paint.textSize = normalTextSize
            paint.color = normalTextColor
            paint.isFakeBoldText = false
        }
    }

    /**
     * 获取第 linePosition 行文字顶部距离第一行顶部的偏移量
     */
    private fun getLineOffsetY(linePosition: Int): Float {
        // todo check padding and margin, maybe no need
        var y = 0f
        for (i in 0 until linePosition) {
            if (linesData.size > i) {
                y += getTextHeight(i) + lineSpace
            } else {
                break
            }
        }
        return y
    }

    /**
     * 获取第 linePosition 行文字的高度
     */
    private fun getTextHeight(linePosition: Int): Float {
        linesData.getOrNull(linePosition)?.let { lines ->
            val paint = Paint(textPaint)
            // todo 不同行设置对应的 Paint 样式，计算准确高度
            setPaintStyle(paint, false)
            val rect = Rect()
            paint.getTextBounds(lines, 0, lines.length, rect)
            return rect.height().toFloat()
        }
        return 0f
    }

    /**
     * 按行滚动，滚动到第 linePosition 行
     */
    private fun scrollToLine(linePosition: Int) {
        val scrollY = getLineOffsetY(linePosition)
        val animator = ValueAnimator.ofFloat(offset, scrollY)
        animator.addUpdateListener { animation ->
            offset = animation.animatedValue as Float
            invalidateView()
        }
        animator.duration = 300
        animator.start()
    }

    // todo 逐行&滚动逻辑一起
    private fun startBySmooth() {
        // todo 开始滚动
        if (linesData.isEmpty()) {
            return
        }
        ViewCompat.postOnAnimation(this, scrollBySmoothRunnable)
    }

    private fun pauseBySmooth() {
        removeCallbacks(scrollBySmoothRunnable)
    }

    /**
     * 开始按行自动滚动
     */
    private fun startByLine() {
        if (linesData.isEmpty()) {
            return
        }
        ViewCompat.postOnAnimation(this, scrollByLineRunnable)
    }

    private fun pauseByLine() {
        removeCallbacks(scrollByLineRunnable)
    }

    private fun invalidateView() {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    interface OnLinesViewListener {
        fun onScrollStarted()
        fun onScrollFinished()
        fun onScrollEnd()
    }

    companion object {
        private const val TAG = "ScrollView"
        private const val FREQUENCY = 60f // 平滑滚动的刷新频率，16ms刷新一次
    }
}