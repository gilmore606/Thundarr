package things

import world.Level

// An entity that can listen to world time updates.
interface Temporal {

    fun advanceTime(delta: Float) {

    }

}
