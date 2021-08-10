package com.hearing.scrolltextview.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hearing.scrolltextview.R
import com.hearing.scrolltextview.utils.SizeUtils
import kotlinx.android.synthetic.main.activity_test_view.scrollView

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class TestViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_view)
        testLinesView()
    }

    private fun testLinesView() {
        val singleWidth = SizeUtils.dp2px(this, 20f)
        val width = SizeUtils.dp2px(this, 320f)
        val count = (width / singleWidth).toInt()
        var start = -count
        var end = 0
        // todo 瞎处理文本分割
        scrollView.setData(ArrayList<String>().apply {
            while (end <= TEXT.length) {
                start += count
                end += count
                if (end > TEXT.length) {
                    end = TEXT.length
                    add(TEXT.substring(start, end))
                    break
                }
                add(TEXT.substring(start, end))
            }
        })
        scrollView.start()
    }

    companion object {
        private const val TEXT = "Android 图形系统是 Android 中一个非常this is a 重要的子系统，它涉及到许多相当复杂的模块，" +
                "如 SurfaceFlinger, Choreographer, HardWare Composer 等平时开发中基本上不会直接接触的概念。" +
                "前后基于 Android 10 版本陆陆续续阅读了图形系统各个组成模块的源码，结合网上的一些博客，" +
                "心中对 Android 的图形系统构成以及各个组件之间的工作流程有了一个整体的理解，这篇文章特意将整个图形系统做一个总结，" +
                "可能还有所疏漏，后续发现了再接着补全。"
    }
}