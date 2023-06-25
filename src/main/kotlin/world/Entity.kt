package world

import render.batches.QuadBatch
import render.tilesets.Glyph
import util.Rect
import util.XY
import util.aOrAn
import world.level.Level

interface Entity {

    enum class Gender(
        val adj: String,
        val human: String,
        val ps: String,
        val po: String,
        val pp: String,
        val pr: String
    ) {
        MALE("male", "man", "he", "him", "his", "himself"),
        FEMALE("female", "woman", "she", "her", "her", "herself"),
        NEUTER("thing", "it", "it", "it", "its", "itself")
    }

    fun glyph(): Glyph
    fun hue(): Float = 0f
    fun glyphBatch(): QuadBatch
    fun uiBatch(): QuadBatch
    fun name(): String
    fun hasProperName(): Boolean = false
    fun dname() = if (hasProperName()) name() else "the " + name()
    fun dnamec() = if (hasProperName()) name() else "The " + name()
    fun iname() = if (hasProperName()) name() else name().aOrAn()
    fun inamec() = if (hasProperName()) name() else name().aOrAn().capitalize()
    fun description(): String
    fun examineDescription(): String = description()
    fun examineInfo(): String = "You don't know anything interesting about " + iname() + "."
    fun gender(): Gender = Gender.NEUTER

    fun level(): Level?
    fun xy(): XY

    fun onRender(delta: Float) { }

}
