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

package com.swordfish.radialgamepad.library.path

import android.graphics.Path
import android.graphics.Rect

object CirclePathBuilder {

    private const val SCALE = 0.8f
    private const val OFFSET = 0.15f

    fun build(rect: Rect): Path {
        val radius = minOf(rect.width(), rect.height()) / 2f
        val offsetX = radius * OFFSET

        return Path().apply {
            addCircle(
                rect.centerX().toFloat() + offsetX,
                rect.centerY().toFloat(),
                radius * SCALE,
                Path.Direction.CCW
            )
        }
    }
}
