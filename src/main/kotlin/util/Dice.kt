package util

import kotlin.random.Random

object Dice {

    fun chance(c: Float) = Random.nextFloat() > c

    fun float(f0: Float, f1: Float) = Random.nextFloat() * (f1 - f0) + f0
}
