package actors.actors

import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import things.Clothing
import things.FurHide
import things.Log
import world.Entity

@Serializable
class Charman : NPC() {
    override val tag = Tag.CHARMAN
    override fun glyph() = Glyph.DEAD_TREE_MAN
    override fun name() = "charman"
    override fun description() = "A charred tree, brought to hideous life by unknown sorcery."
    override fun isSentient() = true
    override fun canSwimShallow() = true
    override fun hpMax() = 16f
    override fun onSpawn() {
        initStats(12, 12, 6, 13, 10, 4, 2)
    }
    override fun corpseMeats() = setOf(Log())
    override fun unarmedWeapon() = branches
    override fun unarmedDamage() = 6f
    override fun skinArmorMaterial() = Clothing.Material.WOOD
    override fun skinArmor() = 1f

    override fun visualRange() = 8f
    override fun canSeeInDark() = true
    override fun idleState() = IdleWander(0.4f)
    override fun opinionOf(actor: Actor) = when {
        actor.isHuman() -> NPC.Opinion.HATE
        else -> super.opinionOf(actor)
    }
}
