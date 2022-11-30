package render.tilesets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import render.Screen
import util.log

object SpriteSheets {

    enum class Sheet(
        val fileName: String,
        val tilesPerRow: Int,
        val tilesPerColumn: Int,
        val filter: Texture.TextureFilter = Texture.TextureFilter.Nearest
    ) {
        ActorSprites(
            "tiles_mob.png", 5, 5, Screen.textureFilter
        ),
        TerrainSprites(
            "tiles_terrain.png", 12, 8, Screen.textureFilter
        ),
        ThingSprites(
            "tiles_thing.png", 10, 11, Screen.textureFilter
        ),
        UISprites(
            "tiles_ui.png", 6, 5, Texture.TextureFilter.Nearest
        )
    }

    val sheets = mutableMapOf<Sheet, Texture>()

    init {
        for (sheet in Sheet.values()) {
            sheets[sheet] = Texture(
                Gdx.files.internal(sheet.fileName), true).apply {
                    setFilter(sheet.filter, sheet.filter)
            }
            log.info("Loaded spritesheet texture ${sheet.fileName} (${sheets[sheet]!!.width} x ${sheets[sheet]!!.height})")
        }
    }
}
