package world.history

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.newSingleThreadAsyncContext
import util.Dice
import util.Madlib
import world.gen.Metamap
import world.gen.features.Stronghold

@Serializable
class History {
    var renderLocked = false

    var year = 1994
    private val finalYear = 2994
    private val yearsPerTurn = 5
    private val turnDelay = 50L

    // Number of comet shards in world at start
    var shardsLeft = 20
    private val shardFindChancePerTurn = 0.005f

    private val figures = mutableMapOf<Int, Figure>()
    private var figuresNextID = 0
    fun createFigure() = Figure(figuresNextID).apply { birthYear = year }.also { figures[figuresNextID++] = it }
    fun figure(id: Int) = figures[id]
    fun activeFigures() = figures.values.filter { it.alive }

    val empires = mutableMapOf<Int, Empire>()
    private var empiresNextID = 1  // 0 reserved for lords of light
    fun createEmpire() = Empire(empiresNextID, App.history.year).also { empires[empiresNextID++] = it }
    fun empire(id: Int) = empires[id]
    fun activeEmpires() = empires.values.filter { it.active }
    var lightEmpire: LightEmpire = LightEmpire().also { empires[0] = it }

    @Transient val strongholds = mutableListOf<Stronghold>()

    @Transient private val coroutineContext = newSingleThreadAsyncContext("History")
    @Transient private val coroutineScope = CoroutineScope(coroutineContext)

    suspend fun begin() {

        coroutineScope.launch {
            renderLocked = true

            // Collect all strongholds
            findStrongholds()

            // 1000 years of glorious tradition
            while (year < finalYear) {
                year += yearsPerTurn
                log("")
                doTurn()
                renderLocked = false
                delay(turnDelay)
                renderLocked = true
            }
        }
    }

    private fun doTurn() {
        // Age figures
        figures.values.forEach { figure ->
            if (figure.alive)
                figure.passYears(yearsPerTurn)
        }

        // Check if a fragment is found; birth a wizard and empire
        maybeBirthWizard()

        // Empires accumulate power

        // Empires try to expand
        empires.values.forEach { empire ->
            if (empire.active)
                empire.passYears(yearsPerTurn)
        }

    }

    private fun findStrongholds() {
        Metamap.forEachMeta { x,y,cell ->
            cell.features().firstOrNull { it is Stronghold }?.also { strongholds.add(it as Stronghold) }
        }
    }

    fun freeStrongholds() = strongholds.filter { it.empire == null }

    private fun maybeBirthWizard() {
        if (shardsLeft > 0) {
            if (Dice.chance(shardsLeft * shardFindChancePerTurn)) {
                birthWizard()
            }
        }
    }

    private fun birthWizard() {
        freeStrongholds().randomOrNull()?.also { firstStronghold ->
            val wizard = createFigure().apply {
                name = Madlib.wizardName()
                expectedLifespan = 300 + Dice.zeroTo(500)
            }
            log("The wizard ${wizard.name} appears in ${firstStronghold.name()}.")
            val empire = createEmpire().apply {
                leader = wizard.id
                addStronghold(firstStronghold)
            }
        }
    }

    fun log(message: String) {
        util.log.info("HISTORY: ($year) $message")
    }
}
