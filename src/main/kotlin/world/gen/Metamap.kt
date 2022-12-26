package world.gen

import App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.newSingleThreadAsyncContext
import ui.panels.Console
import util.*
import world.ChunkMeta
import world.ChunkScratch
import world.RiverExit
import world.RoadExit
import world.gen.biomes.Biome
import world.gen.biomes.*
import world.level.CHUNK_SIZE
import java.lang.Integer.max
import java.lang.Integer.min

object Metamap {

    private val chunkRadius = 100

    private val coroutineContext = newSingleThreadAsyncContext("Metamap")
    private val coroutineScope = CoroutineScope(coroutineContext)

    var isWorking = false

    val coastRoughness = 0.15f
    val bigRiverDensity = 0.4f
    val smallRiverDensity = 0.1f
    val riverBranching = 0.25f
    val maxRiverWidth = 10
    val riverWidth = 0.15f
    val minMountainHeight = 20
    val maxRangePeakDistance = 4
    val mountainRangeWetness = 6
    val citiesRiverMouth = 10
    val citiesRiver = 10
    val citiesCoast = 10
    val citiesInland = 8
    val minCityDistance = 15
    val bigCityFraction = 0.2f
    val ruinFalloff = 14f
    val ruinsMax = 5f

    val outOfBoundsMeta = ChunkMeta(biome = Ocean)

    private var scratches = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }
    private var riverCells = ArrayList<XY>()
    private var cityCells = ArrayList<XY>()
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
                doThis(x,y, scratches[x][y])
            }
        }
    }

    fun loadWorld() {
        isWorking = true
        coroutineScope.launch {
            Console.sayFromThread("Loading world map...")

            for (x in 0 until chunkRadius *2) {
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
        riverCells.clear()
        cityCells.clear()

        fun metaAt(x: Int, y: Int): ChunkScratch? = if (boundsCheck(x,y)) scratches[x][y] else null

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
            for (i in 0 until chunkRadius *2) {
                scratches[i][0].height = 0
                scratches[i][chunkRadius * 2 - 1].height = 0
                scratches[0][i].height = 0
                scratches[chunkRadius * 2 - 1][i].height = 0
            }
            // Cut squares out of edge coast
            for (i in 0 until chunkRadius *2) {
                if (Dice.chance(coastRoughness)) {
                    val w = Dice.range(4,25)
                    val h = Dice.range(4,25)
                    val edge = CARDINALS.random()
                    val x0 = when (edge) {
                        NORTH, SOUTH -> i
                        EAST -> (chunkRadius *2-1)-w
                        else -> 0
                    }
                    val x1 = when (edge) {
                        NORTH, SOUTH -> i + w
                        EAST -> chunkRadius *2-1
                        else -> w
                    }
                    val y0 = when (edge) {
                        NORTH -> 0
                        SOUTH -> (chunkRadius *2-1)-h
                        else -> i
                    }
                    val y1 = when (edge) {
                        NORTH -> h
                        SOUTH -> (chunkRadius *2-1)
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
                val x0 = Dice.zeroTil(chunkRadius *2-5)
                val y0 = Dice.zeroTil(chunkRadius *2-5)
                if ((!(x0 in 32..chunkRadius *2-32) && !(y0 in 32 .. chunkRadius *2-32)) || Dice.chance(0.15f)) {
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
            val coasts = ArrayList<XY>()

            // Set coasts at all coast water
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
            // Fix any height=-1 cells that got missed somehow in sloping
            for (ix in 0 until chunkRadius *2) {
                for (iy in 0 until chunkRadius *2) {
                    if (scratches[ix][iy].height < 0) scratches[ix][iy].height = 1
                }
            }

            // Set mountaintops
            Console.sayFromThread("Raising mountains...")
            val peaks = ArrayList<XY>()
            forEachMeta { x,y,cell ->
                val height = cell.height
                if (height > minMountainHeight) {
                    if (!DIRECTIONS.hasOneWhere {
                        boundsCheck(x+it.x,y+it.y) && scratches[x+it.x][y+it.y].height > height
                        }) {
                        peaks.add(XY(x, y))
                        scratches[x][y].biome = Mountain
                    }
                }
            }

            // Freeze ice caps
            Console.sayFromThread("Freezing ice cap..")
            forEachMeta { x,y,cell ->
                if (y < 20 && cell.height == 0) {
                    cell.biome = Glacier
                }
            }
            for (y in 20..30) {
                for (x in 0 until chunkRadius *2) {
                    if ((scratches[x][y-1].biome == Glacier) && Dice.chance(1f - (y - 20) * 0.09f)) {
                        scratches[x][y].biome = Glacier
                    }
                }
            }

            // Run rivers from coast cells
            val mouths = mutableListOf<XY>()
            coasts.forEach { coast ->
                if (scratches[coast.x][coast.y].biome != Glacier) {
                    countRiverDescendants(coast)
                    if (scratches[coast.x][coast.y].riverDescendantCount > 30) {
                        if (Dice.chance(bigRiverDensity)) mouths.add(coast)
                    } else {
                        if (Dice.chance(smallRiverDensity)) mouths.add(coast)
                    }
                }
            }
            Console.sayFromThread("Running ${mouths.size} rivers from ${coasts.size} possible coasts...")
            mouths.forEach { runRiver(it, wiggle = 0.5f) }

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

            // Link peaks into mountain ranges
            peaks.shuffle()
            while (peaks.isNotEmpty()) {
                val peak = peaks.removeFirst()
                if (scratches[peak.x][peak.y].dryness < mountainRangeWetness) {
                    val connected = ArrayList<XY>()
                    peaks.forEach { otherPeak ->
                        if (distanceBetween(peak, otherPeak) < maxRangePeakDistance) {
                            connected.add(otherPeak)
                        }
                    }
                    connected.forEach { otherPeak ->
                        drawLine(peak, otherPeak) { x, y ->
                            scratches[x][y].biome = Mountain
                            if (boundsCheck(x + 1, y)) scratches[x + 1][y].biome = Mountain
                            if (boundsCheck(x, y + 1)) scratches[x][y + 1].biome = Mountain
                        }
                    }
                }
            }

            // Place cities
            val citiesTotal = citiesRiverMouth + citiesCoast + citiesRiver + citiesInland
            Console.sayFromThread("Building $citiesTotal ancient cities...")
            repeat (citiesRiverMouth) { placeCity(mouths) }
            repeat (citiesCoast) { placeCity(coasts) }
            repeat (citiesRiver) { placeCity(riverCells) }
            val inlandSites = ArrayList<XY>().apply {
                repeat(1000) {
                    val x = Dice.range(10, chunkRadius*2 - 10)
                    val y = Dice.range(10, chunkRadius*2 - 10)
                    if ((scratches[x][y].height > 0)) {
                        add(XY(x,y))
                    }
                }
            }
            repeat (citiesInland) { placeCity(inlandSites) }

            // Build ruins on city sites, and run roads away
            val numBigCities = citiesTotal * bigCityFraction
            var n = 0
            for (city in cityCells) {
                n++
                scratches[city.x][city.y].biome = Ruins
                if (n < numBigCities) {
                    DIRECTIONS.forEach { dir ->
                        if (Dice.chance(0.5f)) scratches[city.x + dir.x][city.y + dir.y].biome = Ruins
                    }
                }
                CARDINALS.forEach { dir ->
                    runRoad(city, dir)
                }
            }
            growBiome(Ruins, 1, 0.4f, false, listOf(Ocean))
            growBiome(Ruins, 2, 0.6f, true, listOf(Ocean))

            // Post-processing
            forEachMeta { x,y,cell ->
                if (cell.riverExits.isNotEmpty()) {
                    cell.riverBlur = 0.3f
                    cell.riverGrass = 0.8f
                    cell.riverDirt = 0.2f
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
                if (cell.biome == Blank) {
                    if (cell.height == 0) {
                        cell.biome = Ocean
                    } else if (cell.dryness >= (maxDry * 0.6f).toInt()) {
                        cell.biome = Desert
                    } else {
                        if (NoisePatches.get("metaForest", x, y) > 0.1f) {
                            cell.biome = Forest
                        } else {
                            cell.biome = Plain
                        }
                        cell.hasLake = Dice.chance(when (cell.riverExits.size) {
                            0 -> 0.02f
                            1 -> 0.3f
                            2 -> 0.06f
                            else -> 0.1f
                        })
                    }
                }
                if (cell.biome == Ruins) cell.roadExits.clear()
                // Add city distance
                cell.cityDistance = cityCells.minOf { distanceBetween(x,y,it.x,it.y) }
                // Add ruins
                val ruinousness = kotlin.math.max(0f, (1f - cell.cityDistance / ruinFalloff)) + cell.roadExits.size * 0.1f
                val minruins = Integer.max(0, (ruinousness * ruinsMax * 0.5f - 1.5f).toInt())
                cell.ruinedBuildings = Dice.range(minruins, (ruinousness * ruinsMax).toInt())
                if (cell.cityDistance <= 2f) cell.ruinedBuildings += 2
                if (Dice.chance(0.01f)) cell.ruinedBuildings += 1
                cell.ruinedBuildings += when (cell.biome) {
                    Mountain -> -1
                    Ruins -> -cell.ruinedBuildings
                    else -> 0
                }
                cell.ruinedBuildings = max(cell.ruinedBuildings, 0)
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

    private fun countRiverDescendants(mouth: XY) {
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

    private fun runRiver(cursor: XY, wiggle: Float) {
        riverCells.add(cursor)
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
                    val width = min(1 + (childCell.riverDescendantCount * riverWidth).toInt(), maxRiverWidth)
                    val edgeDir = XY(child.x - cursor.x, child.y - cursor.y)
                    val edgePos = randomChunkEdgePos(edgeDir, wiggle)
                    val myExit = RiverExit(
                        pos = edgePos,
                        edge = edgeDir,
                        control = edgePos + (edgeDir * -1 * Dice.range(4,20) + (edgeDir.rotated() * Dice.range(-12,12))),
                        width = width
                    )
                    val childEdgePos = flipChunkEdgePos(edgePos)
                    val childEdgeDir = XY(cursor.x - child.x, cursor.y - child.y)
                    val childExit = RiverExit(
                        pos = childEdgePos,
                        edge = childEdgeDir,
                        control = childEdgePos + (edgeDir * Dice.range(4,20)) + (edgeDir.rotated() * Dice.range(-12,12)),
                        width = width
                    )
                    cell.riverExits.add(myExit)
                    childCell.riverExits.add(childExit)
                    runRiver(child, wiggle)
                }
            }
        }
    }

    private fun placeCity(locations: List<XY>) {
        var placed = false
        while (!placed) {
            locations.random().also { site ->
                if (site.isFarEnoughFromAll(minCityDistance, cityCells)) {
                    var actualSite = site
                    if (!isLandAt(site)) {
                        DIRECTIONS.firstOrNull { isLandAt(XY(site.x + it.x, site.y + it.y)) }?.also { dir ->
                            actualSite = XY(site.x + dir.x, site.y + dir.y)
                        }
                    }
                    cityCells.add(actualSite)
                    scratches[actualSite.x][actualSite.y].hasCity = true
                    placed = true
                }
            }
        }
    }

    private fun runRoad(from: XY, toDir: XY) {
        var done = false
        var cursor = from
        var length = 0
        var dir = toDir
        while (!done) {
            val next = XY(cursor.x + dir.x, cursor.y + dir.y)
            if (!boundsCheck(next.x, next.y)) done = true
            else if (!isLandAt(next)) done = true
            else if (scratches[next.x][next.y].hasCity) done = true
            else if (scratches[cursor.x][cursor.y].roadExits.size > 2 && Dice.chance(0.7f)) done = true
            else if (length > 15 && Dice.chance(length * 0.006f)) done = true
            else {
                connectRoadExits(cursor, next)
                cursor = next
                length++
            }
            if (Dice.chance(0.07f)) {
                dir = XY(toDir.y, toDir.x)
                if (Dice.flip()) {
                    dir.x = -dir.x
                    dir.y = -dir.y
                }
            }
            if (Dice.chance(0.015f)) {
                runRoad(cursor, XY(toDir.y, toDir.x))
            }
        }
    }

    private fun isLandAt(xy: XY) = boundsCheck(xy.x, xy.y) && scratches[xy.x][xy.y].height > 0

    private fun connectRoadExits(source: XY, dest: XY) {
        val sourceEdge = XY(dest.x - source.x, dest.y - source.y)
        val destEdge = XY(source.x - dest.x, source.y - dest.y)
        if (!scratches[source.x][source.y].roadExits.hasOneWhere { it.edge == sourceEdge }) {
            scratches[source.x][source.y].roadExits.add(RoadExit(edge = sourceEdge))
        }
        if (!scratches[dest.x][dest.y].roadExits.hasOneWhere { it.edge == destEdge }) {
            scratches[dest.x][dest.y].roadExits.add(RoadExit(edge = destEdge))
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

    private fun growBiome(biome: Biome, threshold: Int, chance: Float, allDirections: Boolean, exclude: List<Biome> = listOf()) {
        val adds = ArrayList<XY>()
        forEachMeta { x, y, cell ->
            if (cell.biome != biome && !exclude.contains(cell.biome) && (biomeNeighbors(x, y, biome, allDirections) >= threshold)) {
                if (Dice.chance(chance)) adds.add(XY(x,y))
            }
        }
        adds.forEach {
            scratches[it.x][it.y].biome = biome
            if (scratches[it.x][it.y].height < 1) scratches[it.x][it.y].height = 1
        }
        adds.clear()
    }
}
