package com.swordfish.radialgamepad.library.utils

object MathUtils {
    infix fun Int.fmod(other: Int) = ((this % other) + other) % other

    infix fun Float.fmod(other: Float) = ((this % other) + other) % other

    fun Int.isEven(): Boolean = this % 2 == 0

    fun Int.isOdd(): Boolean = this % 2 == 1
}