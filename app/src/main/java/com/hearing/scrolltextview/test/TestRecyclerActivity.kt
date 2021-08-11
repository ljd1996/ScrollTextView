package com.hearing.scrolltextview.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hearing.scrolltextview.R
import kotlinx.android.synthetic.main.activity_test_recycler.scrollRecyclerView

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class TestRecyclerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recycler)
        scrollRecyclerView.setStrData(TEXT.split("\n"))
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