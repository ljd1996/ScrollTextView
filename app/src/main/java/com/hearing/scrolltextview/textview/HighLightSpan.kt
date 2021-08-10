package com.hearing.scrolltextview.textview

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ForegroundColorSpan

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
class HighLightSpan(private val typeface: Typeface, private val spacing: Float, color: Int) : ForegroundColorSpan(color) {
    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        textPaint.typeface = typeface
        textPaint.letterSpacing = spacing
    }
}
