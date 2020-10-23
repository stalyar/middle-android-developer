package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup



fun View.setMarginOptionally(top: Int = 0, right: Int = 0, bottom: Int = 0, left: Int = 0) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    params.leftMargin = left
    params.rightMargin = right
    params.topMargin = top
    params.bottomMargin = bottom
    layoutParams = params
}

fun View.setPaddingOptionally(
    left: Int = paddingLeft,
    top: Int = paddingTop,
    right: Int = paddingRight,
    bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}