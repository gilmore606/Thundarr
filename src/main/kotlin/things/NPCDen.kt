package things

import actors.NPC
import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import util.log

@Serializable
class NPCDen(
    private val npcType: NPC.Tag,
    private val spawnDelay: Double = 100.0,
) : Thing(), Temporal {

    private var npcId: String? = null
    private var npcLostAt: Double = 0.0

    override fun isIntangible() = true
    override fun name() = "NPC DEN"
    override fun description() = "NPC DEN"
    override fun glyph() = Glyph.BLANK
    override val tag = Tag.THING_NPCDEN
    override fun isOpaque() = false
    override fun isBlocking() = false
    override fun isPortable() = false

    override fun onRestore(holder: ThingHolder) {
        super.onRestore(holder)
        npcId?.also { npcId ->
            App.level.director.getActor(npcId)?.also { npc ->
                (npc as NPC).den = this
            }
        }
    }

    override fun advanceTime(delta: Float) {
        if (npcId == null) {
            if (App.gameTime.time - npcLostAt > spawnDelay) {
                if (App.player.level?.chunkAt(App.player.xy.x, App.player.xy.y) != this.chunk()) {
                    spawnNpc()
                }
            }
        } else {
            App.level.director.getActor(npcId!!)?.also { npc ->
                (npc as NPC).den = this
            }
        }
    }

    fun chunk() = xy().let { xy -> level()?.chunkAt(xy.x, xy.y) }

    private fun spawnNpc() {
        level()?.also { level ->
            NPC.create(npcType).also {
                it.den = this
                it.spawnAt(level, xy()!!.x, xy()!!.y)
                npcId = it.id
            }
        }
    }

    fun onDie(departed: NPC) {
        if (departed.id == npcId) {
            npcId = null
            npcLostAt = App.gameTime.time
        }
    }

}
