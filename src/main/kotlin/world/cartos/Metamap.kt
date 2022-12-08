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

    val riverDensity = 0.4f
    val riverBranching = 0.25f
    val maxRiverWidth = 10
    val minMountainHeight = 20
    val isolatedMountainDensity = 0.2f

    val outOfBoundsMeta = ChunkMeta(biome = Biome.OCEAN)

    private var scratches = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }
    val metas = ArrayList<ArrayList<ChunkMeta>>()

    fun metaAt(x: Int, y: Int) = if (boundsCheck(x,y)) metas[x][y] else outOfBoundsMeta

    fun boundsCheck(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0 || x >= chunkRadius * 2 || y >= chunkRadius * 2) return false
        return true
    }

    fun xToChunkX(x: Int) = (x - chunkRadius) * CHUNK_SIZE
    fun yToChunkY(y: Int) = (y - chunkRadius) * CHUNK_SIZE
    fun chunkXtoX(x: Int) = (x / CHUNK_SIZE) + chunkRadius
    fun chunkYtoY(y: Int) = (y / CHUNK_SIZE) + chunkRadius

    fun forEachMeta(doThis: (x: Int, y: Int, cell: ChunkScratch)->Unit) {
        for (x in 0 until chunkRadius * 2) {
            for (y in 0 until chunkRadius * 2) {
                doThis(x,y,scratches[x][y])
            }
        }
    }

    fun loadWorld() {
        isWorking = true
        coroutineScope.launch {
            Console.sayFromThread("Loading world map...")

            for (x in 0 until chunkRadius*2) {
                val col = ArrayList<ChunkMeta>()
                App.save.getWorldMetaColumn(xToChunkX(x)).forEach { col.add(chunkYtoY(it.y), it) }
                metas.add(col)
            }

            Console.sayFromThread("Load completed!")
            isWorking = false
        }
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
            // Cut squares out of edge coast
            for (i in 0 until chunkRadius*2) {
                if (Dice.chance(0.1f)) {
                    val w = Dice.range(4,25)
                    val h = Dice.range(4,25)
                    val edge = CARDINALS.random()
                    val x0 = when (edge) {
                        NORTH, SOUTH -> i
                        EAST -> (chunkRadius*2-1)-w
                        else -> 0
                    }
                    val x1 = when (edge) {
                        NORTH, SOUTH -> i + w
                        EAST -> chunkRadius*2-1
                        else -> w
                    }
                    val y0 = when (edge) {
                        NORTH -> 0
                        SOUTH -> (chunkRadius*2-1)-h
                        else -> i
                    }
                    val y1 = when (edge) {
                        NORTH -> h
                        SOUTH -> (chunkRadius*2-1)
                        else -> i + h
                    }
                    for (x in x0 until x1) {
                        for (y in y0 until y1) {
                            if (boundsCheck(x, y) && !((x == x0 || x == x1-1) && (y == y0 || y == y1-1)))
                                scratches[x][y].height = 0
                        }
                    }
                }
            }
            // Cut square seas, preferably around the edge
            repeat (100) {
                val x0 = Dice.zeroTil(chunkRadius*2-5)
                val y0 = Dice.zeroTil(chunkRadius*2-5)
                if ((!(x0 in 32..chunkRadius*2-32) && !(y0 in 32 .. chunkRadius*2-32)) || Dice.chance(0.15f)) {
                    var x1 = x0 + Dice.range(3, 12)
                    var y1 = y0 + Dice.range(3, 12)
                    if (Dice.chance(0.3f)) {
                        x1 = (x1 - x0) * (2 + Dice.oneTo(5)) + x0
                        y1 = (y1 - y0) * (2 + Dice.oneTo(5)) + y0
                    }
                    for (x in x0 until x1) {
                        for (y in y0 until y1) {
                            if (boundsCheck(x, y) && !((x == x0 || x == x1-1) && (y == y0 || y == y1-1)))
                                scratches[x][y].height = 0
                        }
                    }
                }
            }
            // Grow the seas
            for (density in listOf(0.2f, 0.6f, 0.1f, 0.5f, 0.6f, 0.2f, 0.2f)) {
                forEachMeta { x,y,cell ->
                    if (cell.height == 0) {
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
                forEachMeta { x,y,cell ->
                    if (cell.height == -2) cell.height = 0
                }
            }

            // WATER
            Console.sayFromThread("Stirring the face of the waters...")

            val opens = ArrayList<XY>()
            val springs = ArrayList<XY>()
            val coasts = ArrayList<XY>()

            // Set river mouths at all coast water
            forEachMeta { x,y,cell ->
                if (cell.height == 0) {
                    if (CARDINALS.hasOneWhere {
                        boundsCheck(x+it.x,y+it.y) && scratches[x+it.x][y+it.y].height == -1
                        }) {
                        coasts.add(XY(x,y))
                        opens.add(XY(x,y))
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
                                scratches[open.x][open.y].riverChildren.add(XY(open.x+dir.x, open.y+dir.y))
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
                if (opens.size > 200) {
                    repeat((opens.size * 0.2f).toInt()) {
                        val kill = opens.random()
                        opens.remove(kill)
                    }
                }
            }
            // Find all river springs
            for (ix in 0 until chunkRadius*2) {
                for (iy in 0 until chunkRadius*2) {
                    if (scratches[ix][iy].riverChildren.isEmpty()) {
                        springs.add(XY(ix,iy))
                    }
                    if (scratches[ix][iy].height < 0) scratches[ix][iy].height = 1
                }
            }

            // Set mountaintops
            forEachMeta { x,y,cell ->
                val height = cell.height
                if (height > minMountainHeight) {
                    if (!DIRECTIONS.hasOneWhere {
                        boundsCheck(x+it.x,y+it.y) && scratches[x+it.x][y+it.y].height > height
                        }) {
                        scratches[x][y].biome = Biome.MOUNTAIN
                    }
                }
            }

            // Grow mountain ranges
            val mountainAdds = ArrayList<XY>()
            var density = 0.8f
            repeat (3) {
                forEachMeta { x,y,cell ->
                    if (cell.biome != Biome.MOUNTAIN) {
                        var nearMounts = biomeNeighbors(x,y,Biome.MOUNTAIN, true)
                        if (nearMounts > 1 && Dice.chance(density)) {
                            mountainAdds.add(XY(x, y))
                        }
                    }
                }
                mountainAdds.forEach { scratches[it.x][it.y].biome = Biome.MOUNTAIN }
                density *= 0.5f
            }

            // Filter out most isolated mountain cells
            forEachMeta { x,y,cell ->
                if ((cell.biome == Biome.MOUNTAIN) && biomeNeighbors(x,y,Biome.MOUNTAIN) < 1) {
                    if (!Dice.chance(isolatedMountainDensity)) cell.biome = Biome.PLAIN
                }
            }

            // Freeze ice caps
            Console.sayFromThread("Freezing ice cap..")
            forEachMeta { x,y,cell ->
                if (y < 20 && cell.height == 0) {
                    cell.biome = Biome.GLACIER
                    springs.removeIf { it.x == x && it.y == y }
                }
            }
            for (y in 20..30) {
                for (x in 0 until chunkRadius*2) {
                    if ((scratches[x][y-1].biome == Biome.GLACIER) && Dice.chance(1f - (y - 20) * 0.09f)) {
                        scratches[x][y].biome = Biome.GLACIER
                    }
                }
            }

            // Pick some river mouths from coast cells
            val mouths = mutableListOf<XY>()
            coasts.forEach { coast ->
                if (Dice.chance(riverDensity) && scratches[coast.x][coast.y].biome != Biome.GLACIER) mouths.add(coast)
            }
            // Recurse up each mouth, marking all branching river exits
            Console.sayFromThread("Running ${mouths.size} rivers from ${coasts.size} possible coasts...")
            mouths.forEach { mouth ->
                countRiverDescendantsAndSetWidths(mouth)
                runRiver(mouth)
            }

            // Calculate moisture
            Console.sayFromThread("Moisturizing biomes...")
            forEachMeta { x,y,cell ->
                if (cell.height == 0 || cell.riverExits.isNotEmpty()) {
                    cell.dryness = 0
                }
            }
            var done = false
            var maxDry = 0
            while (!done) {
                done = true
                forEachMeta { x,y,cell ->
                    if (cell.dryness == maxDry) {
                        DIRECTIONS.forEach { dir ->
                            if (boundsCheck(x+dir.x,y+dir.y)) {
                                val neighbor = scratches[x+dir.x][y+dir.y]
                                if (neighbor.dryness == -1) {
                                    neighbor.dryness = maxDry + 1
                                    done = false
                                }
                            }
                        }
                    }
                }
                maxDry++
            }
            log.info("max cell dryness: $maxDry")

            // Set wiggles and offsets now that we know every river connection
            forEachMeta { x,y,cell ->
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
                if (cell.biome == Biome.BLANK) {
                    if (cell.height == 0) {
                        cell.biome = Biome.OCEAN
                    } else if (cell.dryness >= (maxDry * 0.6f).toInt()) {
                        cell.biome = Biome.DESERT
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

            Console.sayFromThread("The world is new.")
            isWorking = false
        }
    }

    private fun countRiverDescendantsAndSetWidths(mouth: XY) {
        val springs = findRiverSprings(mouth)
        springs.forEach { spring ->
            var done = false
            val cursor = XY(spring.x,spring.y)
            var count = 0
            while (!done) {
                done = true
                val cell = scratches[cursor.x][cursor.y]
                cell.riverDescendantCount += count
                count++
                if (cell.height > 0) {
                    done = false
                    cursor.x = cell.riverParentX
                    cursor.y = cell.riverParentY
                }
            }
        }
    }

    private fun findRiverSprings(cursor: XY): MutableList<XY> {
        val cell = scratches[cursor.x][cursor.y]
        val springs = mutableListOf<XY>()
        if (cell.riverChildren.isEmpty()) {
            springs.add(cursor)
        } else cell.riverChildren.forEach { child ->
            springs.addAll(findRiverSprings(child))
        }
        return springs
    }

    private fun runRiver(cursor: XY) {
        val cell = scratches[cursor.x][cursor.y]
        if (cell.riverChildren.isNotEmpty()) {
            var longest = cell.riverChildren[0]
            cell.riverChildren.forEach {
                if (scratches[it.x][it.y].riverDescendantCount > scratches[longest.x][longest.y].riverDescendantCount) {
                    longest = it
                }
            }
            cell.riverChildren.forEachIndexed { n, child ->
                if (child == longest || Dice.chance(riverBranching)) {
                    val childCell = scratches[child.x][child.y]
                    val myExit = RiverExit(dest = child, edge = XY(child.x - cursor.x, child.y - cursor.y))
                    val childExit = RiverExit(dest = cursor, edge = XY(cursor.x - child.x, cursor.y - child.y))
                    childExit.otherSide = myExit
                    myExit.otherSide = childExit
                    cell.riverExits.add(myExit)
                    childCell.riverExits.add(childExit)
                    runRiver(child)
                }
            }
        }
    }

    private fun biomeNeighbors(x: Int, y: Int, biome: Biome, allDirections: Boolean = false): Int {
        var c = 0
        (if (allDirections) DIRECTIONS else CARDINALS).forEach { dir ->
            if (boundsCheck(x + dir.x, y + dir.y) &&
                scratches[x + dir.x][y + dir.y].biome == biome) {
                c++
            }
        }
        return c
    }
}
