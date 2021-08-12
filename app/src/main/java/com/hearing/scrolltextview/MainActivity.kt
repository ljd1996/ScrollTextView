package com.hearing.scrolltextview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hearing.scrolltextview.test.TestFancyActivity
import com.hearing.scrolltextview.test.TestRecyclerActivity
import com.hearing.scrolltextview.test.TestTextViewActivity
import com.hearing.scrolltextview.test.TestViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toRecycler(view: View) {
        startActivity(Intent(this, TestRecyclerActivity::class.java))
    }

    fun toView(view: View) {
        startActivity(Intent(this, TestViewActivity::class.java))
    }

    fun toTextView(view: View) {
        startActivity(Intent(this, TestTextViewActivity::class.java))
    }

    fun toFancyPanel(view: View) {
        startActivity(Intent(this, TestFancyActivity::class.java))
    }
}