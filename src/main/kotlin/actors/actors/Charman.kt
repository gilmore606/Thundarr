package actors.actors

import actors.abilities.Scare
import actors.bodyparts.Bodypart
import actors.states.IdleWander
import kotlinx.serialization.Serializable
import render.sparks.GooGore
import render.tilesets.Glyph
import things.Clothing
import things.FurHide
import things.Log
import world.Entity
import world.stains.Goo

@Serializable
class Charman : NPC() {
    override val tag = Tag.CHARMAN
    override fun glyph() = Glyph.DEAD_TREE_MAN
    override fun name() = "charman"
    override fun description() = "A charred tree, brought to hideous life by unknown sorcery."
    override fun makeBody() = Bodypart.tree()
    override fun makeAbilities() = setOf(Scare())
    override fun isSentient() = true
    override fun canSwimShallow() = true
    override fun bloodstain() = Goo()
    override fun gore() = GooGore()
    override fun xpValue() = 80
    override fun hpMax() = 16f
    override fun onSpawn() {
        initStats(12, 12, 6, 13, 10, 4, 2)
    }
    override fun corpse() = null
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
