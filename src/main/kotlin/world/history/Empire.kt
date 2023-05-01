package world.history

import App
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import util.Dice
import util.Madlib
import world.gen.Metamap
import world.gen.features.Stronghold

@Serializable
open class Empire(
    val id: Int,
    val foundingYear: Int,
    var mapColor: Glyph = listOf(Glyph.MAP_COLOR_0, Glyph.MAP_COLOR_1, Glyph.MAP_COLOR_2, Glyph.MAP_COLOR_3,
        Glyph.MAP_COLOR_4, Glyph.MAP_COLOR_5, Glyph.MAP_COLOR_6, Glyph.MAP_COLOR_7, Glyph.MAP_COLOR_8,
        Glyph.MAP_COLOR_9, Glyph.MAP_COLOR_10, Glyph.MAP_COLOR_11, Glyph.MAP_COLOR_12, Glyph.MAP_COLOR_13,
        Glyph.MAP_COLOR_14).random(),
) {

    var leader: Int = 0

    var active: Boolean = true
    var ruinYear: Int = 0

    @Transient val expandMap = MetaStepMap()
    @Transient var expandMapDirty = true

    open fun shortName() = App.history.figure(leader)?.name ?: "MYSTERY"

    fun strongholds(): List<Stronghold> = App.history.strongholds.filter { it.empire == this.id }

    fun addStronghold(stronghold: Stronghold) {
        stronghold.empire?.also { oldEmpireId ->
            App.history.empire(oldEmpireId)!!.loseStronghold(stronghold)
        }
        stronghold.empire = this.id
        expandMapDirty = true
    }

    fun loseStronghold(stronghold: Stronghold) {
        stronghold.empire = null
        expandMapDirty = true
    }

    private fun updateExpandMap() {
        expandMap.apply {
            reset()
            strongholds().forEach { addTarget(chunkXtoX(it.worldX), chunkYtoY(it.worldY)) }
            update()
        }
        expandMapDirty = false
    }

    open fun passYears(yearsPassed: Int) {
        if (strongholds().isEmpty()) return
        if (expandMapDirty) updateExpandMap()
        var target: Stronghold? = null
        var targetDistance = 99999
        App.history.freeStrongholds().forEach {
            val distance = expandMap.distanceAt(chunkXtoX(it.worldX), chunkYtoY(it.worldY))
            if (distance < targetDistance) {
                target = it
                targetDistance = distance
            }
        }
        target?.also { target ->
            attackStronghold(target)
        }
    }

    private fun attackStronghold(target: Stronghold) {
        addStronghold(target)
        App.history.log("${shortName()} takes ${target.name()}!")
    }

    private fun chunkXtoX(x: Int) = Metamap.chunkXtoX(x)
    private fun chunkYtoY(y: Int) = Metamap.chunkYtoY(y)
}

@Serializable
class LightEmpire : Empire(0, 1994, Glyph.MAP_COLOR_15) {
    private val prophetBirthChance = 0.1f

    var activeProphet = -1

    override fun shortName() = "the Lords of Light"

    override fun passYears(yearsPassed: Int) {
        val freeStrongholds = App.history.freeStrongholds()
        if (freeStrongholds.isEmpty()) return

        val prophet = App.history.figure(activeProphet)
        if (prophet == null || !prophet.alive) {
            activeProphet = -1
            if (Dice.chance(prophetBirthChance)) {
                birthProphet(freeStrongholds.random())
            }
        }
        
        super.passYears(yearsPassed)
    }

    private fun birthProphet(birthplace: Stronghold) {
        val prophet = App.history.createFigure().apply {
            name = Madlib.prophetName()
        }
        addStronghold(birthplace)
        activeProphet = prophet.id
        App.history.log("The prophet ${prophet.name} appears in ${birthplace.name()}.")
    }
}
