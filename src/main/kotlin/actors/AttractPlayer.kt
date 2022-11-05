package actors

import actors.actions.*
import render.tilesets.Glyph
import things.*
import util.*
import world.terrains.BrickWall
import world.terrains.Terrain
import world.terrains.Wall
import kotlin.random.Random

class AttractPlayer : Player() {

    var lastActionMs = System.currentTimeMillis()

    var isMining = false
    var tunnelDir: XY? = null
    var noMiningUntil: Double = App.time


    override fun glyph() = Glyph.MOK

    override fun nextAction() = super.nextAction() ?: defaultAction()

    override fun hasActionJuice() = true

    override fun defaultAction(): Action? {
        if (System.currentTimeMillis() - lastActionMs > 350L) {
            lastActionMs = System.currentTimeMillis() - Random.nextLong(150L)

            level?.also { level ->
                val roofedHere = level.isRoofedAt(xy.x, xy.y)
                val light = level.lightAt(xy.x, xy.y)

                if (Dice.chance(0.2f)) {
                    if (light.brightness() < 0.1f) {
                        doWeHave("sunsword")?.also { return useThing(it, Thing.UseTag.SWITCH) }
                    }
                    if (light.brightness() == 1.0f && !roofedHere) {
                        doWeHave("sunsword")?.also { return useThing(it, Thing.UseTag.SWITCH) }
                    }
                }
                // Pick up stuff
                val stuff = level.thingsAt(xy.x, xy.y)
                if (stuff.isNotEmpty()) {
                    stuff.forEach {
                        if (it.isPortable()) {
                            if (it is Torch) {

                            } else if (it is Brick) {
                                if (roofedHere && Dice.chance(0.5f)) return Get(it)
                            } else return Get(it)
                        }
                    }
                }

                // Drop junk
                if (!roofedHere) {
                    if (Dice.chance(0.7f)) {
                        doWeHave("brick")?.also { brick ->
                            return Drop(brick, groundAtPlayer())
                        }
                    }
                } else {
                    doWeHave("torch")?.also { torch ->
                        if ((torch as Torch).active) {
                            return Drop(torch, groundAtPlayer())
                        } else if (light.r < 0.7f && Dice.chance(0.3f)) {
                            return useThing(torch, Thing.UseTag.SWITCH)
                        }
                    }
                }

                // Wield weapons
                doWeHave("pickaxe")?.also { pick ->
                    if (!(pick as Gear).equipped) {
                        return Equip(pick)
                    } else {
                        // Dig
                        val digDirs = mutableListOf<XY>()
                        var finishDig: Action? = null
                        forCardinals { tx, ty, dir ->
                            if (level.getTerrain(tx, ty) == Terrain.Type.TERRAIN_BRICKWALL) {
                                level.getTerrainData(tx, ty)?.also {
                                    if ((it as Wall.Data).damage > 0 && (App.time > noMiningUntil)) {
                                        finishDig = Bump(xy.x, xy.y, dir)
                                        willAggro = true
                                        isMining = true
                                    }
                                }
                                digDirs.add(dir)
                            }
                        }
                        finishDig?.also { return it }

                        if (digDirs.isNotEmpty() && Dice.chance(0.95f)) {
                            if (isMining || (App.time > noMiningUntil) && Dice.chance(0.2f)) {
                                isMining = true
                                willAggro = true
                                val dir = digDirs.random()
                                tunnelDir?.also { tunnelDir ->
                                    if (Dice.chance(0.1f)) this.tunnelDir = dir
                                    if (Dice.chance(0.93f) && tunnelDir in digDirs) {
                                        return Bump(xy.x, xy.y, tunnelDir)
                                    } else if (canStep(tunnelDir)) {
                                        return Move(tunnelDir)
                                    } else {
                                        stopMining()
                                        return wander()
                                    }
                                } ?: run {
                                    if (Dice.chance(0.08f)) {
                                        log.info("start tunnel")
                                        tunnelDir = dir
                                    }
                                }

                                if (Dice.chance(0.002f)) {
                                    stopMining()
                                }
                                return Bump(xy.x, xy.y, digDirs.random())
                            }
                        }
                    }
                } ?: doWeHave { it is Weapon }?.also { weapon ->
                    if (!(weapon as Gear).equipped) {
                        return Equip(weapon)
                    }
                }


                // Yeet apples at herders
                if (Dice.chance(0.2f)) {
                    val seen = entitiesSeen { it is Herder }
                    doWeHave("apple")?.also {
                        if (seen.isNotEmpty()) {
                            val target = seen.keys.random() as Herder
                            if (distanceTo(target) in 2f .. 8f) {
                                return Throw(it, target.xy.x, target.xy.y)
                            }
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
                    if (entity is Consumable || entity is Weapon) {
                        return Move(XY(entity.xy()!!.x - xy.x, entity.xy()!!.y - xy.y))
                    }
                }
            }
            return wander()

        }
        return null
    }

    private fun stopMining() {
        isMining = false
        willAggro = false
        noMiningUntil = App.time + Dice.range(10, 500)
        tunnelDir = null
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
