package ru.skillbranch.skillarticles.extensions

fun List<Pair<Int,Int>>.groupByBounds(bounds : List<Pair<Int,Int>>) : List<List<Pair<Int,Int>>>{
    //val boundsTest = listOf(Pair(0,10),Pair(10,20),Pair(20,30))
    //val listTest = listOf(Pair(2,3), Pair(5,12), Pair(13,19))
    //val resultTest = listOf(listOf(Pair(2,3),Pair(5,10)), listOf(Pair(10,12),Pair(13,19)), listOf())

    val size = bounds.size
    val result = ArrayList<List<Pair<Int,Int>>>()
    var range = 0..1
    for (i in 0 until size){

        val a = ArrayList<Pair<Int,Int>>()

        range = bounds[i].first..bounds[i].second
        this.forEach {
            if (it.first in range && it.second in range){ //range 0..10  it (2,5)
                a.add(Pair(it.first, it.second))
            }
            else if (it.first in range && it.second !in range){
                a.add(Pair(it.first, bounds[i].second))
            }
        }
        result.add(a)
    }
    return result
}