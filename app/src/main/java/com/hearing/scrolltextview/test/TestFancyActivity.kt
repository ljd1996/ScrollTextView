package com.hearing.scrolltextview.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hearing.scrolltextview.R
import com.hearing.scrolltextview.panel.FancyPanel
import com.hearing.scrolltextview.panel.FancyPanel.Companion.TAG
import kotlinx.android.synthetic.main.activity_test_fancy.container

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/11
 */
class TestFancyActivity : AppCompatActivity() {
    private var fancyPanel: FancyPanel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_fancy)
        container.setOnClickListener {
            Log.i(TAG, "click container")
        }
        container.post {
            fancyPanel = FancyPanel(
                context = this,
                parentWidth = container.width.toFloat(),
                parentHeight = container.height.toFloat()
            )
            fancyPanel?.addToActivity(this)
            fancyPanel?.setPanelContent(TEXT)
            fancyPanel?.onResume()
        }
        container.setOnClickListener {
            Toast.makeText(this, "Click container!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        fancyPanel?.onResume()
        super.onResume()
    }

    override fun onPause() {
        fancyPanel?.onPause()
        super.onPause()
    }

    companion object {
        private const val TEXT = "划一根火柴\n" +
                "将慵倦的夜点亮\n" +
                "吐出一缕烟\n" +
                "飘向半掩的窗\n" +
                "你纵身跃入酒杯\n" +
                "梦从此溺亡\n" +
                "心门上一把锁\n" +
                "钥匙在你手上\n" +
                "快将尘埃掸落\n" +
                "别将你眼眸弄脏\n" +
                "或许吧\n" +
                "谈笑中你早已淡忘\n" +
                "而我在颠沛中\n" +
                "已饱经一脸沧桑\n" +
                "思念需要时间\n" +
                "慢慢调养"
    }
}