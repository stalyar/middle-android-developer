package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

fun View.setMarginOptionally(left:Int = marginLeft, top : Int = marginTop, right : Int = marginRight, bottom : Int = marginBottom){

    val params:ViewGroup.MarginLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    this.requestLayout()
}

fun View.setPaddingOptionally(left:Int = paddingLeft, top : Int = paddingTop, right : Int = paddingRight, bottom : Int = paddingBottom){

    this.setPadding(left, top, right, bottom)
}