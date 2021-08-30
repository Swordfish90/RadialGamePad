/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2021. This file is part of RadialGamePad.
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

package com.swordfish.radialgamepad.library.touch

import android.graphics.PointF
import com.swordfish.radialgamepad.library.math.MathUtils

class TouchAnchor(
    private val point: PointF,
    private val normalizedPoint: PointF,
    private val strength: Float,
    val ids: Set<Int>
) {
    fun getX() = point.x

    fun getY() = point.y

    fun getNormalizedX() = normalizedPoint.x

    fun getNormalizedY() = normalizedPoint.y

    fun getNormalizedDistance(x: Float, y: Float): Float {
        return MathUtils.distanceSquared(x, point.x, y, point.y) / (strength * strength)
    }

    companion object {
        fun fromPolar(angle: Float, length: Float, strength: Float, ids: Set<Int>): TouchAnchor {
            return TouchAnchor(
                MathUtils.polarCoordinatesToPoint(angle, length),
                MathUtils.polarCoordinatesToPoint(angle, 1f),
                strength,
                ids
            )
        }
    }
}
