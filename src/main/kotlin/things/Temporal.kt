package things

// An entity that can listen to world time updates.
interface Temporal {
    fun temporalDone() = false
    fun advanceTime(delta: Float)
}
