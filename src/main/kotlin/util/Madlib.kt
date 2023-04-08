package util

import world.Entity

object Madlib {

    fun escapeReason(): String {
        val pres = listOf(
            "Thanks to",
            "With the help of",
            "Using only",
            "With just",
        )
        val helps = listOf(
            "a pilfered fork from the cafeteria",
            "a stolen bit of wire",
            "a helpful scullery maid",
            "a sympathetic guard",
            "my keen sense of smell",
            "a forgetful overseer",
            "a distracted slavemaster",
            "a temporary power outage",
            "a fight in the guardhouse",
            "a convenient distraction",
            "some curious squirrels",
            "very strange weather",
            "a sleeping guard",
            "a handmade lockpick",
            "prayers to the Lords of Light",
            "my keen sense of direction",
            "my barbarian strength",
            "my barbarian will",
            "a complicated lie to the head warlock"
        )
        var h = pres.random() + " "
        if (Dice.chance(0.3f)) {
            return h + helps.random()
        } else {
            return h + helps.random() + " and " + helps.random()
        }
    }

    fun wizardNameWord(gender: Entity.Gender = Entity.Gender.NEUTER): String {
        val syl1 = listOf(
            "Mo",
            "Mol",
            "Gran",
            "Gan",
            "Men",
            "Man",
            "Sar",
            "Zar",
            "Ne",
            "Zan",
            "Gro",
            "Kro",
            "Bul",
            "Ha",
            "He",
            "Ko",
            "Ka",
            "Kel",
            "Lo",
            "Ho",
            "Sor",
            "Sa",
            "Si",
            "Za",
            "Ze",
            "Zol",
            "Bel",
            "Hell",
            "Ou",
            "A",
            "E",
            "O",
            "Yee",
            "Ye",
            "U",
            "Ul",
        )
        val conn = listOf(
            "ba",
            "sa",
            "ta",
            "to",
            "zo",
            "da",
            "ga",
            "ti",
            "da",
            "ma",
            "zi",
            "za",
            "-",
            "ton",
            "tar",
            "kar",
            "kal",
            "ka",
            "ni",
            "'",
        )
        val syl2 = listOf(
            "tar",
            "th",
            "sh",
            "star",
            "stro",
            "gan",
            "gar",
            "to",
            "do",
            "dan",
            "tok",
            "tak",
            "lok",
            "tan",
            "zan",
            "zar",
            "bar",
            "tani",
            "tano",
            "ni",
            "no",
            "ko",
            "dar",
            "llo",
            "lli",
            "rra",
            "rro",
            "d",
            "m",
            "mm",
            "n",
            "nn",
            "lio",
            "lar",
            "tron",
            "zoid",
            "roid",
            "zar",
            "zan"
        )

        var name = ""
        if (Dice.chance(0.4f)) {
            name = syl1.random() + conn.random() + syl2.random()
        } else {
            name = syl1.random() + syl2.random()
        }
        return name
    }

    fun wizardName(gender: Entity.Gender = Entity.Gender.NEUTER): String {
        var name = wizardNameWord(gender)
        if (Dice.chance(0.7f - name.length * 0.1f)) {
            name += " " + wizardName(gender)
        }
        return name
    }

    fun wizardFullName(name: String): String {
        val pre = listOf(
            "Lord",
            "Master",
            "King",
            "Emperor",
            "Judge",
            "the Mighty",
            "the All-Powerful",
            "the Amazing",
            "the Incredible",
            "All-Seeing",
            "the Great",
            "Almighty",
            "the God-King",
            "the Eternal",
            "the Infamous",
            "the Dark Lord",
            "Duke",
            "Doctor",
            "Doktor",
            "Chief",
            "Li'l",
            "Mr.",
            "Mister",
            "President",
            "A Pimp Called",
            "Captain",
            "Dark Lord",
            "Dr.",
            "Father",
            "Brother",
            "Overseer",
            "Boss",
            "Professor",
            "Prince",
            "Baron",
            "Judge",
            "Baron von",
            "the Astounding",
            "the World-Famous",
            "Czar",
            "Overseer",
            "Big",
        )
        val post = listOf(
            "the Mighty",
            "the Snake",
            "the Tiger",
            "the Witch-King",
            "the Sorceror-Lord",
            "the Sorceror",
            "the Warlock",
            "the Mind-Taker",
            "the Three-Faced",
            "the Two-Faced",
            "the One-Faced",
            "the Confusor",
            "the Soul-Taker",
            "the Soulstealer",
            "the Enslaver",
            "the Bloody",
            "the Terrible",
            "the Infinite",
            "the Undying",
            "the Unliving",
            "the Soulless",
            "the Cursed",
            "the Wise",
            "the Eternal",
            "the Great",
            "the Tempter",
            "the Stormbringer",
            "the Annihilator",
            "the Ender",
            "the World-Ender",
            "the Destroyer",
            "the Dark",
            "the Evil",
            "the Malignant",
            "the Normal",
            "the Abnormal",
            "the Mutant",
            "the Wrathful",
            "the Tyrant",
            "of the Abyss",
            "of the Shadow",
            "of the Dark",
            "of the Nine Hells",
            "of the Seven Hells",
            "of the One Hell",
            "the Survivor",
            "the Serpent",
            "the Impressive",
            "the Important",
            "the Snake",
            "the Ender",
            "the Slayer",
            "the Abjurer",
            "the Protector",
            "the Unhelpful",
            "the Unpleasant",
            "the Impossible",
            "the Wonder-Worker",
            "the All-Knowing",
            "the Betrayer",
            "the Cracker-Jack Kid",
            "the Kid",
            "the God-King",
            "Jr.",
            "III",
            "IV",
            "the First",
            "2.0",
        )

        val full = if (Dice.chance(0.06f)) {
            pre.random() + " " + name + " " + post.random()
        } else if (Dice.chance(0.4f)) {
            pre.random() + " " + name
        } else {
            name + " " + post.random()
        }
        return full
    }

    fun prophetName(): String {
        val a = listOf("P", "F", "B", "H", "Fl", "Br", "Pr", "Fr", "Pl", "Fl").random()
        val b = listOf("a", "o", "e", "ei", "ou", "ae", "ea", "ai", "ee", "oo").random()
        val c = listOf("b", "t", "s", "th", "ch", "f", "sh", "n", "d", "dh").random()
        val d = listOf("o", "a", "ai", "oo", "ee", "ae", "io", "u", "ue", "ya").random()
        return a+b+c+d
    }

    fun bigCityName() = listOf<String>(
        "Man-Hat",
        "Hostone",
        "Atlanta",
        "Port-Lan",
        "Bronks",
        "Sanfan",
        "Windy",
        "Angelis",
        "Luis",
        "Orlon",
        "Yorik",
        "Sea-Tell",
        "Fenix",
        "Fila-Dell",
        "Dallas",
        "Sanoze",
        "Aston",
        "Denver",
        "Capitol",
        "Vega$"
    ).random()

    fun smallCityName() = listOf<String>(
        "Springfield",
        "Colum",
        "Troit",
        "Menfos",
        "Two Sons",
        "Saco-Ment",
        "Balomar",
        "Passo",
        "Bakersfield",
        "Henderson",
        "Stock",
        "Chula",
        "Greens",
        "Freeman",
        "Tako-Man",
        "Moinese",
        "Chester",
        "Ontana",
        "Oreno",
        "Marill",
        "Gomer",
        "Mon-Tag",
        "Chev-Ton",
        "Rockford",
        "Thorn-Ton",
        "Torran",
        "Dent",
        "Visal",
        "Yrac",
        "Condid",
        "Pembroke",
        "Eu-Gein",
        "Ack-Ron",
        "Nox",
        "Ubboc",
        "Madison",
        "Cinatus",
        "Saint",
        "Pineland",
        "Butcher",
        "Gastown",
        "Clevend",
        "Anhelm",
        "Lewis-Ron",
        "Temper",
        "Meson",
        "Amazon",
        "Gorgol",
        "Meta",
        "Elton",
        "Elvion",
        "Galas",
        "Pornoth",
        "Clenton",
        "Dinvill",
        "Racheta",
        "Omoho",
        "Skaran-Ton",
    ).random()

    fun mountainRangeName() = if (Dice.flip()) {
        "Mountains of " + listOf(
            "Mist",
            "Dreams",
            "the Moon",
            "the Sun",
            "Avarice",
            "Nightmares",
            "Hunger",
            "Silence",
            "Madness",
            "Ending",
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord()
        ).random()
    } else {
        listOf(
            "Misty",
            "Hazy",
            "Foggy",
            "Rocky",
            "Stony",
            "Frosty",
            "Forgotten",
            "Nameless",
            "Spine",
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord(),
            wizardNameWord()
        ).random() + " Mountains"
    }

    fun desertName() = listOf(
        "Mirrored",
        "Painted",
        "Forsaken",
        "Forgotten",
        "Forbidden",
        "Lacquered",
        "Blasted",
        "Bleached",
        "Skull",
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord()
    ).random() + " " + listOf(
        "Desert",
        "Wastes",
        "Wasteland"
    ).random()

    fun swampName() = listOf(
        "Bog", "Fen", "Swamp"
    ).random() + " of " + listOf(
        "Despair",
        "Drowning",
        "Stench",
        "Vapours",
        "Regrets",
        "Twilight",
        "Howling",
        "Weeping",
        "Rashes",
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord(),
        wizardNameWord()
    ).random()

    fun forestName(): String {
        val adjs = listOf("Horror", "Fear", "Gloom", "Spider", "Beetle", "Death", "Moon", "Moss",
            "Weeping", "Howling",
            wizardNameWord(), wizardNameWord(), wizardNameWord(), wizardNameWord(), wizardNameWord(), wizardNameWord(), wizardNameWord())
        return when (Dice.oneTo(4)) {
            1 -> { adjs.random() + "wood" }
            2 -> { adjs.random() + "woods" }
            3 -> { "Forest of "  + adjs.random() }
            else -> { adjs.random() + " Forest" }
        }
    }

    fun villageName(): String {
        val pre = listOf("iron", "owl", "deer", "oak", "sparrow", "robin", "mud", "snake", "turnip",
            "carrot", "onion", "cheese", "coon", "bear", "wolf", "fox", "hound", "water", "rabbit", "hare",
            "elm", "birch", "pine", "brook", "tarn", "brass", "barn", "loch", "lark", "roche", "east", "west",
            "north", "south", "larry", "bill", "maude", "sally", "chester", "white", "black", "gray", "blue",
            "hawk", "eagle", "rat", "possum", "wall", "archer", "arrow", "axe", "anvil", "sharp", "dull",
            "fail", "bleak", "fear", "gale", "frost", "anger", "hunger", "hope", "faith", "mel", "kayle", "figs",
            "turd", "waste", "dust", "ruby", "kirby", "stan", "buffa", "cow", "chicken", "caul", "omen",
            "comet", "moon", "demon", "motor", "trucker", "kill", "dismal", "dubya", "yeezy", "prince", "beggar",
            "hobo", "bum", "drifter", "booze"
        )
        val post = listOf("ton", "town", "ville", " Town", "more", "bury", "dale", "field", "bend", "dell",
            "pool", "mouth", "ham", "wick", "ford", "hope", "meet", "stead", "vale", "point", "sted", "ston",
            " Hollow", " Holler", " Camp", "topia", " Valley", " Gulch", " Bend", " Fork", " Mill", " Grove",
            "land", "stein", "ward", "gard", "'s Folly", "'s End"
        )

        var name = pre.random() + post.random()
        if (Dice.chance(0.1f)) name = listOf("North", "East", "South", "West", "New", "Old").random() + " " + name.capitalize()

        return name.capitalize()
    }

    val firstNames = listOf("Cletus", "Abe", "Arthur", "Fred", "Phil", "Walt", "Ralph", "Amos", "Theo", "Atticus",
        "Felix", "Silas", "Oliver", "Cassius", "Hugo", "Oscar", "Milo", "Otto", "August", "Jude", "Miles",
        "Ezra", "Clarence", "Adolf", "Clem", "Joseph", "Archie", "Bernard", "Grady", "Cliff", "Dean", "Earl",
        "Edison", "Edmund", "Elijah", "Emile", "Erwin", "Fletcher", "Frank", "George", "Gerald", "Gus",
        "Harold",  "Harvey", "Howard", "Hugh", "Jerry", "Langston", "Louis", "Mickey", "Milton", "Morgan",
        "Nelson", "Norman", "Neville", "Orville", "Oscar", "Otis", "Reed", "Rodney", "Roy", "Sherman",
        "Spencer", "Stan", "Sterling"
    )

    fun graveName(): String {
        if (Dice.chance(0.1f)) {
            val names = listOf(
                "Henry Corden", "Robert Ridgely", "Nellie Bellflower", "Dick Tufeld", "Alan Oppenheimer",
                "Joe Ruby", "Ken Spears", "Jerry Eisenberg", "Rudy Larriva", "Buzz Dixon", "Mel Stanley", "Dan Burford",
                "Wade Bell", "Jesse Morris"
            )
            return names.random()
        }
        val first = firstNames.random()
        val initial = listOf("X", "S", "Q", "N", "L", "P", "T", "Z", "B")

        return first.random() + " " + initial.random() + "."
    }

    fun epitaph(): String {
        val epitaphs = listOf(
            "All dressed up and noplace to go.",
            "Quick on the trigger, slow on the draw.",
            "He told us he was sick.",
            "Sire of few, father of many.",
            "Stay dead this time.",
            "Go to the old tree at full moon to learn my secret.",
            "Died in bed.",
            "He is with his God now.",
            "All shall join me one day.",
            "A Saint to many, a Scoundrel to some.",
            "Keep Back - Contagious Necromancy",
            "We thank him for his sacrifice.",
            "Measured once, shot twice.",
            "The world was not ending -- only him.",
            "Fell before the walls did.",
            "One day he will rise for his vengeance.",
            "Died hungry, or at least he said as much.",
            "Sticks and stones broke his bones.",
            "He never heard it coming.",
            "Covered in glory and dirt.",
            "Sleeps alone.",
            "One crazy son of a bitch.",
            "He couldn't take it with him.",
            "His works completed, may he find rest.",
            "A true Oddity.",
            "May all his children prosper.",
            "Clan exile, hero to our village.",
            "Let this stone be a warning to those who would imitate him.",
            "Resting in the place he loved most."
        )
        return epitaphs.random()
    }

    fun tavernName(): String {
        val post = listOf("Pub", "Tavern", "Bar", "House", "Inn", "Hole", "Rest", "Watering Hole", "Station",
            "Toast", "Last Call", "Respite", "Public House", "Rest Stop", "Cask", "Jug", "Taphouse", "Alehouse", "Brewery",
            "Distillery")
        val adj = listOf("Laughing", "Dancing", "Scarlet", "Crimson", "Golden", "Silver", "Sloshed", "Tipsy", "Toasty",
            "Singing", "Plastered", "Smiling", "Sleepy", "Stout", "Weeping", "Flaming", "Suffering", "Tempered", "Humble")
        val noun = listOf("Dragon", "Pig", "Boar", "Groundhog", "Aurochs", "Ox", "Hound", "Toad", "Caroc", "Manape", "Axe",
            "Tankard", "Flagon", "Lantern", "Hearth", "Dagger", "Wizard", "Witch", "Serpent", "Mok", "Stallion", "Mare")

        return when (Dice.oneTo(6)) {
            1 -> firstNames.random() + "'s " + post.random()
            2 -> "The " + adj.random() + " " + noun.random()
            3 -> adj.random() + " " + noun.random() + " " + post.random()
            4 -> adj.random() + " " + firstNames.random() + "'s"
            5 -> noun.random() + " & " + noun.random() + " " + post.random()
            else -> adj.random() + " " + firstNames.random() + "'s " + post.random()
        }
    }
}
