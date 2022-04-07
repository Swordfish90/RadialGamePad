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

package com.swordfish.radialgamepad.library.paint

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme

class CompositeButtonPaint(context: Context, theme: RadialGamePadTheme) {

    private var radius: Float = 0f

    private val painterPalette = PainterPalette(context, theme)

    fun updateDrawingBox(drawingBox: RectF) {
        radius = minOf(drawingBox.width(), drawingBox.height()) / 30f
    }

    fun drawCompositeButton(canvas: Canvas, x: Float, y: Float, isActive: Boolean) {
        val paint = if (isActive) painterPalette.pressed else painterPalette.light
        paint.paint {
            canvas.drawCircle(x, y, radius, it)
        }
    }
}
