package things

import actors.actors.Actor
import actors.actors.Player
import actors.stats.Brains
import actors.stats.Stat
import actors.stats.skills.Survive
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import ui.panels.Console

@Serializable
sealed class Book : Portable() {

    override fun glyph() = Glyph.BOOK_GRAY
    override fun weight() = 0.1f
    override fun value() = 2
    override fun flammability() = 0.8f
    override fun toolbarName() = "read " + this.name()
    override fun toolbarUseTag() = UseTag.USE

    override fun category() = Category.TOOL

    override fun uses() = mutableMapOf(
        UseTag.USE to Use("read " + this.name(), 5f,
            canDo = { actor,x,y,targ -> !targ && isHeldBy(actor) },
            toDo = { actor, level, x, y ->
                tryRead(actor)
            })
    )

    private fun readMsg() = "You open your copy of %d and read for a while."
    private fun readOtherMsg() = "%Dn reads a book."

    open fun text() = "It's something about a guy who's a writer, and he's got writer's block, so he goes to some weird place, and maybe someone attacked him.  It was confusing."

    open fun difficulty() = 1f

    private fun tryRead(actor: Actor) {
        Console.sayAct(readMsg(), readOtherMsg(), actor, this)
        val result = Brains.resolve(actor, difficulty())
        if (result < 0) {
            if (actor is Player) Console.say("You struggle to glean any useful information.")
        } else if (result > 10) {
            if (actor is Player) Console.say("You can't even comprehend such puerile trash.  You snap the book closed, disgusted.")
        } else {
            if (actor is Player) Console.say(text())
            onReadBy(actor)
        }
    }

    open fun onReadBy(actor: Actor) { }
}

@Serializable
class Paperback : Book() {
    override val tag = Tag.PAPERBACK
    override fun name() = "paperback"
    override fun description() = "A waterlogged but still readable old paperback novel."
}

@Serializable
sealed class SkillBook: Book() {

    override fun glyph() = Glyph.BOOK_PURPLE

    abstract fun skill(): Stat
    abstract fun skillMinBase(): Float
    abstract fun skillMaxBase(): Float

    override fun onReadBy(actor: Actor) {
        val skill = skill()
        val base = skill.getBase(actor)
        if (base < skillMinBase()) {
            if (actor is Player) Console.say("You don't understand the subject well enough to learn from this.")
        } else if (base >= skillMaxBase()) {
            if (actor is Player) Console.say("You have nothing more to learn from this.")
        } else {
            if (actor is Player) Console.say("You learned something about " + skill.verb() + "!")
            skill.improve(actor)
        }
    }
}

@Serializable
class BoysLife: SkillBook() {
    override val tag = Tag.BOYSLIFE
    override fun name() = "Boys Life magazine"
    override fun description() = "An ancient but implausibly well preserved magazine, full of practical scouting advice for youths."
    override fun text() = "Oh boy!  An article about " + listOf(
        "fishing", "hunting", "fly fishing", "deer hunting", "squirrel trapping", "fire starting", "campfire building",
        "deadly mantraps", "murderhole construction", "cold weather survival", "cheesemaking", "how beating off is wrong")
        .random() + "!"
    override fun difficulty() = 3f
    override fun skill() = Survive
    override fun skillMinBase() = 0f
    override fun skillMaxBase() = 2f
}
