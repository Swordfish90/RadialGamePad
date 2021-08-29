/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2020. This file is part of RadialGamePad.
 *
 * RadialGamePad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RadialGamePad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RadialGamePad.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("unused")

package com.swordfish.radialgamepad.library.math

import android.graphics.PointF
import com.swordfish.radialgamepad.library.utils.Constants
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object MathUtils {

    fun toRadians(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()

    fun toDegrees(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()

    /** Compute the angle with the x axis of the line between two points. Results in range [0,2pi[.*/
    fun angle(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return ((-atan2(y2 - y1, x2 - x1) + Constants.PI2) % (Constants.PI2))
    }

    fun clamp(x: Float, min: Float, max: Float): Float {
        return maxOf(minOf(x, max), min)
    }

    fun distance(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return sqrt(distanceSquared(x1, x2, y1, y2))
    }

    fun distanceSquared(x1: Float, x2: Float, y1: Float, y2: Float): Float {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
    }

    /** Check if angle is in range. Handles negative values and overflow. */
    fun isAngleInRange(x: Float, min: Float, max: Float): Boolean {
        val pi2 = Constants.PI2
        return sequenceOf(x in min..max, x - pi2 in min..max, x + pi2 in min..max).any { it }
    }

    fun lint(x: Float, a: Float, b: Float) = a * (1 - x) + b * x

    fun polarCoordinatesToPoint(angle: Float, magnitude: Float = 1.0f): PointF {
        return PointF(magnitude * cos(angle), magnitude * sin(angle))
    }

    fun convertPolarCoordinatesToSquares(angle: Float, strength: Float): PointF {
        val u = strength * cos(angle)
        val v = strength * sin(angle)
        return mapEllipticalDiskCoordinatesToSquare(u, v)
    }

    private fun mapEllipticalDiskCoordinatesToSquare(u: Float, v: Float): PointF {
        val u2 = u * u
        val v2 = v * v
        val twoSqrt2 = 2.0f * sqrt(2.0f)
        val subTermX = 2.0f + u2 - v2
        val subTermY = 2.0f - u2 + v2
        val termX1 = subTermX + u * twoSqrt2
        val termX2 = subTermX - u * twoSqrt2
        val termY1 = subTermY + v * twoSqrt2
        val termY2 = subTermY - v * twoSqrt2

        val x = (0.5f * sqrt(termX1) - 0.5f * sqrt(termX2))
        val y = (0.5f * sqrt(termY1) - 0.5f * sqrt(termY2))

        return PointF(x, y)
    }

    infix fun Int.fmod(other: Int) = ((this % other) + other) % other

    infix fun Float.fmod(other: Float) = ((this % other) + other) % other

    fun Int.isEven(): Boolean = this % 2 == 0

    fun Int.isOdd(): Boolean = this % 2 == 1
}