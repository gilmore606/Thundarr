package things

import kotlinx.serialization.Serializable
import render.tilesets.Glyph
import java.lang.Math.max

@Serializable
class Treasure(
    val totalWorth: Int  // 2-10
) : Portable() {
    enum class Form(
        val worth: Int,
        val typeName: String,
        val value: Int,
        val weight: Float,
    ) {
        COIN(1, "coin", 30, 0.02f),
        RING(2, "ring", 50, 0.05f),
        PENDANT(4, "pendant", 70, 0.05f),
        CHAIN(4, "chain", 80, 0.1f),
        FIGURINE(4, "figurine", 100, 0.2f),
        WATCH(5, "watch", 120, 0.1f),
        INGOT(5, "ingot", 150, 0.3f),
    }

    enum class Material(
        val worth: Int,
        val materialName: String,
        val valueMod: Float,
        val hue: Float,
    ) {
        BRASS(1, "brass", 0.5f, -0.2f),
        STEEL(2, "steel", 0.8f, 2.1f),
        ALUMINUM(2, "aluminum", 1f, 2.1f),
        SILVER(3, "silver", 1.3f, 2.3f),
        TITANIUM(3, "titanium", 1.6f, 2.5f),
        GOLD(5, "gold", 2.2f, 0f),
        PLATINUM(5, "platinum", 2.8f, -0.1f),
    }

    val form: Form
    val material: Material

    init {
        var worthleft = max(totalWorth, 2)
        form = Form.values().filter { it.worth < worthleft }.random()
        worthleft -= form.worth
        material = Material.values().filter { it.worth <= worthleft && it.worth >= (worthleft / 2) }.random()
    }

    override val tag = Tag.TREASURE
    override fun name() = material.materialName + " " + form.typeName
    override fun weight() = form.weight
    override fun glyph() = Glyph.GOLD_TREASURE
    override fun hue() = material.hue
    override fun value() = (form.value * material.valueMod).toInt()
    override fun canListGrouped() = false
    override fun description() = "An ancient " + material.materialName + " " + form.typeName + ".  Looks valuable."
}
