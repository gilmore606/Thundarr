package things

import util.XY
import world.Entity

interface Smashable {

    fun moveTo(holder: ThingHolder?)
    fun xy(): XY?
    val holder: ThingHolder?

    fun sturdiness() = 1f
    fun isSmashable() = true
    fun smashVerbName() = "smash"
    fun smashSuccessSelfMsg() = "You smash %dd to bits!"
    fun smashSuccessOtherMsg() = "%Dn smashes %dd to bits!"
    fun smashFailSelfMsg() = "You slam %dd, but it holds firm."
    fun smashFailOtherMsg() = "%Dn slams %dd but it holds firm."
    fun smashDebris(): Thing? = null
    fun onSmashFail() { }
    fun onSmashSuccess() {
        smashDebris()?.moveTo(holder)
        moveTo(null)
    }

}
