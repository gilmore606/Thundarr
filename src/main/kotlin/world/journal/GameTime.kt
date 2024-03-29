package world.journal

import kotlinx.serialization.Serializable
import util.DayTime

@Serializable
data class GameTime(
    val time: Double
) {
    companion object {
        const val TURNS_PER_DAY = 2500.0
        const val YEAR_ZERO = 2994

        val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        const val DAYS_PER_MONTH = 30
        const val DAYS_PER_YEAR = 360
    }

    val timeString: String
    val dateString: String
    val minute: Int
    val hour: Int
    val day: Int
    val month: Int
    val monthDay: Int
    val monthName: String
    val year: Int
    val date: Int
    init {
        day = (time / TURNS_PER_DAY).toInt()
        val timeOfDay = time - (day * TURNS_PER_DAY)
        val minutes = (timeOfDay / TURNS_PER_DAY) * 1440.0
        hour = (minutes / 60).toInt()
        minute = minutes.toInt() - (hour * 60)
        var ampm = "am"
        var amhour = hour
        if (hour >= 11) {
            ampm = "pm"
            if (hour >= 12) {
                amhour -= 12
            }
        }
        amhour += 1
        val minstr = if (minute < 10) "0$minute" else "$minute"
        val zyear = (day / DAYS_PER_YEAR)
        val yearDay = day - (zyear * DAYS_PER_YEAR)
        month = yearDay / DAYS_PER_MONTH
        monthDay = (yearDay - month * DAYS_PER_MONTH) + 1
        monthName = monthNames[month]
        year = YEAR_ZERO + zyear
        date = year * DAYS_PER_YEAR + (month * DAYS_PER_MONTH) + day

        timeString = "$amhour:$minstr $ampm"
        dateString = "$monthName $monthDay, $year"
    }

    fun isBefore(dayTime: DayTime): Boolean {
        if (hour < dayTime.hour) return true
        if (hour > dayTime.hour) return false
        if (minute < dayTime.min) return true
        return false
    }
    fun isAfter(dayTime: DayTime): Boolean {
        if (hour > dayTime.hour) return true
        if (hour < dayTime.hour) return false
        if (minute > dayTime.min) return true
        return false
    }

    fun dayTime() = DayTime(hour, minute)
}
