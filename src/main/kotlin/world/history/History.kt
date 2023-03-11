package world.history

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ktx.async.newSingleThreadAsyncContext
import util.Dice
import util.Madlib
import util.hasOneWhere
import world.gen.Metamap
import world.gen.features.Stronghold

@Serializable
class History {

    var year = 1994
    private val finalYear = 2994
    private val yearsPerTurn = 5

    // Number of comet shards in world at start
    var shardsLeft = 20
    private val shardFindChancePerTurn = 0.01f

    private val figures = mutableMapOf<Int, Figure>()
    private var figuresNextID = 0
    fun createFigure() = Figure(figuresNextID).apply { birthYear = year }.also { figures[figuresNextID++] = it }
    fun figure(id: Int) = figures[id]
    fun activeFigures() = figures.values.filter { it.alive }

    val empires = mutableMapOf<Int, Empire>()
    private var empiresNextID = 0
    fun createEmpire() = Empire(empiresNextID).apply { foundingYear = year }.also { empires[empiresNextID++] = it }
    fun empire(id: Int) = empires[id]
    fun activeEmpires() = empires.values.filter { it.active }

    @Transient private val strongholds = mutableListOf<Stronghold>()

    @Transient private val coroutineContext = newSingleThreadAsyncContext("History")
    @Transient private val coroutineScope = CoroutineScope(coroutineContext)

    suspend fun begin() {

        coroutineScope.launch {

            // Collect all strongholds
            findStrongholds()

            // 1000 years of glorious tradition
            while (year < finalYear) {
                year += yearsPerTurn
                doTurn()
                delay(1000L)
            }
        }
    }

    private fun doTurn() {

        // Check if a fragment is found; birth a wizard and empire
        maybeBirthWizard()

        // Empires accumulate power

        // Empires try to expand

    }

    private fun findStrongholds() {
        Metamap.forEachMeta { x,y,cell ->
            cell.features().firstOrNull { it is Stronghold }?.also { strongholds.add(it as Stronghold) }
        }
    }

    private fun maybeBirthWizard() {
        if (shardsLeft > 0) {
            if (Dice.chance(shardsLeft * shardFindChancePerTurn)) {
                birthWizard()
            }
        }
    }

    private fun birthWizard() {
        val wizard = createFigure().apply {
            name = Madlib.wizardName()
        }
        val empire = createEmpire().apply {
            leader = wizard.id
        }

    }
}
