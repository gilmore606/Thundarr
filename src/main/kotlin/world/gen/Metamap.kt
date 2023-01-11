package world.gen

import App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ktx.async.newSingleThreadAsyncContext
import ui.panels.Console
import util.*
import world.*
import world.gen.biomes.Biome
import world.gen.biomes.*
import world.gen.biomes.Blank
import world.gen.habitats.*
import world.level.CHUNK_SIZE
import java.lang.Integer.max
import java.lang.Integer.min
import java.lang.Math.abs

object Metamap {

    private val chunkRadius = 100

    private val coroutineContext = newSingleThreadAsyncContext("Metamap")
    private val coroutineScope = CoroutineScope(coroutineContext)

    var isWorking = false

    val minLand = 0.55f
    val maxLand = 0.8f
    val coastRoughness = 0.15f
    val bigRiverDensity = 0.4f
    val smallRiverDensity = 0.1f
    val riverBranching = 0.25f
    val maxRiverWidth = 10
    val riverWidth = 0.15f
    val minMountainHeight = 20
    val maxRangePeakDistance = 3f
    val isolatedMountainDensity = 0.4f
    val mountainRangeWetness = 6
    val citiesRiverMouth = 6
    val citiesRiver = 8
    val citiesCoast = 8
    val citiesInland = 8
    val citiesDesert = 2
    val minCityDistance = 15
    val bigCityFraction = 0.2f
    val ruinFalloff = 14f
    val ruinsMax = 5f
    val minStepsBetweenSideRoads = 4
    val sideRoadChance = 0.2f
    val maxVolcanoes = 100

    val outOfBoundsMeta = ChunkMeta(biome = Ocean)

    private var scratches = Array(chunkRadius * 2) { Array(chunkRadius * 2) { ChunkScratch() } }
    private var riverCells = ArrayList<XY>()
    private var cityCells = ArrayList<XY>()
    private var roadCells = ArrayList<XY>()
    private var isolatedPeaks = ArrayList<XY>()
    private var volcanoPeaks = ArrayList<XY>()
    val metas = ArrayList<ArrayList<ChunkMeta>>(chunkRadius*2)
    var suggestedPlayerStart = XY(-999,-999)

    fun metaAt(x: Int, y: Int) = if (boundsCheck(x,y)) metas[x][y] else outOfBoundsMeta
    fun metaAtWorld(x: Int, y: Int): ChunkMeta {
        val cx = chunkXtoX(x)
        val cy = chunkYtoY(y)
        return metaAt(cx,cy)
    }

    fun boundsCheck(x: Int, y: Int): Boolean {
        if (x < 0 || y < 0 || x >= chunkRadius * 2 || y >= chunkRadius * 2) return false
        return true
    }

    fun xToChunkX(x: Int) = (x - chunkRadius) * CHUNK_SIZE
    fun yToChunkY(y: Int) = (y - chunkRadius) * CHUNK_SIZE
    fun chunkXtoX(x: Int) = (x / CHUNK_SIZE) + chunkRadius + (if (x < 0) -1 else 0)
    fun chunkYtoY(y: Int) = (y / CHUNK_SIZE) + chunkRadius + (if (y < 0) -1 else 0)

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
            metas.clear()
            for (x in 0 until chunkRadius *2) {
                val col = ArrayList<ChunkMeta>(chunkRadius*2)
                App.save.getWorldMetaColumn(xToChunkX(x)).forEach {
                    val i = chunkYtoY(it.y)
                    if (i >= 0 && i < chunkRadius*2) col.add(it)
                }
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
        roadCells.clear()
        isolatedPeaks.clear()
        volcanoPeaks.clear()

        fun metaAt(x: Int, y: Int): ChunkScratch? = if (boundsCheck(x,y)) scratches[x][y] else null

        isWorking = true
        coroutineScope.launch {

            // CONTINENT
            Console.sayFromThread("Breaking the moon in half...")
            var landFraction = 0f
            while (landFraction < minLand || landFraction > maxLand) {
                if (landFraction > 0f) Console.sayFromThread("Re-breaking -- insufficient " + (if (landFraction < minLand) "land" else "ocean") + "!")
                var landC = 0
                var totalC = 0
                for (ix in -chunkRadius until chunkRadius) {
                    for (iy in -chunkRadius until chunkRadius) {
                        val chunkX = ix * CHUNK_SIZE
                        val chunkY = iy * CHUNK_SIZE
                        scratches[ix + chunkRadius][iy + chunkRadius].apply {
                            x = chunkX
                            y = chunkY
                            height = -1
                        }
                    }
                }
                for (i in 0 until chunkRadius * 2) {
                    scratches[i][0].height = 0
                    scratches[i][chunkRadius * 2 - 1].height = 0
                    scratches[0][i].height = 0
                    scratches[chunkRadius * 2 - 1][i].height = 0
                }
                // Cut squares out of edge coast
                for (i in 0 until chunkRadius * 2) {
                    if (Dice.chance(coastRoughness)) {
                        val w = Dice.range(4, 25)
                        val h = Dice.range(4, 25)
                        val edge = CARDINALS.random()
                        val x0 = when (edge) {
                            NORTH, SOUTH -> i
                            EAST -> (chunkRadius * 2 - 1) - w
                            else -> 0
                        }
                        val x1 = when (edge) {
                            NORTH, SOUTH -> i + w
                            EAST -> chunkRadius * 2 - 1
                            else -> w
                        }
                        val y0 = when (edge) {
                            NORTH -> 0
                            SOUTH -> (chunkRadius * 2 - 1) - h
                            else -> i
                        }
                        val y1 = when (edge) {
                            NORTH -> h
                            SOUTH -> (chunkRadius * 2 - 1)
                            else -> i + h
                        }
                        for (x in x0 until x1) {
                            for (y in y0 until y1) {
                                if (boundsCheck(x, y) && !((x == x0 || x == x1 - 1) && (y == y0 || y == y1 - 1)))
                                    scratches[x][y].height = 0
                            }
                        }
                    }
                }
                // Cut square seas, preferably around the edge
                repeat(100) {
                    val x0 = Dice.zeroTil(chunkRadius * 2 - 5)
                    val y0 = Dice.zeroTil(chunkRadius * 2 - 5)
                    if ((!(x0 in 32..chunkRadius * 2 - 32) && !(y0 in 32..chunkRadius * 2 - 32)) || Dice.chance(0.15f)) {
                        var x1 = x0 + Dice.range(3, 12)
                        var y1 = y0 + Dice.range(3, 12)
                        if (Dice.chance(0.3f)) {
                            x1 = (x1 - x0) * (2 + Dice.oneTo(5)) + x0
                            y1 = (y1 - y0) * (2 + Dice.oneTo(5)) + y0
                        }
                        for (x in x0 until x1) {
                            for (y in y0 until y1) {
                                if (boundsCheck(x, y) && !((x == x0 || x == x1 - 1) && (y == y0 || y == y1 - 1)))
                                    scratches[x][y].height = 0
                            }
                        }
                    }
                }
                // Grow the seas
                for (density in listOf(0.2f, 0.6f, 0.1f, 0.5f, 0.6f, 0.2f, 0.2f)) {
                    forEachMeta { x, y, cell ->
                        if (cell.height == 0) {
                            CARDINALS.forEach { dir ->
                                if (boundsCheck(x + dir.x, y + dir.y)) {
                                    val neighbor = scratches[x + dir.x][y + dir.y]
                                    if ((neighbor.height == -1) && Dice.chance(density)) {
                                        neighbor.height = -2
                                        CARDINALS.forEach { ndir ->
                                            if (boundsCheck(x + dir.x + ndir.x, y + dir.y + ndir.y)) {
                                                val nn = scratches[x + dir.x + ndir.x][y + dir.y + ndir.y]
                                                if (Dice.chance(density * 0.7f)) nn.height = -2
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    forEachMeta { x, y, cell ->
                        if (cell.height == -2) cell.height = 0
                    }
                }
                forEachMeta { x, y, cell ->
                    if (cell.height != 0) landC++
                    totalC++
                }
                landFraction = landC.toFloat() / totalC.toFloat()
            }

            // SLOPES
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
                        DIRECTIONS.from(x,y) { dx, dy, _ ->
                            if (boundsCheck(dx, dy)) {
                                val neighbor = scratches[dx][dy]
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
                        if (abs(peak.x - otherPeak.x) < maxRangePeakDistance && abs(peak.y - otherPeak.y) < maxRangePeakDistance * 1.3f) {
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

            // Seed and grow deserts
            val desertSites = ArrayList<XY>()
            forEachMeta { x,y,cell ->
                if (cell.dryness > (maxDry * 0.6f)) desertSites.add(XY(x,y))
                if (cell.height > 1 && cell.biome == Blank && cell.riverExits.isEmpty() && cell.dryness >= (maxDry * 0.1f)) {
                    val mountains = biomeNeighbors(x,y,Mountain)
                    if (mountains > 1) {
                        val deserts = biomeNeighbors(x,y,Desert)
                        val chance = mountains * 0.06f + deserts * 0.3f
                        if (Dice.chance(chance)) {
                            cell.biome = Desert
                        }
                    }
                }
            }
            Evolver(chunkRadius*2, chunkRadius*2, false, { x,y ->
                boundsCheck(x,y) && scratches[x][y].biome == Desert
            }, { x,y ->
                if (boundsCheck(x,y) && scratches[x][y].biome != Mountain) {
                    scratches[x][y].biome = Desert
                }
            }, { x,y,n ->
                n > 0 && Dice.chance(0.6f)
            }).evolve(5)

            // Seed and grow swamps
            forEachMeta { x,y,cell ->
                if (y > 50 && cell.height > 0 && cell.height < 13 && cell.biome == Blank) {
                    var chance = cell.riverExits.size * 0.015f +
                            (if (cell.dryness < 7) 0.005f else 0f)
                    chance *= 1f + (y * 0.0004f)
                    if (Dice.chance(chance)) {
                        if (NoisePatches.get("metaVariance2", x, y) > 0.4f) {
                            cell.biome = Swamp
                        }
                    }
                }
            }
            Evolver(chunkRadius*2, chunkRadius*2, true, { x,y ->
                boundsCheck(x,y) && scratches[x][y].biome == Swamp
            }, { x,y ->
                if (boundsCheck(x,y) && scratches[x][y].biome == Blank && scratches[x][y].height > 0) {
                    scratches[x][y].biome = Swamp
                }
            }, { x,y,n ->
                n > 0 && Dice.chance(0.3f)
            }).evolve(3)
            Evolver(chunkRadius*2, chunkRadius*2, false, { x,y ->
                boundsCheck(x,y) && scratches[x][y].biome == Swamp
            }, { x,y ->
                if (boundsCheck(x,y) && scratches[x][y].biome == Blank && scratches[x][y].height > 0) {
                    scratches[x][y].biome = Swamp
                }
            }, { x,y,n ->
                n > 0 && Dice.chance(0.7f)
            }).evolve(1)

            // Place cities
            val citiesTotal = citiesRiverMouth + citiesCoast + citiesRiver + citiesInland + citiesDesert
            Console.sayFromThread("Building $citiesTotal ancient cities...")
            repeat (citiesRiverMouth) { placeCity(mouths) }
            repeat (citiesCoast) { placeCity(coasts) }
            repeat (citiesRiver) { placeCity(riverCells) }
            repeat (citiesDesert) { placeCity(desertSites) }
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

            // Build ruins on city sites
            val numBigCities = citiesTotal * bigCityFraction
            var n = 0
            for (city in cityCells) {
                n++
                scratches[city.x][city.y].biome = Ruins
                if (n < numBigCities) {
                    DIRECTIONS.from(city.x, city.y) { dx, dy, _ ->
                        if (Dice.chance(0.5f)) {
                            scratches[dx][dy].biome = Ruins
                            scratches[dx][dy].height = 1
                        }
                    }
                }
            }
            growBiome(Ruins, 1, 0.4f, false, listOf(Ocean))
            growBiome(Ruins, 2, 0.6f, true, listOf(Ocean))

            // Biomes pass 1 -- dry deserts, grow forests on plains, remove some isolated mountains
            forEachMeta { x,y,cell ->
                if (cell.biome == Blank) {
                    if (cell.height == 0) cell.biome = Ocean
                    else if (cell.dryness >= (maxDry * 0.6f).toInt()) {
                        cell.biome = Desert
                    } else if (NoisePatches.get("metaForest", x, y) > 0.04f + (cell.dryness * 0.08f) + max(0, 80 - y) * 0.005f) {
                        cell.biome = Forest
                    } else {
                        cell.biome = Plain
                    }
                } else if (cell.biome == Mountain) {
                    if (biomeNeighbors(x,y,Mountain,true) == 0) {
                        if (!Dice.chance(isolatedMountainDensity)) {
                            cell.biome = Plain
                        } else {
                            isolatedPeaks.add(XY(x,y))
                            if (biomeNeighbors(x,y,Desert,true) > 4) {
                                volcanoPeaks.add(XY(x,y))
                            }
                        }
                    }
                }
            }

            // Volcanoes
            if (volcanoPeaks.isNotEmpty()) {
                var eruptions = 0
                repeat(maxVolcanoes) {
                    val volcano = volcanoPeaks.random()
                    if (!scratches[volcano.x][volcano.y].hasVolcano) {
                        scratches[volcano.x][volcano.y].hasVolcano = true
                        CARDINALS.forEach { dir ->
                            if (Dice.chance(0.85f)) digLavaFlow(volcano, dir, Dice.float(4f, 7f))
                        }
                        eruptions++
                        suggestedPlayerStart.x = xToChunkX(volcano.x)
                        suggestedPlayerStart.y = yToChunkY(volcano.y)
                    }
                }
                Console.sayFromThread("Erupted $eruptions volcanoes in desert peaks.")
            }

            // Biomes pass 2 - insert intermediate biomes
            forEachMeta { x,y,cell ->
                when (cell.biome) {
                    Plain -> {
                        if (biomeNeighbors(x,y,Mountain,true) > 0) cell.biome = Hill
                        if (biomeNeighbors(x,y,Desert, true) > 0) cell.biome = Scrub
                        if (biomeNeighbors(x,y,Ruins, true) > 0) cell.biome = Suburb
                        if ((biomeNeighbors(x,y,Forest,true) == 0) && Dice.chance(0.01f)) cell.biome = Scrub
                    }
                    else -> { }
                }
            }
            repeat (3) { growBiome(Scrub, 1, 0.5f, false) { x,y,cell ->
                cell.biome == Plain } }
            growBiome(Suburb, 2, 0.5f, true) { x,y,cell ->
                cell.biome == Plain }

            // Biomes pass 3 - forest some hills, add desert oases
            forEachMeta { x,y,cell ->
                when (cell.biome) {
                    Hill -> {
                        if (biomeNeighbors(x, y, Forest, true) > 0) cell.biome = ForestHill
                        else if (NoisePatches.get("metaForest", x, y) > 0.02f + (cell.dryness * 0.08f)) cell.biome = ForestHill
                    }
                    Forest -> {
                        if (Dice.chance(0.01f)) cell.biome = ForestHill
                        else if (biomeNeighbors(x, y, Mountain) > 1 && Dice.chance(0.5f)) cell.biome = ForestHill
                    }
                    Plain -> {
                        if (biomeNeighbors(x, y, Plain, true) == 8 && Dice.chance(0.01f)) cell.biome = if (Dice.flip()) Hill else ForestHill
                    }
                    Desert -> {
                        if (biomeNeighbors(x, y, Desert, true) == 8 && Dice.chance(0.01f)) {
                            cell.biome = if (Dice.flip()) Plain else Scrub
                            cell.hasLake = true
                        }
                    }
                    else -> { }
                }
            }
            growBiome(ForestHill, 1, 0.5f, true) { x,y,cell ->
                cell.biome == Hill || cell.biome == Plain
            }

            // Clean up biome collisions
            forEachMeta { x, y, cell ->
                when (cell.biome) {
                    Swamp -> {
                        if (biomeNeighbors(x,y,Swamp,false) < 1) cell.biome = Plain
                    }
                    Forest -> {
                        if (biomeNeighbors(x,y,Desert, true) > 0) cell.biome = Plain
                        if (biomeNeighbors(x,y,Mountain,false) > 0) cell.biome = ForestHill
                    }
                    Plain -> {
                        if (biomeNeighbors(x,y,Swamp, false) == 4) cell.biome = Swamp
                        if (biomeNeighbors(x,y,Desert, false) == 4) cell.biome = Desert
                    }
                    Desert -> {
                        if (cell.riverExits.isNotEmpty()) cell.biome = if (Dice.flip()) Plain else Scrub
                    }
                    else -> { }
                }
            }

            // Post-processing
            forEachMeta { x,y,cell ->
                if (cell.riverExits.isNotEmpty()) {
                    cell.riverBlur = 0.3f
                }
                // Set coasts
                if (cell.height > 0) {
                    DIRECTIONS.from(x, y) { dx, dy, dir ->
                        metaAt(dx, dy)?.also { neighbor ->
                            if (neighbor.height == 0) {
                                cell.coasts.add(dir)
                            }
                        }
                    }
                    val adds = ArrayList<XY>()
                    cell.coasts.forEach { when (it) {
                        NORTH -> { adds.add(NORTHWEST) ; adds.add(NORTHEAST) }
                        SOUTH -> { adds.add(SOUTHWEST) ; adds.add(SOUTHEAST) }
                        WEST -> { adds.add(NORTHWEST) ; adds.add(SOUTHWEST) }
                        EAST -> { adds.add(NORTHEAST) ; adds.add(SOUTHEAST) }
                    } }
                    adds.forEach { if (!cell.coasts.contains(it)) cell.coasts.add(it) }
                }
                // Add lake
                if (cell.biome.canHaveLake()) {
                    cell.hasLake = Dice.chance(when (cell.riverExits.size) {
                        0 -> 0.02f
                        1 -> 0.3f
                        2 -> 0.06f
                        else -> 0.1f
                    })
                }

                // Add ruined buildings around cities
                cell.cityDistance = cityCells.minOf { distanceBetween(x,y,it.x,it.y) }
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

            // Set temperatures
            forEachMeta { x,y,cell ->
                var temp = 10 + (y * 0.5f) + NoisePatches.get("metaVariance2", x, y) * 15
                when (cell.biome) {
                    Ocean -> temp += 10
                    Desert -> {
                        if (biomeNeighbors(x,y,Desert,true) == 8) temp += Dice.zeroTo(10) + (y/6) else temp += (y/10)
                    }
                    Mountain -> {
                        if (biomeNeighbors(x,y,Mountain,false) > 2) temp -= 20 + Dice.zeroTo(10) else temp -= 10
                    }
                }
                temp -= cell.height / 2
                if (biomeNeighbors(x,y,Ocean,false) > 0) temp += -10 + (y / 7)

                cell.temperature = temp.toInt()
            }
            repeat (3) {
                forEachMeta { x, y, cell ->
                    var total = 0
                    DIRECTIONS.from(x, y) { x, y, _ -> if (boundsCheck(x, y)) total += scratches[x][y].temperature }
                    total = total / 8
                    cell.temperature = (cell.temperature + total) / 2
                }
            }

            // Distribute habitats
            forEachMeta { x,y,cell ->
                if (cell.biome != Ocean) {
                    cell.habitat = if (cell.temperature < 30) Arctic
                        else if (cell.temperature < 50) AlpineA
                        else if (cell.temperature < 75) TemperateA
                        else TropicalA
                }
            }
            forEachMeta { x,y,cell ->
                if (NoisePatches.get("metaHabitats", x, y) > 0.5f) {
                    when (cell.habitat) {
                        AlpineA -> cell.habitat = AlpineB
                        TemperateA -> cell.habitat = TemperateB
                        TropicalA -> cell.habitat = TropicalB
                        else -> {}
                    }
                }
            }

            // Run trails
            forEachMeta { x,y,cell ->
                if (Dice.chance(cell.biome.trailChance())) {
                    runTrail(XY(x,y), Dice.float(0.05f, 0.2f))
                }
            }

            // Run roads
            cityCells.forEach { city ->
                cityCells.forEach { ocity ->
                    if (ocity != city) connectCityToRoads(city, ocity)
                }
            }

            // Name contiguous features
            Console.sayFromThread("Naming geography...")
            val areaMap = Array(chunkRadius*2) { Array(chunkRadius*2) { 0 } }
            fun floodFill(x: Int, y: Int, id: Int, biome: Biome): Int {
                var cells = 1
                areaMap[x][y] = id
                DIRECTIONS.from(x,y) { dx, dy, dir ->
                    if (boundsCheck(dx,dy) && areaMap[dx][dy] == 0 && scratches[dx][dy].biome == biome) {
                        cells += floodFill(dx,dy,id,biome)
                    }
                }
                return cells
            }
            class BiomeArea(val areaID: Int, val size: Int)
            val areas = mutableMapOf<Biome,ArrayList<BiomeArea>>()
            var areaID = 1
            forEachMeta { x,y,cell ->
                if (areaMap[x][y] == 0) {
                    val biome = scratches[x][y].biome
                    if (listOf(Ruins, Suburb, Desert, Mountain, Swamp).contains(biome)) {
                        val size = floodFill(x, y, areaID, biome)
                        if (areas.containsKey(biome)) {
                            areas[biome]?.add(BiomeArea(areaID, size))
                        } else {
                            areas[biome] = ArrayList<BiomeArea>().apply { add(BiomeArea(areaID, size)) }
                        }
                        areaID++
                    }
                }
            }
            fun nameArea(areaID: Int, name: String) {
                forEachMeta { x,y,cell -> if (areaMap[x][y] == areaID) cell.title = name }
            }
            areas[Ruins]?.sortByDescending { it.size }
            areas[Ruins]?.forEachIndexed { n, area ->
                nameArea(area.areaID, if (n < 6) Madlib.bigCityName() else Madlib.smallCityName())
            }
            areas[Swamp]?.forEachIndexed { n, area ->
                nameArea(area.areaID, Madlib.swampName())
            }
            areas[Desert]?.sortByDescending { it.size }
            areas[Desert]?.forEachIndexed { n, area ->
                if (n < 12) nameArea(area.areaID, Madlib.desertName())
            }
            areas[Mountain]?.sortByDescending { it.size }
            areas[Mountain]?.forEachIndexed { n, area ->
                if (n < 30) nameArea(area.areaID, Madlib.mountainRangeName())
            }
            areas[Forest]?.sortByDescending { it.size }
            areas[Forest]?.forEachIndexed { n, area ->
                if (n < 10) nameArea(area.areaID, Madlib.forestName())
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

    private fun digLavaFlow(start: XY, startDir: XY, startWidth: Float) {
        val branchChance = 0.4f
        val cursor = XY(start.x,start.y)
        val dir = XY(startDir.x,startDir.y)
        var width = startWidth
        var done = false
        while (!done) {
            val next = XY(cursor.x + dir.x, cursor.y + dir.y)
            if (scratches[next.x][next.y].height < 1) done = true
            if (scratches[next.x][next.y].riverExits.isNotEmpty()) done = true
            if (scratches[next.x][next.y].lavaExits.isNotEmpty()) done = true
            if (!done) {
                val edgePos = randomChunkEdgePos(dir, 0.8f)
                val myExit = LavaExit(
                    pos = edgePos,
                    edge = dir,
                    width = width.toInt()
                )
                val childEdgePos = flipChunkEdgePos(edgePos)
                val childDir = XY(-dir.x, -dir.y)
                val childExit = LavaExit(
                    pos = childEdgePos,
                    edge = childDir,
                    width = width.toInt()
                )
                scratches[cursor.x][cursor.y].hasLake = false
                scratches[cursor.x][cursor.y].lavaExits.add(myExit)
                scratches[next.x][next.y].lavaExits.add(childExit)
                cursor.x = next.x
                cursor.y = next.y
                width -= Dice.float(1f, 2.5f)
                if (width <= 1f) done = true
                if (Dice.chance(branchChance * (width / 5f))) {
                    val branchDir = if (Dice.flip()) dir.rotated() else dir.rotated().flipped()
                    digLavaFlow(cursor, branchDir, width * 0.7f)
                }
            } else {
                if (scratches[cursor.x][cursor.y].lavaExits.isNotEmpty()) scratches[cursor.x][cursor.y].lavaExits.last().width = 1
            }
        }
    }

    private fun runTrail(cursor: XY, turnChance: Float) {
        var done = false
        var direction = CARDINALS.random()
        while (!done) {
            if (!boundsCheck(cursor.x + direction.x, cursor.y + direction.y)) done = true
            else {
                val cell = scratches[cursor.x][cursor.y]
                val childCell = scratches[cursor.x + direction.x][cursor.y + direction.y]
                val edgePos = randomChunkEdgePos(direction, 0.8f)
                val myExit = TrailExit(
                    pos = edgePos,
                    edge = direction,
                    control = edgePos + (direction * -1 * Dice.range(8, 25) + (direction.rotated() * Dice.range(-12, 12)))
                )
                val childEdgePos = flipChunkEdgePos(edgePos)
                val childDirection = XY(-direction.x, -direction.y)
                val childExit = TrailExit(
                    pos = childEdgePos,
                    edge = childDirection,
                    control = childEdgePos + (direction * Dice.range(8, 25) + (direction.rotated() * Dice.range(-12, 12)))
                )
                cell.trailExits.add(myExit)
                childCell.trailExits.add(childExit)
                if (childCell.height < 1 || Dice.chance(0.05f)) done = true
                if (childCell.trailExits.size > 1) done = true
                if (childCell.biome.trailChance() <= 0f) done = true
                cursor.x += direction.x
                cursor.y += direction.y
                if (Dice.chance(turnChance)) {
                    direction = CARDINALS.toMutableList().apply { remove(direction) }.random()
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

    private fun connectCityToRoads(city: XY, targetCity: XY) {
        val cursor = XY(city.x, city.y)
        val targets = ArrayList<XY>().apply {
            addAll(cityCells)
            addAll(roadCells)
            remove(city)
        }
        val newRoads = ArrayList<XY>()
        var done = false
        var stepsSinceSideRoad = 0
        while (!done) {
            // Find target closest to cursor which is closer to targetCity than cursor
            var nearestTarget = targetCity
            val cursorToTargetCity = manhattanDistance(cursor, targetCity)
            var nearestTargetDistance = cursorToTargetCity
            targets.forEach { choice ->
                val cursorToChoice = manhattanDistance(cursor, choice)
                val choiceToTargetCity = manhattanDistance(choice, targetCity)
                if (cursorToChoice < 0.5f) done = true
                else if (choiceToTargetCity < cursorToTargetCity) {
                    if (cursorToChoice < nearestTargetDistance) {
                        nearestTargetDistance = cursorToChoice
                        nearestTarget = choice
                    }
                }
            }
            if (!done) {
                // Pick a direction that moves closer to it
                var moveDir = XY(0, 0)
                val possDirs = ArrayList<XY>()
                CARDINALS.from(cursor.x, cursor.y) { dx, dy, dir ->
                    if (boundsCheck(dx, dy) && scratches[dx][dy].biome != Mountain) {
                        if (!newRoads.contains(XY(dx,dy))) {
                            possDirs.add(dir)
                        }
                    }
                }
                possDirs.shuffled().from(cursor.x, cursor.y) { dx, dy, dir ->
                    if (manhattanDistance(nearestTarget.x, nearestTarget.y, dx, dy) < nearestTargetDistance) {
                        moveDir = dir
                    }
                }
                if (moveDir == XY(0,0)) {
                    if (possDirs.isEmpty()) done = true else moveDir = possDirs.random()
                }
                if (!done) {
                    connectRoadExits(cursor, XY(cursor.x + moveDir.x, cursor.y + moveDir.y))
                    if (cursor != city) newRoads.add(XY(cursor.x, cursor.y))
                    stepsSinceSideRoad++
                    if (stepsSinceSideRoad >= minStepsBetweenSideRoads && Dice.chance(sideRoadChance)) {
                        var sideRoadDir = moveDir.rotated()
                        if (Dice.flip()) sideRoadDir = sideRoadDir.flipped()
                        runSideRoad(XY(cursor.x, cursor.y), sideRoadDir)
                        stepsSinceSideRoad = 0
                    }
                    cursor.x += moveDir.x
                    cursor.y += moveDir.y
                }
            }
        }
        roadCells.addAll(newRoads)
    }

    private fun runSideRoad(start: XY, dir: XY) {
        var done = false
        val cursor = XY(start.x,start.y)
        var currentDir = XY(dir.x, dir.y)
        while (!done) {
            if (Dice.chance(0.05f)) done = true
            val next = XY(cursor.x + currentDir.x, cursor.y + currentDir.y)
            if (scratches[next.x][next.y].roadExits.isNotEmpty()) done = true
            else if (scratches[next.x][next.y].height < 1) done = true
            else if (scratches[next.x][next.y].biome == Mountain) {
                if (Dice.chance(0.2f)) done = true
                else {
                    currentDir = currentDir.rotated()
                    if (Dice.flip()) currentDir = currentDir.flipped()
                }
            } else {
                connectRoadExits(cursor, next)
                roadCells.add(XY(cursor.x, cursor.y))
                cursor.x = next.x
                cursor.y = next.y
            }
        }
    }

    private fun isLandAt(xy: XY) = boundsCheck(xy.x, xy.y) && scratches[xy.x][xy.y].height > 0

    private fun connectRoadExits(source: XY, dest: XY) {
        if (scratches[dest.x][dest.y].biome == Ocean) return
        if (scratches[dest.x][dest.y].height < 1) return
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
        (if (allDirections) DIRECTIONS else CARDINALS).from(x, y) { dx, dy, dir ->
            if (boundsCheck(dx, dy) &&
                scratches[dx][dy].biome == biome) {
                c++
            }
        }
        return c
    }

    private fun growBiome(biome: Biome, threshold: Int, chance: Float, allDirections: Boolean, exclude: List<Biome> = listOf()) {
        val adds = ArrayList<XY>()
        forEachMeta { x, y, cell ->
            if (cell.biome != biome && cell.height > 0 && !exclude.contains(cell.biome) && (biomeNeighbors(x, y, biome, allDirections) >= threshold)) {
                if (Dice.chance(chance)) adds.add(XY(x,y))
            }
        }
        adds.forEach {
            scratches[it.x][it.y].biome = biome
            if (scratches[it.x][it.y].height < 1 && biome != Ocean) scratches[it.x][it.y].height = 1
        }
        adds.clear()
    }

    private fun growBiome(biome: Biome, threshold: Int, chance: Float, allDirections: Boolean, cellOK: (x: Int, y: Int, cell: ChunkScratch)->Boolean) {
        val adds = ArrayList<XY>()
        forEachMeta { x, y, cell ->
            if (cell.biome != biome && cell.height > 0 && cellOK(x,y,cell) && (biomeNeighbors(x, y, biome, allDirections) >= threshold)) {
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
