package com.hearing.scrolltextview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hearing.scrolltextview.test.TestTextViewActivity
import com.hearing.scrolltextview.test.TestViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toView(view: View) {
        startActivity(Intent(this, TestViewActivity::class.java))
    }

    fun toTextView(view: View) {
        startActivity(Intent(this, TestTextViewActivity::class.java))
    }
}