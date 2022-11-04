package world

import render.batches.QuadBatch
import render.tilesets.Glyph
import util.XY
import util.aOrAn

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
    fun dname() = "the " + name()
    fun dnamec() = "The " + name()
    fun iname() = name().aOrAn()
    fun inamec() = name().aOrAn().capitalize()
    fun description(): String
    fun examineDescription(): String = description()
    fun examineInfo(): String = "You don't know anything interesting about " + iname() + "."
    fun gender(): Gender = Gender.NEUTER

    fun level(): Level?
    fun xy(): XY?

    fun onRender(delta: Float) { }

}
