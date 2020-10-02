package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int,Int>>.groupByBounds(bounds : List<Pair<Int,Int>>) : List<List<Pair<Int,Int>>>{
    val result = listOf<List<Pair<Int,Int>>>(bounds)
    return result
}