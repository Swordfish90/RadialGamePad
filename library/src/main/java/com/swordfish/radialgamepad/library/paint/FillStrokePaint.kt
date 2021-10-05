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
import android.graphics.Paint
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.utils.PaintUtils

class FillStrokePaint(context: Context, theme: RadialGamePadTheme) {
    private val fillPaint = buildFillPaint(theme, context)
    private val strokePaint = buildStrokePaint(theme, context)

    private fun buildStrokePaint(theme: RadialGamePadTheme, context: Context): BasePaint? {
        if (!theme.enableStroke) return null
        return BasePaint().apply {
            style = Paint.Style.STROKE
            color = theme.strokeColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = PaintUtils.convertDpToPixel(theme.strokeWidthDp, context)
        }
    }

    private fun buildFillPaint(theme: RadialGamePadTheme, context: Context): BasePaint {
        return BasePaint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = theme.normalColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = PaintUtils.convertDpToPixel(theme.strokeWidthDp, context)
        }
    }

    fun setFillColor(color: Int) {
        fillPaint.color = color
    }

    fun setStrokeColor(color: Int) {
        strokePaint?.color = color
    }

    fun paint(paintLambda: (Paint) -> Unit) {
        paintLambda(fillPaint)
        strokePaint?.let {
            paintLambda(it)
        }
    }
}
