package com.hearing.scrolltextview.panel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.hearing.scrolltextview.R
import com.hearing.scrolltextview.utils.SizeUtil
import com.hearing.scrolltextview.utils.hide
import com.hearing.scrolltextview.utils.show
import kotlinx.android.synthetic.main.layout_multi_panel.view.bigPanel
import kotlinx.android.synthetic.main.layout_multi_panel.view.foldView
import kotlinx.android.synthetic.main.layout_multi_panel.view.littlePanel
import kotlinx.android.synthetic.main.layout_multi_panel.view.scrollTextView
import kotlinx.android.synthetic.main.layout_multi_panel.view.transView
import kotlin.math.sqrt

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/11
 * @Desc: 可移动，缩放，旋转的面板
 * todo 小面板模式下没做横竖屏旋转，如果要支持的话，需要重新计算一下对应 transX 和 transY 值
 */
class FancyPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val parentWidth: Float = 0f,
    private val parentHeight: Float = 0f,
    private val marginL: Float = SizeUtil.dp2px(context, 12f),
    private val marginT: Float = SizeUtil.dp2px(context, 12f),
    private val marginR: Float = SizeUtil.dp2px(context, 12f),
    private val marginB: Float = SizeUtil.dp2px(context, 12f)
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // 小面板的尺寸
    private val littlePanelSize = SizeUtil.dp2px(context, 45f)

    // 小面板吸附的边缘距离
    private val littlePanelPadding = SizeUtil.dp2px(context, 12f)

    // ---------------------------------- 平移相关参数 ---------------------------------

    // 用来判断是点击还是拖拽的最小距离
    private var moveDistance = 0f

    // 是否在拖动大面板
    private var isBigDrag = false

    // 是否在拖动小面板
    private var isLittleDrag = false

    // 上次触摸事件的坐标
    private var lastX = 0f
    private var lastY = 0f

    // 平移量
    private var tranX = 0f
    private var tranY = 0f

    // ---------------------------------- 缩放相关参数 ---------------------------------

    // 是否正在展开/收起面板
    private var isFolding = false

    // ---------------------------------- 旋转相关参数 ---------------------------------

    // 屏幕方向
    private var orientation = Surface.ROTATION_0

    // 横竖屏切换
    private val orientationListener: OrientationEventListener by lazy {
        object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                val cur = if (orientation >= 330 || orientation < 30) {
                    // 竖屏向下
                    Surface.ROTATION_0
                } else if (orientation in 60..119) {
                    // 横屏向右
                    Surface.ROTATION_90
                } else if (orientation in 150..209) {
                    // 竖屏向上
                    Surface.ROTATION_180
                } else if (orientation in 240..299) {
                    // 横屏向左
                    Surface.ROTATION_270
                } else {
                    Surface.ROTATION_0
                }
                if (!isFolding && inBigPanel() && this@FancyPanel.orientation != cur) {
                    this@FancyPanel.orientation = cur
                    pivotX = width() / 2f
                    pivotY = height() / 2f
                    rotation = when {
                        isPortrait() -> 0f
                        isLandLeft() -> 90f
                        else -> 270f
                    }
                    if (isPortrait()) {
                        foldView.show()
                    } else {
                        foldView.hide()
                    }
                    correctBigBorder()
                }
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_multi_panel, this, true)
        correctBigBorder()
        foldView.setOnClickListener {
            fold()
        }
    }

    /**
     * 将其显示在指定 [activity] 顶层
     */
    fun addToActivity(activity: Activity) {
        activity.addContentView(this, FrameLayout.LayoutParams(width().toInt(), height().toInt()))
    }

    /**
     * 设置滚动内容 [content]
     */
    fun setPanelContent(content: String) {
        scrollTextView.setContentText(content)
        scrollTextView.setTextColor(Color.WHITE)
        scrollTextView.highLightColor = Color.BLUE
        scrollTextView.isScrollRepeatable = true
        scrollTextView.resume(500)
    }

    /**
     * resume 时开启横竖屏监听
     */
    fun onResume() {
        orientationListener.enable()
    }

    /**
     * pause 时关闭横竖屏监听
     */
    fun onPause() {
        orientationListener.disable()
    }

    /**
     * 收起面板
     */
    private fun fold() {
        if (isFolding) {
            return
        }
        isFolding = true
        correctPivot(isBigPanelInLeft())
        val xScaleAnim = ObjectAnimator.ofFloat(bigPanel, "scaleX", littlePanelSize / width())
        val yScaleAnim = ObjectAnimator.ofFloat(bigPanel, "scaleY", littlePanelSize / height())
        val animSet = AnimatorSet()
        animSet.playTogether(xScaleAnim, yScaleAnim)
        animSet.duration = 500
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                adjustPanelAttach(isBigPanelInLeft())
                littlePanel.show()
                isFolding = false
            }
        })
        animSet.start()
    }

    /**
     * 展开面板
     */
    private fun unfold() {
        if (isFolding) {
            return
        }
        isFolding = true
        littlePanel.hide()
        correctPivot(isLittlePanelInLeft())
        val xScaleAnim = ObjectAnimator.ofFloat(bigPanel, "scaleX", 1f)
        val yScaleAnim = ObjectAnimator.ofFloat(bigPanel, "scaleY", 1f)
        val animSet = AnimatorSet()
        animSet.playTogether(xScaleAnim, yScaleAnim)
        animSet.duration = 500
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                correctBigBorder()
                isFolding = false
            }
        })
        animSet.start()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && (touchInTransView(ev) || touchInLittlePanel(ev))) {
            return onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // 触摸的不是拖动控件则直接返回
        if (event == null || (event.action == MotionEvent.ACTION_DOWN && !touchInTransView(event) && !touchInLittlePanel(event))) {
            return inBigPanel()
        }
        if (isBigDrag || touchInTransView(event)) {
            return onBigPanelTouchEvent(event)
        }
        if (isLittleDrag || touchInLittlePanel(event)) {
            return onLittlePanelTouchEvent(event)
        }
        return inBigPanel()
    }

    /**
     * 大面板滑动
     */
    private fun onBigPanelTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                isBigDrag = true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                when {
                    isPortrait() -> {
                        tranX += dx
                        tranY += dy
                    }
                    isLandLeft() -> {
                        tranX -= dy
                        tranY += dx
                    }
                    else -> {
                        tranX += dy
                        tranY -= dx
                    }
                }
                correctBigBorder()
            }
            MotionEvent.ACTION_UP -> {
                isBigDrag = false
            }
        }
        return true
    }

    /**
     * 小面板滑动
     */
    private fun onLittlePanelTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLittleDrag = true
                moveDistance = 0f
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                when {
                    isPortrait() -> {
                        tranX += dx
                        tranY += dy
                    }
                    isLandLeft() -> {
                        tranX -= dy
                        tranY += dx
                    }
                    else -> {
                        tranX += dy
                        tranY -= dx
                    }
                }
                correctLittleYBorder()
                translationX = tranX
                moveDistance += sqrt(dx * dx + dy * dy)
            }
            MotionEvent.ACTION_UP -> {
                isLittleDrag = false
                correctPivot(isLittlePanelInLeft())
                adjustPanelAttach(isLittlePanelInLeft())
                // click
                if (moveDistance <= SizeUtil.dp2px(context, 2f)) {
                    unfold()
                }
            }
        }
        return true
    }

    /**
     * 是否触摸移动 View
     */
    private fun touchInTransView(event: MotionEvent): Boolean {
        return inBigPanel() && event.x >= transView.x && event.x <= transView.x + transView.width &&
                event.y >= transView.y && event.y <= transView.y + transView.height
    }

    /**
     * 是否触摸小面板
     */
    private fun touchInLittlePanel(event: MotionEvent): Boolean {
        return inLittlePanel() && event.x >= littlePanel.x && event.x <= littlePanel.x + littlePanel.width &&
                event.y >= littlePanel.y && event.y <= littlePanel.y + littlePanel.height
    }


    /**
     * 纠正拖拽大面板的边界
     */
    private fun correctBigBorder() {
        val lMax = leftMax()
        if (tranX < lMax) {
            tranX = lMax
        }
        val rMax = rightMax()
        if (tranX > rMax) {
            tranX = rMax
        }
        val tMax = topMax()
        if (tranY < tMax) {
            tranY = tMax
        }
        val bMax = bottomMax()
        if (tranY > bMax) {
            tranY = bMax
        }
        translationX = tranX
        translationY = tranY
    }

    /**
     * 纠正拖拽小面板的边界
     */
    private fun correctLittleYBorder() {
        val tMax = littleTopMax()
        if (tranY < tMax) {
            tranY = tMax
        }
        val bMax = littleBottomMax()
        if (tranY > bMax) {
            tranY = bMax
        }
        translationY = tranY
    }

    /**
     * 纠正展开和收起大面板时缩放动画的中心
     */
    private fun correctPivot(inLeft: Boolean) {
        if (inLeft) {
            bigPanel.pivotX = 0f
            bigPanel.pivotY = 0f
        } else {
            bigPanel.pivotX = width()
            bigPanel.pivotY = 0f
        }
    }


    /**
     * 自适应小面板在大面板中的偏移
     */
    private fun adjustPanelAttach(inLeft: Boolean) {
        if (inLeft) {
            littlePanel.x = 0f
            littlePanel.y = 0f
            x = littlePanelPadding
        } else {
            littlePanel.x = width() - littlePanelSize
            littlePanel.y = 0f
            x = parentWidth - littlePanelPadding - width()
        }
        tranX = x - left
    }


    /**
     * 大面板是否位于父 View 左侧
     */
    private fun isBigPanelInLeft(): Boolean {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return when {
            isPortrait() -> location[0] < (parentWidth - width()) / 2
            isLandLeft() -> location[0] - height() < (parentWidth - height()) / 2
            else -> location[0] < (parentWidth - height()) / 2
        }
    }

    /**
     * 小面板是否位于父 View 左侧
     */
    private fun isLittlePanelInLeft(): Boolean {
        val location = IntArray(2)
        littlePanel.getLocationOnScreen(location)
        return if (isPortrait() || isLandRight()) {
            location[0] < (parentWidth - littlePanelSize) / 2
        } else {
            location[0] < parentWidth / 2
        }
    }


    /**
     * 大面板滑动的左部边界
     * [getTranslationX] = [getX] - [getLeft]
     */
    private fun leftMax() = if (isPortrait()) marginL - left else (height() - width()) / 2 + marginL - left

    /**
     * 大面板滑动的顶部边界
     * [getTranslationY] = [getY] - [getTop]
     */
    private fun topMax() = if (isPortrait()) marginT - top else marginT - top - (height() - width()) / 2

    /**
     * 大面板滑动的右部边界
     * [getTranslationX] = [getX] - [getLeft]
     */
    private fun rightMax() = maxWidth() - orientedWidth() + leftMax()

    /**
     * 大面板滑动的底部边界
     * [getTranslationY] = [getY] - [getTop]
     */
    private fun bottomMax(real: Boolean = true) = maxHeight() - orientedHeight() + topMax()

    /**
     * 小面板滑动的顶部边界
     */
    private fun littleTopMax(): Float {
        return if (isPortrait()) {
            marginT - top
        } else if (isLandLeft()) {
            if (littlePanel.translationX == 0f) {
                topMax()
            } else {
                topMax() - width() + littlePanelSize
            }
        } else {
            if (littlePanel.translationX == 0f) {
                topMax() - width() + littlePanelSize
            } else {
                topMax()
            }
        }
    }

    /**
     * 小面板滑动的底部边界
     */
    private fun littleBottomMax(): Float {
        return if (isPortrait()) {
            parentHeight - littlePanelSize - marginB
        } else if (isLandLeft()) {
            if (littlePanel.translationX == 0f) {
                bottomMax() + width() - littlePanelSize
            } else {
                bottomMax()
            }
        } else {
            if (littlePanel.translationX == 0f) {
                bottomMax()
            } else {
                bottomMax() + width() - littlePanelSize
            }
        }
    }


    private fun width() = parentWidth * 0.6f

    private fun height() = parentHeight * 0.4f

    /**
     * 区分横竖屏方向后的面板宽度
     */
    private fun orientedWidth() = if (isPortrait()) width() else height()

    /**
     * 区分横竖屏方向后的面板高度
     */
    private fun orientedHeight() = if (isPortrait()) height() else width()

    /**
     * 可展示区域的宽度
     */
    private fun maxWidth() = parentWidth - marginL - marginR

    /**
     * 可展示区域的高度
     */
    private fun maxHeight() = parentHeight - marginT - marginB

    /**
     * 小面板模式
     */
    private fun inLittlePanel() = littlePanel.isVisible

    /**
     * 大面板模式
     */
    private fun inBigPanel() = !inLittlePanel()

    /**
     * 是否横屏
     */
    private fun isLand(o: Int = orientation) = !isPortrait(o)

    /**
     * 是否向左侧横屏
     */
    private fun isLandLeft(o: Int = orientation) = o == Surface.ROTATION_270

    /**
     * 是否向右侧横屏
     */
    private fun isLandRight(o: Int = orientation) = o == Surface.ROTATION_90

    /**
     * 是否竖屏
     */
    private fun isPortrait(o: Int = orientation) = o == Surface.ROTATION_0 || o == Surface.ROTATION_180

    companion object {
        const val TAG = "FancyPanel"
    }
}