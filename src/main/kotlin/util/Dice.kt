package util

import kotlin.random.Random

object Dice {

    val random = Random(1)

    fun chance(c: Float) = random.nextFloat() < c

    fun float(f0: Float, f1: Float) = random.nextFloat() * (f1 - f0) + f0

    fun zeroTil(max: Int) = random.nextInt(max)

    fun oneTil(max: Int) = random.nextInt(max-1) + 1

    fun zeroTo(max: Int) = random.nextInt(max+1)

    fun oneTo(max: Int) = random.nextInt(max) + 1

    fun range(min: Int, max: Int) = random.nextInt(max - min) + min
}
