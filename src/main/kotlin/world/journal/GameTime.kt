package world.journal

import kotlinx.serialization.Serializable

@Serializable
data class GameTime(
    val time: Double
) {
    companion object {
        const val TURNS_PER_DAY = 3000.0
        const val YEAR_ZERO = 2994
    }

    private val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val timeString: String
    val dateString: String
    val minute: Int
    val hour: Int
    val day: Int
    val month: Int
    val monthDay: Int
    val monthName: String
    val year: Int
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
        val zyear = (day / 360)
        val yearDay = day - (zyear * 360)
        month = yearDay / 30
        monthDay = (yearDay - month * 30) + 1
        monthName = monthNames[month]
        year = YEAR_ZERO + zyear

        timeString = "$amhour:$minstr $ampm"
        dateString = "$monthName $monthDay, $year"
    }

    fun isBefore(iHour: Int, iMinute: Int): Boolean {
        if (hour < iHour) return true
        if (hour > iHour) return false
        if (minute < iMinute) return true
        return false
    }
    fun isAfter(iHour: Int, iMinute: Int): Boolean {
        if (hour > iHour) return true
        if (hour < iHour) return false
        if (minute > iMinute) return true
        return false
    }
}
