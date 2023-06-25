package util

import kotlinx.serialization.Serializable

@Serializable
data class DayTime(
    val hour: Int,
    val min: Int
) {
    companion object {
        fun betweenHoursOf(h1: Int, h2: Int) = DayTime(Dice.range(h1, h2 - 1), Dice.range(0, 59))
    }

    override fun toString() = "$hour:$min"

    fun isBefore(dayTime: DayTime): Boolean {
        if (hour < dayTime.hour) return true
        if (hour > dayTime.hour) return false
        if (min < dayTime.min) return true
        return false
    }
    fun isAfter(dayTime: DayTime): Boolean {
        if (hour > dayTime.hour) return true
        if (hour < dayTime.hour) return false
        if (min > dayTime.min) return true
        return false
    }
}
