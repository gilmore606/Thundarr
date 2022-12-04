package world.cartos

import App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.newSingleThreadAsyncContext
import ui.panels.Console
import util.*
import world.Biome
import world.ChunkScratch
import world.RiverExit
import world.level.CHUNK_SIZE

object Metamapper {

    private val chunkRadius = 100

    private val coroutineContext = newSingleThreadAsyncContext("Metamapper")
    private val coroutineScope = CoroutineScope(coroutineContext)

    var isWorking = false

    val riverMouthDensity = 0.03f
    val riverCount = 1500
    val inlandSeaCount = 2
    val maxRiverWidth = 10

    var metas = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }

    fun buildWorld() {

        metas = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }

        fun boundsCheck(x: Int, y: Int): Boolean {
            if (x < 0 || y < 0 || x >= chunkRadius * 2 || y >= chunkRadius * 2) return false
            return true
        }

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
                    metas[ix + chunkRadius][iy + chunkRadius].apply {
                        x = chunkX
                        y = chunkY
                    }
                }
            }

            // WATER
            Console.sayFromThread("Stirring the face of the waters...")

            val opens = ArrayList<XY>()
            val springs = ArrayList<XY>()
            val mouths = ArrayList<XY>()

            // Set the initial sea bottom and river mouths around outer edges
            for (i in 0 until chunkRadius*2) {
                metas[i][0].height = 0
                metas[i][chunkRadius*2-1].height = 0
                metas[0][i].height = 0
                metas[chunkRadius*2-1][i].height = 0
                if (Dice.chance(riverMouthDensity)) {
                    val mouth = when (Dice.zeroTil(4)) {
                        0 -> XY(i, 1)
                        1 -> XY(1, i)
                        2 -> XY(i, chunkRadius*2-2)
                        else -> XY(chunkRadius*2-2, i)
                    }
                    metas[mouth.x][mouth.y].height = 0
                    opens.add(mouth)
                    mouths.add(mouth)
                }
            }
            // Add inland sea river mouths
            for (i in 0 until inlandSeaCount) {
                val sea = XY(Dice.zeroTil(chunkRadius*2-20) + 10, Dice.zeroTil(chunkRadius*2-20) + 10)
                metas[sea.x][sea.y].height = 0
                opens.add(sea)
                mouths.add(sea)
            }

            // Run slopes up from bottoms
            while (opens.isNotEmpty()) {
                opens.shuffled().forEach { open ->
                    var added = false
                    val dirs = CARDINALS.shuffled().toMutableList()
                    while (!added && dirs.isNotEmpty()) {
                        val dir = dirs.removeFirst()
                        if (boundsCheck(open.x + dir.x, open.y + dir.y)) {
                            val neighbor = metas[open.x + dir.x][open.y + dir.y]
                            if (neighbor.height == -1) {
                                metas[open.x][open.y].hasRiverChildren = true
                                neighbor.height = metas[open.x][open.y].height + 1
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
                    if (!metas[ix][iy].hasRiverChildren) {
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
                    val cell = metas[head.x][head.y]
                    if (cell.height > 0 && !cell.riverRun) {

                        cell.riverRun = true
                        val childExit = RiverExit(
                            edge = XY(cell.riverParentX - head.x, cell.riverParentY - head.y),
                            width = width
                        )
                        cell.riverExits.add(childExit)

                        if (width > 2 && Dice.chance(0.1f)) {
                            width--
                        } else if (width < maxRiverWidth && Dice.chance(0.2f)) {
                            width++
                        }

                        val parent = metas[cell.riverParentX][cell.riverParentY]
                        val parentExit = RiverExit(
                            edge = XY(head.x - cell.riverParentX, head.y - cell.riverParentY),
                            width = width
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
                    val cell = metas[x][y]
                    if (cell.riverExits.isNotEmpty()) {

                        val wiggle = 0.2f  // TODO : get from perlin
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

                    // Set biome
                    if (cell.height == 0) {
                        cell.biome = Biome.OCEAN
                    } else {
                        cell.biome = Biome.PLAIN
                    }

                }
            }


            // END STAGE : WRITE ALL DATA

            Console.sayFromThread("Saving generated world to database...")
            for (ix in -chunkRadius until chunkRadius) {
                App.save.putWorldMetas(metas[ix + chunkRadius])
                if (ix % 100 == 0) {
                    Console.sayFromThread("wrote latitude ${ix * CHUNK_SIZE}...")
                }
            }
            metas = Array(1) { Array(1) { ChunkScratch() } }

            log.info("Metamapper completed!")
            isWorking = false
        }
    }

}
