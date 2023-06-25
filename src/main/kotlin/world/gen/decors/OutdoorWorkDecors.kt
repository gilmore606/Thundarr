package world.gen.decors

import kotlinx.serialization.Serializable

@Serializable
class HuntingGround : Decor() {
    override fun description() = "The ground here is well-trodden."
    override fun announceJobMsg() = listOf(
        "Time to hunt.",
        "Let's see what the Lords of Light provide us today.",
        "Hunting time."
    ).random()

    override fun doFurnish() {

    }
}