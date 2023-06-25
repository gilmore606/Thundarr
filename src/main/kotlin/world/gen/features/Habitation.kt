package world.gen.features

import actors.Citizen
import actors.Villager
import actors.factions.HabitationFaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.Madlib
import util.XY
import world.ChunkScratch
import world.gen.decors.*
import world.level.Level

@Serializable
sealed class Habitation : Feature() {

    override fun trailDestinationChance() = 1f

    @Serializable
    enum class Flavor(
        val displayName: String,
        @Transient val neighborFeatures: List<Triple<Float, (ChunkScratch, Village)->Boolean, (Boolean, XY)->Feature>> = listOf(
            Triple(0.6f, { meta, village -> Farm.canBuildOn(meta) }, { isAbandoned, dir -> Farm(isAbandoned) }),
            Triple(0.3f, { meta, village -> Graveyard.canBuildOn(meta) }, { isAbandoned, dir -> Graveyard(isAbandoned) }),
            Triple(0.3f, { meta, village -> village.size > 8 && !village.isAbandoned && Tavern.canBuildOn(meta) },
                { isAbandoned, dir -> Tavern(Madlib.tavernName(), dir) }),
        ),
        @Transient val shopDecors: MutableList<Decor> = mutableListOf(
            BlacksmithShop(),
            Schoolhouse(),
            Church(),
            StorageShed(),
        ),
        val homeComments: Set<String> = setOf(
            "Ah, home and hearth.",
            "It's good to be home.",
            "It's not much, but it's my safe place.",
            "A villager's home is a castle.",
            "Home is where the heart is."
        ),
        val childChance: Float = 0.4f,
        val namePrefix: String = "",
    ) {

        HUMAN("human"),

        HERMIT("hermit",
            homeComments = setOf(
                "Sometimes I talk to myself, so I don't forget how.",
                "There's only one person you can really trust.",
                "I must do my penance in solitude.",
            ),
            childChance = 0f,
            shopDecors = mutableListOf(
                MeditationSpot(),
                MeditationSpot(),
                MeditationSpot(),
                MeditationSpot()
            ),
            namePrefix = "hermit ",
        ),

        HUNTER("hunter",
            homeComments = setOf(
                "I live off the land, as the Lords of Light intended.",
                "Life's not easy out here, but it's free.",
                "Hunting and foraging makes a body powerful hungry."
            ),
            shopDecors = mutableListOf(
                HuntingGround(),
                HuntingGround(),
                HuntingGround(),
                HuntingGround()
            )
        ),

        TAVERN("tavern",
            homeComments = setOf(
                "Drinking helps me cope.",
                "Have an ale, it'll take the edge off."
            ),
            childChance = 0f
        ),

        FARM("farm",
            homeComments = setOf(
                "Tilling the soil brings new life."
            ),
            childChance = 0.5f
        ),

        THRALL("thrall",
            neighborFeatures = listOf(
                Triple(0.6f, { meta, village -> Farm.canBuildOn(meta) }, { isAbandoned, dir -> Farm(isAbandoned) }),
                Triple(0.3f, { meta, village -> Graveyard.canBuildOn(meta) }, { isAbandoned, dir -> Graveyard(isAbandoned) }),
            ),
            homeComments = setOf(
                "We just want to be left alone.",
                "This miserable hovel is all the wizard allows us.",
                "It's humble but it's all we have.",
                "At least I can rest from wizard's labours, for a little while."
            ),
            childChance = 0.1f
        ),
    }

    protected val citizens = mutableSetOf<String>() // actor ids
    protected fun addCitizen(citizen: Citizen) {
        citizens.add(citizen.id)
        citizen.habitation = this
    }

    var factionID: String? = null
        get() = if (field == null) {
            field = App.factions.addFaction(HabitationFaction(name(), flavor()))
            field
        } else field

    val workAreas = mutableSetOf<Villager.WorkArea>()

    abstract fun flavor(): Flavor

    override fun onRestore(level: Level) {
        citizens.forEach { citizenID ->
            level.director.getActor(citizenID)?.also { citizen ->
                if (citizen is Citizen) {
                    citizen.habitation = this
                }
            }
        }
    }

}
