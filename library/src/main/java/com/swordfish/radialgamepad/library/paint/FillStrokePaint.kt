/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2022. This file is part of RadialGamePad.
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

import android.graphics.Color
import android.graphics.Paint

class FillStrokePaint(fillColor: Int, strokeColor: Int, strokeSizePx: Float) {
    val fillPaint = buildFillPaint(fillColor, strokeSizePx)
    val strokePaint = buildStrokePaint(strokeColor, strokeSizePx)

    private fun buildStrokePaint(strokeColor: Int, strokeSizePx: Float): BasePaint? {
        // TODO FILIPPO... Handle stroke size set to zero.
        if (strokeColor == Color.TRANSPARENT) return null
        return BasePaint().apply {
            style = Paint.Style.STROKE
            color = strokeColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = strokeSizePx
        }
    }

    private fun buildFillPaint(fillColor: Int, strokeSizePx: Float): BasePaint {
        return BasePaint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = fillColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = strokeSizePx
        }
    }

    inline fun paint(paintLambda: (Paint) -> Unit) {
        paintLambda(fillPaint)
        strokePaint?.let {
            paintLambda(it)
        }
    }
}
