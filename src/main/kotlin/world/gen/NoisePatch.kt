package world.gen

import com.badlogic.gdx.Gdx
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import util.Simplex
import java.io.File
import java.lang.Double.max

@Serializable
enum class NoiseMode(
    val label: String,
    val combine: (a: Double, b: Double)->Double
    ) {
    MULT("Mult", { a,b -> a * b }),
    ADD("Add", { a,b -> a + b }),
    MAX("Max", { a,b -> kotlin.math.max(a, b) }),
    AVG("Avg", { a,b -> (a + b) * 0.5 }),
    DIV("Div", { a,b -> a / b }),
    ONLYA("OnlyA", { a,b -> a })
}

@Serializable
class NoiseSample(
    var scale: Float = 0.01f,
    var octaves: Int = 2,
    var persistence: Float = 0.5f,
    var amplitude: Float = 1.0f,
    var min: Double = 0.0,
    var max: Double = 1.0
) {
    fun value(x: Int, y: Int) =
        java.lang.Double.min(max,
            java.lang.Double.max(0.0,
                Simplex.octave(x, y, octaves, persistence, scale) - min) / (1.0 - min)
        ) * amplitude
}

@Serializable
class NoisePatch(
    var samples: Pair<NoiseSample, NoiseSample> = Pair(NoiseSample(), NoiseSample()),
    var mode: NoiseMode = NoiseMode.MULT,
    var quantize: Int = 0,
    var amp: Double = 1.0,
    var min: Double = 0.0
) {
    fun value(x: Int, y: Int): Double {
        var raw = mode.combine(samples.first.value(x,y), samples.second.value(x,y)) * amp
        raw = max(0.0, raw - min) / (1.0 - min)
        if (quantize > 0) {
            val steps = 9 - quantize
            val step = 1.0 / steps
            var quantized = 0.0
            while (quantized < raw) {
                quantized += step
            }
            quantized -= step
            raw = quantized
        }
        return max(0.0, kotlin.math.min(1.0, raw))
    }
}

object NoisePatches {
    val json = Json { ignoreUnknownKeys = true }
    val patches: MutableMap<String, NoisePatch> = json.decodeFromString(
        Gdx.files.internal("res/noise/noisepatches.json").readString()
    )

    fun get(patch: String, x: Int, y: Int) = patches[patch]?.value(x,y) ?: throw RuntimeException("Non-existent noise patch $patch !")

    fun save() {
        File("src/main/resources/noisepatches.json").printWriter().use { out ->
            out.println(
                json.encodeToString(patches)
            )
        }
    }
}
