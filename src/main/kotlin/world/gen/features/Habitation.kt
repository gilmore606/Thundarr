package world.gen.features

import actors.Citizen
import actors.Villager
import actors.factions.HabitationFaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import util.*
import world.ChunkScratch
import world.gen.cartos.WorldCarto
import world.gen.decors.*
import world.level.Level
import world.lore.DirectionsLore
import world.lore.Lore
import world.lore.MoonLore1
import world.lore.WizardLore1
import world.quests.Quest

@Serializable
sealed class Habitation(
    val isAbandoned: Boolean
) : Feature() {

    open fun numberOfLoreHavers() = 0
    open fun numberOfQuestsDesired() = 0  // Return >0 to have quests with an NPC here as source
    val questIDsAsSource = mutableListOf<String>()

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
        ) {
            override fun restTime() = DayTime(23, Dice.range(0, 30))
            override fun sleepTime() = DayTime(23, Dice.range(35, 45))
            override fun wakeTime() = DayTime.betweenHoursOf(8, 9)
            override fun workTime() = DayTime.betweenHoursOf(9, 11)
          },

        INNKEEPER("innkeeper",
            homeComments = setOf(
                "Drinking helps me cope.",
                "Have an ale, it'll take the edge off."
            ),
            childChance = 0f
        ) {
            override fun restTime() = DayTime(23, 35)
            override fun sleepTime() = DayTime(23,50)
            override fun wakeTime() = DayTime(4, 30)
            override fun workTime() = DayTime(5, 0)
          },

        FARM("farm",
            homeComments = setOf(
                "Tilling the soil brings new life.",
                "Farm work is hard work, but it's honest.",
                "The Lords make the plants grow; I only fulfill their work."
            ),
            childChance = 0.5f,
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
        );

        open fun restTime() = DayTime.betweenHoursOf(17, 20)
        open fun sleepTime() = DayTime.betweenHoursOf(21, 23)
        open fun wakeTime() = DayTime.betweenHoursOf(4, 5)
        open fun workTime() = DayTime.betweenHoursOf(6, 8)
    }

    protected val skinSet = Villager.skinSets.random()
    protected val citizens = mutableSetOf<String>() // actor ids
    protected fun addCitizen(citizen: Citizen) {
        citizens.add(citizen.id)
        citizen.habitation = this
        if (citizen is Villager) {
            val skin = if (Dice.chance(0.8f)) skinSet.random() else Villager.allSkins.random()
            citizen.setSkin(skin)
        }
    }

    protected fun placeCitizen(citizen: Citizen, spawnRect: Rect, homeArea: Villager.WorkArea? = null,
                               fulltimeJobArea: Villager.WorkArea? = null) {
        factionID?.also { citizen.joinFaction(it) }
        homeArea?.also { if (citizen is Villager) citizen.homeArea = it }
        fulltimeJobArea?.also { if (citizen is Villager) citizen.fulltimeJobArea = it }
        addCitizen(citizen)
        findSpawnPointForNPC(chunk, citizen, spawnRect)?.also { spawnPoint ->

            citizen.spawnAt(App.level, spawnPoint.x, spawnPoint.y)

            questsNeedingGiver.randomOrNull()?.also { quest ->
                if (quest.couldBeGivenBy(citizen) && citizen.couldGiveQuest(quest)) {
                    quest.onGiverSpawn(citizen)
                    questsNeedingGiver.remove(quest)
                    log.info("  assigned $quest to $citizen")
                }
            }

        } ?: run { log.warn("Failed to spawn citizen in ${spawnRect}") }
    }

    var factionID: String? = null
        get() = if (field == null) {
            field = App.factions.addFaction(HabitationFaction(name(), flavor()))
            field
        } else field
    fun faction() = factionID?.let { App.factions.byID(it) }

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

    @Transient val questsAsSource = mutableListOf<Quest>()
    @Transient val questsNeedingGiver = mutableListOf<Quest>()

    fun addQuestAsSource(quest: Quest) {
        faction()?.also { faction ->
            faction.addQuest(quest)
            questIDsAsSource.add(quest.id)
        } ?: run { log.warn("Tried to add quest to $this but faction() was null!") }
    }

    override fun dig(carto: WorldCarto) {
        questIDsAsSource.forEach { questID ->
            App.factions.questByID(questID)?.also { quest ->
                questsAsSource.add(quest)
                if (quest.needsGiver()) {
                    questsNeedingGiver.add(quest)
                }
            }
        }
        super.dig(carto)
        addLore()
        questsAsSource.forEach { quest ->
            quest.digSource(this)
        }
    }

    private fun addLore() {
        val lores = mutableListOf<Lore>().apply {
            addAll(Lore.static)
            add(DirectionsLore(XY(worldX, worldY)))
        }
        repeat (numberOfLoreHavers()) {
            if (lores.isNotEmpty()) {
                val newLore = lores.random()
                lores.remove(newLore)
                var tries = 20
                while (tries > 0) {
                    tries--
                    citizens.randomOrNull()?.also { citizenID ->
                        App.level.director.getActor(citizenID)?.also { citizen ->
                            if ((citizen as Citizen).couldHaveLore()) {
                                citizen.lore.add(newLore)
                                tries = 0
                            }
                        }
                    }
                }
            }
        }
    }
}
