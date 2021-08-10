package com.hearing.scrolltextview.test

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hearing.scrolltextview.R
import kotlinx.android.synthetic.main.activity_test_text_view.scrollTextView

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class TestTextViewActivity : AppCompatActivity() {

    private var textSize = 20
    private var speed = 45

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_text_view)
        scrollTextView.isScrollRepeatable = false
        scrollTextView.setContentText(TEXT)
    }

    fun start(view: View) {
        scrollTextView.resume()
    }

    fun pause(view: View) {
        scrollTextView.pause()
    }

    fun large(view: View) {
        scrollTextView.setContentSize(++textSize)
    }

    fun small(view: View) {
        scrollTextView.setContentSize(--textSize)
    }

    fun fast(view: View) {
        scrollTextView.setSpeed((++speed).toFloat())
    }

    fun slow(view: View) {
        scrollTextView.setSpeed((--speed).toFloat())
    }

    companion object {
        private const val TEXT = "Android 图形系统是 Android 中一个非常重要的子系统，它涉及到许多相当复杂的模块，" +
                "如 SurfaceFlinger, Choreographer, HardWare Composer 等平时开发中基本上不会直接接触的概念。" +
                "前后基于 Android 10 版本陆陆续续阅读了图形系统各个组成模块的源码，结合网上的一些博客，" +
                "心中对 Android 的图形系统构成以及各个组件之间的工作流程有了一个整体的理解，这篇文章特意将整个图形系统做一个总结，" +
                "可能还有所疏漏，后续发现了再接着补全。"
    }
}