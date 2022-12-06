package world.cartos

import App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.newSingleThreadAsyncContext
import ui.panels.Console
import util.*
import world.Biome
import world.ChunkMeta
import world.ChunkScratch
import world.RiverExit
import world.level.CHUNK_SIZE

object Metamap {

    private val chunkRadius = 100

    private val coroutineContext = newSingleThreadAsyncContext("Metamapper")
    private val coroutineScope = CoroutineScope(coroutineContext)

    var isWorking = false

    val riverCount = 4500
    val inlandSeaCount = 2
    val maxRiverWidth = 10

    val outOfBoundsMeta = ChunkMeta(biome = Biome.OCEAN)

    private var scratches = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }
    val metas = ArrayList<ArrayList<ChunkMeta>>()

    fun metaAt(x: Int, y: Int) = if (boundsCheck(x,y)) metas[x][y] else outOfBoundsMeta

    fun boundsCheck(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0 || x >= chunkRadius * 2 || y >= chunkRadius * 2) return false
        return true
    }

    fun buildWorld() {

        scratches = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }


        fun metaAt(x: Int, y: Int): ChunkScratch? = if (boundsCheck(x,y)) scratches[x][y] else null

        fun setRiverOffset(exit: RiverExit, offset: Int) {
            if (exit.offset == -999) {
                exit.offset = offset
                exit.otherSide?.offset = offset
                exit.otherSide = null
            }
        }

        isWorking = true
        coroutineScope.launch {
            Console.sayFromThread("Breaking the moon in half...")

            for (ix in -chunkRadius until chunkRadius) {
                for (iy in -chunkRadius until chunkRadius) {
                    val chunkX = ix * CHUNK_SIZE
                    val chunkY = iy * CHUNK_SIZE
                    scratches[ix + chunkRadius][iy + chunkRadius].apply {
                        x = chunkX
                        y = chunkY
                    }
                }
            }

            // CONTINENT
            for (i in 0 until chunkRadius*2) {
                scratches[i][0].height = 0
                scratches[i][chunkRadius * 2 - 1].height = 0
                scratches[0][i].height = 0
                scratches[chunkRadius * 2 - 1][i].height = 0
            }
            repeat (50) {
                val x0 = Dice.zeroTil(chunkRadius*2-10)
                val y0 = Dice.zeroTil(chunkRadius*2-10)
                var x1 = x0 + Dice.zeroTo(4)
                var y1 = y0 + Dice.zeroTo(4)
                if (Dice.chance(0.3f)) {
                    x1 = (x1 - x0) * (2 + Dice.oneTo(5)) + x0
                    y1 = (y1 - y0) * (2 + Dice.oneTo(5)) + y0
                }
                for (x in x0 until x1) {
                    for (y in y0 until y1) {
                        if (boundsCheck(x,y)) scratches[x][y].height = 0
                    }
                }
            }
            for (density in listOf(0.2f, 0.6f, 0.1f, 0.5f, 0.6f, 0.2f, 0.2f)) {
                for (x in 0 until chunkRadius * 2) {
                    for (y in 0 until chunkRadius * 2) {
                        if (scratches[x][y].height == 0) {
                            CARDINALS.forEach { dir ->
                                if (boundsCheck(x+dir.x, y+dir.y)) {
                                    val neighbor = scratches[x + dir.x][y + dir.y]
                                    if ((neighbor.height == -1) && Dice.chance(density)) {
                                        neighbor.height = -2
                                        CARDINALS.forEach { ndir ->
                                            if (boundsCheck(x+dir.x+ndir.x, y+dir.y+ndir.y)) {
                                                val nn = scratches[x+dir.x+ndir.x][y+dir.y+ndir.y]
                                                if (Dice.chance(density * 0.7f)) nn.height = -2
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (x in 0 until chunkRadius * 2) {
                    for (y in 0 until chunkRadius * 2) {
                        if (scratches[x][y].height == -2) scratches[x][y].height = 0
                    }
                }
            }

            // WATER
            Console.sayFromThread("Stirring the face of the waters...")

            val opens = ArrayList<XY>()
            val springs = ArrayList<XY>()
            val mouths = ArrayList<XY>()

            // Set river mouths at all coast water
            for (x in 0 until chunkRadius * 2) {
                for (y in 0 until chunkRadius * 2) {
                    if (scratches[x][y].height == 0) {
                        if (CARDINALS.hasOneWhere {
                            boundsCheck(x+it.x,y+it.y) && scratches[x+it.x][y+it.y].height == -1
                            }) {
                            mouths.add(XY(x,y))
                            opens.add(XY(x,y))
                        }
                    }
                }
            }

            // Run slopes up from bottoms
            while (opens.isNotEmpty()) {
                opens.shuffled().forEach { open ->
                    var added = false
                    val dirs = CARDINALS.shuffled().toMutableList()
                    while (!added && dirs.isNotEmpty()) {
                        val dir = dirs.removeFirst()
                        if (boundsCheck(open.x + dir.x, open.y + dir.y)) {
                            val neighbor = scratches[open.x + dir.x][open.y + dir.y]
                            if (neighbor.height == -1) {
                                scratches[open.x][open.y].hasRiverChildren = true
                                neighbor.height = scratches[open.x][open.y].height + 1
                                neighbor.riverParentX = open.x
                                neighbor.riverParentY = open.y
                                opens.add(XY(open.x + dir.x, open.y + dir.y))
                                added = true
                            }
                        }
                    }
                    if (!added) { // No free dirs from this node, it's done
                        opens.remove(open)
                    }
                }
            }
            for (ix in 0 until chunkRadius*2) {
                for (iy in 0 until chunkRadius*2) {
                    if (!scratches[ix][iy].hasRiverChildren) {
                        springs.add(XY(ix,iy))
                    }
                }
            }

            // Make some of our slope paths into rivers
            Console.sayFromThread("Running $riverCount rivers from ${springs.size} possible springs...")
            repeat (riverCount) {
                val head = springs.random()
                springs.remove(head)
                var done = false
                var width = 2
                while (!done) {
                    val cell = scratches[head.x][head.y]
                    if (cell.height > 0 && !cell.riverRun) {
                        cell.riverRun = true
                        val childExit = RiverExit(
                            edge = XY(cell.riverParentX - head.x, cell.riverParentY - head.y),
                            width = if (cell.height > 1) width else (width * 2f).toInt()
                        )
                        cell.riverExits.add(childExit)

                        if (width > 2 && Dice.chance(0.05f)) {
                            width--
                        } else if (width < maxRiverWidth && Dice.chance(0.2f)) {
                            width++
                        }

                        val parent = scratches[cell.riverParentX][cell.riverParentY]
                        val parentExit = RiverExit(
                            edge = XY(head.x - cell.riverParentX, head.y - cell.riverParentY),
                            width = if (cell.height > 1) width else (width * 2f).toInt()
                        )
                        parent.riverExits.add(parentExit)
                        head.x = cell.riverParentX
                        head.y = cell.riverParentY

                        childExit.otherSide = parentExit
                        parentExit.otherSide = childExit
                    } else {
                        done = true
                    }
                }
            }
            // Set wiggles and offsets now that we know every river connection
            for (x in 0 until chunkRadius*2) {
                for (y in 0 until chunkRadius*2) {
                    val cell = scratches[x][y]
                    if (cell.riverExits.isNotEmpty()) {

                        val wiggle = 0.9f  // TODO : get from perlin
                        cell.riverWiggle = wiggle
                        cell.riverBlur = 0.3f
                        cell.riverGrass = 0.8f
                        cell.riverDirt = 0.2f

                        var isNorth = false
                        var isSouth = false
                        var isEast = false
                        var isWest = false
                        cell.riverExits.forEach { exit ->
                            when (exit.edge) {
                                NORTH -> isNorth = true
                                SOUTH -> isSouth = true
                                WEST -> isWest = true
                                else -> isEast = true
                            }
                        }
                        val cornerPush = 8
                        if (cell.riverExits.size != 2 || (isNorth && isSouth) || (isEast && isWest)) {
                            cell.riverExits.forEach { exit ->
                                setRiverOffset(exit, Dice.zeroTil((CHUNK_SIZE / 2f * wiggle).toInt()) - CHUNK_SIZE / 4)
                            }
                        } else if ((isNorth && isEast) || (isSouth && isWest)) {
                            cell.riverExits.firstOrNull { it.edge == NORTH || it.edge == WEST }?.also {
                                setRiverOffset(it, cornerPush)
                            }
                            cell.riverExits.firstOrNull { it.edge == EAST || it.edge == SOUTH }?.also {
                                setRiverOffset(it, -cornerPush)
                            }
                        } else if ((isNorth && isWest)) {
                            cell.riverExits.firstOrNull { it.edge == NORTH }?.also {
                                setRiverOffset(it, -cornerPush)
                            }
                            cell.riverExits.firstOrNull { it.edge == WEST }?.also {
                                setRiverOffset(it, -cornerPush)
                            }
                        } else if ((isSouth && isEast)) {
                            cell.riverExits.firstOrNull { it.edge == SOUTH }?.also {
                                setRiverOffset(it,  cornerPush)
                            }
                            cell.riverExits.firstOrNull { it.edge == EAST }?.also {
                                setRiverOffset(it, cornerPush)
                            }
                        }
                    }

                    // Set coasts
                    if (cell.height > 0) {
                        DIRECTIONS.forEach { dir ->
                            metaAt(x + dir.x, y + dir.y)?.also { neighbor ->
                                if (neighbor.height == 0) {
                                    cell.coasts.add(dir)
                                }
                            }
                        }
                    }

                    // Set biome
                    if (cell.height == 0) {
                        cell.biome = Biome.OCEAN
                    } else {
                        cell.biome = Biome.PLAIN
                    }

                }
            }


            // END STAGE : WRITE ALL DATA

            Console.sayFromThread("Saving generated world...")
            for (ix in -chunkRadius until chunkRadius) {
                App.save.putWorldMetas(scratches[ix + chunkRadius])
                ArrayList<ChunkMeta>().also {
                    for (iy in -chunkRadius until chunkRadius) {
                        it.add(scratches[ix + chunkRadius][iy + chunkRadius].toChunkMeta())
                    }
                    metas.add(it)
                }
                if (ix % 50 == 0) {
                    Console.sayFromThread("...wrote latitude ${ix * CHUNK_SIZE}...")
                }
            }
            scratches = Array(1) { Array(1) { ChunkScratch() } }

            log.info("Metamapper completed!")
            isWorking = false
        }
    }

}
