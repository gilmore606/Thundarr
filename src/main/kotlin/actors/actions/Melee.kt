package actors.actions

import actors.Actor
import actors.animations.Whack
import render.sparks.Pow
import ui.panels.Console
import util.XY
import world.Level

class Melee(
    private val target: Actor,
    private val dir: XY
): Action(1.0f) {

    override fun execute(actor: Actor, level: Level) {
        actor.animation = Whack(dir)
        level.addSpark(Pow().at(actor.xy.x + dir.x, actor.xy.y + dir.y))
        Console.say("Whack!")
        target.receiveAttack(actor)
    }

}
