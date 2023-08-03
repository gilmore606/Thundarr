package world.gen.decors

import actors.jobs.ForgeJob
import actors.jobs.GeneralStoreJob
import actors.jobs.TavernJob
import kotlinx.serialization.Serializable
import things.*
import util.Dice
import world.gen.spawnsets.GeneralStoreWares
import world.gen.spawnsets.SmithyWares
import world.gen.spawnsets.TavernWares
import world.terrains.Terrain

@Serializable
class BlacksmithShop : Decor() {
    var caseKey: Thing.Key? = null

    override fun description() = "A blacksmith's shop."
    override fun abandonedDescription() = "An abandoned blacksmith's shop."

    override fun doFurnish() {
        againstWall { spawn(Candlestick()) }
        againstWall {
            caseKey = spawn(
                StoreCabinet().withLoot(SmithyWares.set, Dice.range(4, 6), carto.threatLevel)
            )?.getKey()
        }
        forArea(x0+1, y0+1, x1-1, y1-1) { x,y ->
            setTerrain(x, y, Terrain.Type.TERRAIN_DIRT, roofed = true)
        }
        atCenter { spawn(Forge()) }
        repeat (Dice.range(1, 4)) {
            againstWall { spawn(Table()) }
        }
    }

    override fun job() = caseKey?.let { caseKey ->
        ForgeJob(room.rect, caseKey)
    } ?: super.job()
}

@Serializable
class GeneralStore : Decor() {
    var caseKey: Thing.Key? = null

    override fun description() = "A general supply shop."
    override fun abandonedDescription() = "An abandoned shop."

    override fun doFurnish() {
        againstWall { spawn(Candlestick()) }
        againstWall {
            caseKey = spawn(
                StoreCabinet().withLoot(GeneralStoreWares.set, Dice.range(4, 6), carto.threatLevel)
            )?.getKey()
        }
    }

    override fun job() = caseKey?.let { caseKey ->
        GeneralStoreJob(room.rect, caseKey)
    } ?: super.job()
}

@Serializable
class Tavern(val name: String) : Decor() {
    var caseKey: Thing.Key? = null

    override fun description() = "A roadside inn: \"$name\"."
    override fun abandonedDescription() = "An abandoned inn."
    override fun doFurnish() {
        againstWall { spawn(Candlestick())}
        againstWall { caseKey = spawn(
            StoreCabinet().withLoot(TavernWares.set, Dice.range(4, 6), carto.threatLevel)
        )?.getKey() }

        repeat (Dice.range(5, 8)) {
            awayFromWall {
                spawn(Table().also { if (Dice.chance(0.3f)) Candle().moveTo(it) })
                clearAround()
            }
        }
    }
    override fun job() = caseKey?.let { caseKey ->
        TavernJob(name, room.rect, caseKey)
    } ?: super.job()
}
