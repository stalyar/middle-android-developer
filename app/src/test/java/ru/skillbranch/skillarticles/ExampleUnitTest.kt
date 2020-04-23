package ru.skillbranch.skillarticles

import org.junit.Test

import org.junit.Assert.*
import ru.skillbranch.skillarticles.extensions.indexesOf

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_indexesOf() {
        println("AbrtARTsdfgRTrt".indexesOf("rT"))
        println("AbrtARTsdfgRTrt".indexesOf("RT"))
        println("AbrtARTsdfgRTrt".indexesOf("Rt"))
        println("AbrtARTsdfgRTrt".indexesOf("RT",false))
        println("AbrtARTsdfgRTrt".indexesOf("rt",false))
        println("AbrtARTsdfgRTrt".indexesOf("Rt",false))
    }


}
