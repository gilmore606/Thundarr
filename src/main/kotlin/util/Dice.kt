package util

import kotlin.random.Random

object Dice {

    fun chance(c: Float) = Random.nextFloat() > c

    fun float(f0: Float, f1: Float) = Random.nextFloat() * (f1 - f0) + f0

    fun zeroTil(max: Int) = Random.nextInt(max)

    fun oneTil(max: Int) = Random.nextInt(max-1) + 1

    fun zeroTo(max: Int) = Random.nextInt(max+1)

    fun oneTo(max: Int) = Random.nextInt(max) + 1
}
