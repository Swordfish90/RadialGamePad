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

package com.swordfish.radialgamepad.library.utils

import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt

object PaintUtils {

    fun RectF.scaleCentered(scale: Float): RectF {
        val scaledWidth = width() * scale
        val scaledHeight = height() * scale
        val deltaWidth = (width() - scaledWidth) * 0.5f
        val deltaHeight = (height() - scaledHeight) * 0.5f
        return RectF(left + deltaWidth, top + deltaHeight, right - deltaWidth, bottom - deltaHeight)
    }

    fun RectF.roundToInt(): Rect = Rect(
        left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt()
    )

    fun toTransparent(color: Int, transparency: Float): Int {
        val alpha = (255 * transparency).roundToInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}