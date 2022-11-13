package util

import world.Entity

object Madlib {

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
            "Zan",
            "Gron",
            "Gro",
            "Lo",
            "Ho",
            "Sor",
            "Sa",
            "Za",
            "Ze",
            "Zol",
            "Bel",
            "Hell",
            "Ou",
            "A",
            "E",
            "O",
            "Yee"
        )
        val conn = listOf(
            "ba",
            "sa",
            "da",
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
            "ka"
        )
        val syl2 = listOf(
            "tar",
            "gan",
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
        return name
    }

    fun wizardFullName(name: String): String {
        val pre = listOf(
            "Lord",
            "Master",
            "King",
            "Emperor",
            "the Mighty",
            "the All-Powerful",
            "the Amazing",
            "the Incredible",
            "All-Seeing",
            "the Great",
            "Almighty",
            "the God-King",
            "the Eternal",
            "Duke",
            "Doctor",
            "Doktor",
            "Chief",
            "Li'l",
            "Mr.",
            "Mister",
            "President"
        )
        val post = listOf(
            "the Mighty",
            "the Terrible",
            "the Infinite",
            "the Wise",
            "the Eternal",
            "the Great",
            "the Tempter",
            "the Stormbringer",
            "the Annihilator",
            "the Destroyer",
            "the Dark",
            "the Evil",
            "the Malignant",
            "the Wrathful",
            "the Tyrant",
            "of the Abyss",
            "of the Shadow",
            "of the Nine Hells",
            "the Survivor",
            "the Serpent",
            "the Impressive",
            "the Important",
            "the Snake",
            "the Ender",
            "the Slayer",
            "the Abjurer",
            "the Protector",
            "the All-Knowing",
            "the Betrayer",
            "the Cracker-Jack Kid"
        )

        val full = if (Dice.chance(0.05f)) {
            pre.random() + " " + name + " " + post.random()
        } else if (Dice.chance(0.4f)) {
            pre.random() + " " + name
        } else {
            name + " " + post.random()
        }
        return full
    }

}
