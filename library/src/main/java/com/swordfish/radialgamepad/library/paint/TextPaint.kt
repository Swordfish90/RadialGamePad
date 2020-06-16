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

package com.swordfish.radialgamepad.library.paint

import android.graphics.*
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.utils.memoize

class TextPaint {

    private var textBounds: Rect = Rect()

    private val cachedTextAspectRatio = { text: String ->
        computeTextAspectRatio(text)
    }.memoize()

    private val textPaint = BasePaint().apply {
        this.typeface = Typeface.DEFAULT_BOLD
        this.style = Paint.Style.FILL
    }

    fun paintText(rectF: RectF, text: String, canvas: Canvas, theme: RadialGamePadTheme) {
        paintText(rectF.left, rectF.top, rectF.width(), rectF.height(), text, canvas, theme)
    }

    private fun paintText(left: Float, top: Float, width: Float, height: Float, text: String, canvas: Canvas, theme: RadialGamePadTheme) {
        val textAspectRatio = cachedTextAspectRatio(text)

        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = minOf(height / 2 , width / textAspectRatio)
        textPaint.color = theme.textColor

        val textWidth = textPaint.measureText(text)

        val xPos = left - textWidth / 2f + width / 2f
        val yPos = top + height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        canvas.drawText(text, xPos, yPos, textPaint)
    }

    private fun computeTextAspectRatio(text: String): Float {
        textPaint.textSize = 20f
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        return textBounds.width().toFloat() / textBounds.height()
    }
}