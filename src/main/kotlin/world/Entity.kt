package world

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
    fun name(): String
    fun dname() = "the " + name()
    fun iname() = name().aOrAn()
    fun description(): String
    open fun gender(): Gender = Gender.NEUTER

    fun level(): Level?
    fun xy(): XY?
}
