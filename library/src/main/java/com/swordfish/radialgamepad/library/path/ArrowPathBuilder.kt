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

object ArrowPathBuilder {

    private const val X_START = 0.05f
    private const val X_END = 0.15f
    private const val X_MID = 0.33f
    private const val Y_SPACING = 0.20f

    fun build(drawingBox: Rect): Path {
        val xLeft = drawingBox.left + drawingBox.width() * X_START
        val xMid = drawingBox.left + drawingBox.width() * X_MID
        val xRight = drawingBox.left + drawingBox.width() * (1.0f - X_END)
        val xRightControl = drawingBox.left + drawingBox.width().toFloat()
        val yTop = drawingBox.top + drawingBox.height() * Y_SPACING
        val yMid = drawingBox.top + drawingBox.height() / 2f
        val yBottom = drawingBox.top + drawingBox.height() * (1.0f - Y_SPACING)

        return Path().apply {
            moveTo(xLeft, yMid)
            lineTo(xMid, yTop)
            lineTo(xRight, yTop)
            quadTo(xRightControl, yMid, xRight, yBottom)
            lineTo(xRight, yBottom)
            lineTo(xMid, yBottom)
            close()
        }
    }
}