package com.hearing.scrolltextview.utils

import android.view.View

/**
 * @Author: 苍耳叔叔
 * @Date: 2021/8/11
 */
fun View.gone() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}
