package util

import kotlin.random.Random

object Dice {

    var random = Random(Random.nextInt())

    fun newSeed(newSeed: Int) {
        random = Random(newSeed)
    }

    fun chance(c: Float) = random.nextFloat() < c
    fun flip() = chance(0.5f)

    fun float(f0: Float, f1: Float) = random.nextFloat() * (f1 - f0) + f0

    fun zeroTil(max: Int) = random.nextInt(max)

    fun oneTil(max: Int) = random.nextInt(max-1) + 1

    fun zeroTo(max: Int) = random.nextInt(max+1)

    fun oneTo(max: Int) = random.nextInt(max) + 1

    fun range(min: Int, max: Int) = if (min == max) min else if (min > max) random.nextInt(min - max + 1) + max else random.nextInt(max - min + 1) + min

    fun skillCheck() = (random.nextInt(6) + random.nextInt(6) + random.nextInt(6) + 3).toFloat()

    fun sign() = if (flip()) -1 else 1
}
