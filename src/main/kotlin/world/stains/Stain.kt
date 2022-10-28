package world.stains

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.Temporal
import world.CellContainer

@Serializable
sealed class Stain : Temporal {

    enum class Type { BLOOD }

    var offsetX = 0f
    var offsetY = 0f
    var scale = 1.0
    var alpha = 1f

    @Transient var holder: CellContainer? = null
    private var birthTime: Double = App.time
    protected var sizeMod: Float = 0f
    protected var posModX: Float = 0f
    protected var posModY: Float = 0f
    protected var alphaMod: Float = 0f

    abstract fun glyph(): Glyph
    abstract fun name(): String
    abstract fun lifespan(): Double
    abstract fun stackType(): Type

    override fun temporalDone() = holder == null

    open fun stackWith(stain: Stain) {
        birthTime = App.time
    }

    override fun advanceTime(delta: Float) {
        offsetX = posModX + sizeMod
        offsetY = posModY + sizeMod
        scale = 1.0 - sizeMod
        val elapsed = App.time - birthTime
        val lifespan = lifespan()
        val halfspan = (lifespan / 2f).toFloat()
        if (elapsed > lifespan) {
            expire()
        } else if (elapsed > halfspan) {
            alpha = 1f - (elapsed - halfspan).toFloat() / halfspan + alphaMod
        } else {
            alpha = 1f + alphaMod
        }
    }

    fun expire() {
        holder?.expireStain(this)
    }

    fun onRestore(holder: CellContainer) {
        this.holder = holder
        holder.level?.linkTemporal(this)
        advanceTime(0f)
    }
}