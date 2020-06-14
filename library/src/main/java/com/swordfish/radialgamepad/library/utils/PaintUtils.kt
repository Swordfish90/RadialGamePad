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

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object PaintUtils {

    fun RectF.scale(scale: Float): RectF {
        return RectF(left * scale, top * scale, right * scale, bottom * scale)
    }

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

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val density = context.resources.displayMetrics.densityDpi.toFloat()
        return dp * (density / DisplayMetrics.DENSITY_DEFAULT)
    }
}