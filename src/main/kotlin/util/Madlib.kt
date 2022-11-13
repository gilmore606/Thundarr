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

    fun wizardName(gender: Entity.Gender = Entity.Gender.NEUTER): String {
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
            "lar"
        )

        var name = ""
        if (Dice.chance(0.4f)) {
            name = syl1.random() + conn.random() + syl2.random()
        } else {
            name = syl1.random() + syl2.random()
        }
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
            "the World-Famous"
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
            "the All-Knowing",
            "the Betrayer",
            "the Cracker-Jack Kid",
            "the Kid",
            "the God-King",
            "Jr.",
            "III",
            "IV",
            "the First"
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

}
