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
            "res/sheets/tiles_mob.png", 11, 11, Screen.textureFilter
        ),
        TerrainSprites(
            "res/sheets/tiles_terrain.png", 12, 14, Screen.textureFilter
        ),
        ThingSprites(
            "res/sheets/tiles_thing.png", 13, 11, Screen.textureFilter
        ),
        UISprites(
            "res/sheets/tiles_ui.png", 6, 7, Texture.TextureFilter.Nearest
        ),
        MapSprites(
            "res/sheets/tiles_map.png", 10, 8, Screen.textureFilter
        ),
        PortraitSprites(
            "res/sheets/tiles_portrait.png", 10, 10, Texture.TextureFilter.Nearest
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
