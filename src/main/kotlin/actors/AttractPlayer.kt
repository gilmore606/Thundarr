package actors

import actors.actions.*
import render.tilesets.Glyph
import things.Apple
import things.Pear
import util.DIRECTIONS
import util.Dice
import util.XY
import util.log
import kotlin.random.Random

class AttractPlayer : Player() {

    var lastActionMs = System.currentTimeMillis()

    override fun glyph() = Glyph.MOK

    override fun nextAction() = super.nextAction() ?: defaultAction()

    override fun hasActionJuice() = true

    override fun defaultAction(): Action? {
        if (System.currentTimeMillis() - lastActionMs > 400L) {
            lastActionMs = System.currentTimeMillis() - Random.nextLong(200L)

            level?.also { level ->
                // Pick up stuff
                val stuff = level.thingsAt(xy.x, xy.y)
                if (stuff.isNotEmpty()) {
                    stuff.forEach {
                        if (it.isPortable()) {
                            return Get(it)
                        }
                    }
                }

                // Yeet apples at herders
                if (Dice.chance(0.2f)) {
                    val seen = entitiesSeen { it is Herder }
                    seen.keys.forEach { target ->
                        target as Herder
                        doWeHave("apple")?.also {
                            return Throw(it, target.xy.x, target.xy.y)
                        }
                    }
                }

                // talk or fight or grab nearby stuff
                entitiesNextToUs().forEach { entity ->
                    if (entity is NPC && entity.isHostile()) {
                        return Melee(entity as Actor, XY(entity.xy()!!.x - xy.x, entity.xy()!!.y - xy.y))
                    }
                    if (entity is Ox || entity is MuskOx) {
                        if (Dice.flip()) return Converse(entity as Actor)
                    }
                    if (entity is Apple || entity is Pear) {
                        return Move(XY(entity.xy()!!.x - xy.x, entity.xy()!!.y - xy.y))
                    }
                }
            }
            return wander()

        }
        return null
    }

    val lastDirs = mutableListOf<XY>()

    fun wander(): Action? {
        if (Dice.chance(0.08f)) level?.setPov(xy.x, xy.y)
        val dirs = mutableListOf<XY>()
        level?.also { level ->
            lastDirs.forEach { dir ->
                if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y) && level.actorAt(xy.x + dir.x, xy.y + dir.y) == null) {
                    dirs.add(dir)
                }
            }
            if (lastDirs.isEmpty() || Dice.chance(0.2f)) {
                DIRECTIONS.forEach { dir ->
                    if (level.isWalkableAt(xy.x + dir.x, xy.y + dir.y) && level.actorAt(xy.x + dir.x, xy.y + dir.y) == null) {
                        dirs.add(dir)
                    }
                }
            }
        }
        if (dirs.isNotEmpty()) {
            val dir = dirs.random()
            lastDirs.add(dir)
            if (lastDirs.size > 3) lastDirs.removeFirst()
            return Move(dir)
        }
        return null
    }

}
