package ru.skillbranch.skillarticles.extensions

import org.jetbrains.annotations.TestOnly
import java.util.*
import kotlin.collections.ArrayList

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true) : List<Int>{

    if (this==null || this.length<substr.length || substr=="") return listOf()

    var str = this
    var _substr = substr

    if (ignoreCase){
        str = str.toLowerCase(Locale.getDefault())
        _substr = substr.toLowerCase(Locale.getDefault())
    }

    val list : ArrayList<Int> = ArrayList()
    val length = substr.length
    var start = 0
    var end = length - 1
    var flag = true
    var substrMain:String
    while (flag){
        substrMain = str.substring(start..end)
        if (substrMain==_substr) {
            list.add(start)
            start += length
            end += length
        }
        else {
            start++
            end++
        }
        if (start>this.length-1 || end>this.length-1 ) flag = false
    }
    return list
}