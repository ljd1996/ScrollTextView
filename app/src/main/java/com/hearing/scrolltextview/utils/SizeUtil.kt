package com.hearing.scrolltextview.utils

import android.content.Context
import android.util.TypedValue

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/10
 */
object SizeUtil {
    fun dp2px(context: Context, dpVal: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal, context.resources.displayMetrics
        )
    }

    fun sp2px(context: Context, spVal: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            spVal, context.resources.displayMetrics
        )
    }
}