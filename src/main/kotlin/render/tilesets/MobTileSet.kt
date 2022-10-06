package render.tilesets

import render.tileholders.SimpleTile
import util.Tile

fun MobTileSet() =
    TileSet("tiles_mob.png", 7, 4).apply {
        setTile(Tile.PLAYER, SimpleTile(this, 2, 2))
    }
