package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

const val SECOND = 1000L
const val MINUTE = 60* SECOND
const val HOUR = 60 *MINUTE
const val DAY = 24 * HOUR

fun Date.shortFormat() : String{
    val pattern = if (this.isSameDay(Date())) "HH:mm" else "dd.MM.yy"
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.isSameDay(date: Date): Boolean{
    val day1 = this.time/ DAY
    val day2 = date.time/ DAY
    return day1 == day2
}

fun Date.format(pattern : String = "HH:mm:ss dd.MM.yy") : String{
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.add(value:Int, units: TimeUnits=TimeUnits.SECOND) : Date{
    var time = this.time

    time += when (units){
        TimeUnits.SECOND -> value* SECOND
        TimeUnits.MINUTE -> value* MINUTE
        TimeUnits.HOUR -> value* HOUR
        TimeUnits.DAY -> value* DAY
    }

    this.time = time

    return this
}

fun Date.humanizeDiff(date:Date = Date()): String {

    val diff:Long = (this.time - date.time)/1000L

    val mes = when (diff){
        in -1..1 -> "только что"
        in -45..-1 -> "несколько секунд назад"
        in 1..45 -> "через несколько секунд"
        in -75..-45 -> "минуту назад"
        in 45..75 -> "через минуту"
        in -45*60..-75 -> {
            val num = (diff/60).absoluteValue
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L) "$num минуту назад"
            else if (lastNum in 2..4 && num !in 12..14) "$num минуты назад"
            else "$num минут назад"
        }
        in 75..45*60 -> {
            val num = diff/60
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L) "через $num минуту"
            else if (lastNum in 2..4 && num !in 12..14) "через $num минуты"
            else "через $num минут"
        }
        in -75*60..-45*60 -> "час назад"
        in 45*60..75*60 -> "через час"
        in -22*3600..-75*60 -> {
            val num = (diff/3600).absoluteValue
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L) "$num час назад"
            else if (lastNum in 2..4 && num !in 12..14) "$num часа назад"
            else "$num часов назад"
        }
        in 75*60..22*3600 -> {
            val num = diff/3600
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L) "через $num час"
            else if (lastNum in 2..4 && num !in 12..14) "через $num часа"
            else "через $num часов"
        }
        in -26*3600..-22*3600 -> "день назад"
        in 22*3600..26*3600 -> "через день"
        in -360*3600*24..-26*3600 -> {
            val num = (diff/3600/24).absoluteValue
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L && num !=111L && num !=211L) "$num день назад"
            else if (lastNum in 2..4 && num !in 12..14 && num !in 112..114 && num !in 212..214) "$num дня назад"
            else "$num дней назад"
        }
        in 26*3600..360*3600*24 -> {
            val num = (diff/3600/24)
            val lastNum = num.toString().last().toString().toInt()
            if (lastNum == 1 && num !=11L && num !=111L && num !=211L) "через $num день"
            else if (lastNum in 2..4 && num !in 12..14 && num !in 112..114 && num !in 212..214) "через $num дня"
            else "через $num дней"
        }
        else -> if (diff>360*3600*24) "более чем через год"
                else "более года назад"
    }
/*0с - 1с "только что"
1с - 45с "несколько секунд назад"
45с - 75с "минуту назад"
75с - 45мин "N минут назад"
45мин - 75мин "час назад"
75мин 22ч "N часов назад"
22ч - 26ч "день назад"
26ч - 360д "N дней назад"
>360д "более года назад"*/

return mes
}

enum class TimeUnits{
    SECOND{
        override fun plural(value:Int):String{
            val lastNum = value.toString().last().toString().toInt()
            if (lastNum == 1 && value !=11) return "$value секунду"
            else if (lastNum in 2..4 && value !in 12..14) return "$value секунды"
            else return "$value секунд"
        }
    },
    MINUTE{
        override fun plural(value:Int):String{
            val lastNum = value.toString().last().toString().toInt()
            if (lastNum == 1 && value !=11) return "$value минуту"
            else if (lastNum in 2..4 && value !in 12..14) return "$value минуты"
            else return "$value минут"
        }
    },
    HOUR{
        override fun plural(value:Int):String{
            val lastNum = value.toString().last().toString().toInt()
            if (lastNum == 1 && value !=11) return "$value час"
            else if (lastNum in 2..4 && value !in 12..14) return "$value часа"
            else return "$value часов"
        }
    },
    DAY{
        override fun plural(value:Int):String{
            val lastNum = value.toString().last().toString().toInt()
            if (lastNum == 1 && value !=11 && value != 111 && value != 211) return "$value день"
            else if (lastNum in 2..4 && value !in 12..14 && value !in 112..114 && value !in 212..214) return "$value дня"
            else return "$value дней"
        }
    };
    abstract fun plural(value:Int):String
}
