package world.stains

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import render.tilesets.Glyph
import things.Temporal
import world.CellContainer

@Serializable
sealed class Stain : Temporal {

    enum class Type { BLOOD }

    @Transient var holder: CellContainer? = null
    private var birthTime: Double = App.time
    protected var sizeMod: Float = 0f
    protected var posModX: Float = 0f
    protected var posModY: Float = 0f
    protected var alphaMod: Float = 0f
    protected var alpha: Float = 1f

    abstract fun glyph(): Glyph
    abstract fun name(): String
    abstract fun lifespan(): Double
    abstract fun stackType(): Type


    open fun stackWith(stain: Stain) {
        birthTime = App.time
    }

    override fun advanceTime(delta: Float) {
        val elapsed = App.time - birthTime
        val lifespan = lifespan()
        val halfspan = (lifespan / 2f).toFloat()
        if (elapsed > lifespan) {
            expire()
        } else if (elapsed > halfspan) {
            alpha = 1f - (elapsed - halfspan).toFloat() / halfspan
        } else {
            alpha = 1f
        }
    }

    fun expire() {
        holder?.expireStain(this)
    }

    fun offsetX() = posModX + sizeMod
    fun offsetY() = posModY + sizeMod
    fun scale() = 1f - sizeMod
    fun alpha() = alpha + alphaMod

    fun onRestore(holder: CellContainer) {
        this.holder = holder
        holder.level?.linkTemporal(this)
        advanceTime(0f)
    }
}
