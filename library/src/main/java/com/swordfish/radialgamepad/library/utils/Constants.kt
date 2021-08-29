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

object Constants {
    val DEFAULT_COLOR_NORMAL = Color.argb(125, 125, 125, 125)
    val DEFAULT_COLOR_PRESSED = Color.argb(255, 125, 125, 125)
    val DEFAULT_COLOR_TEXT = Color.argb(125, 255, 255, 255)
    val DEFAULT_COLOR_BACKGROUND = Color.argb(50, 125, 125, 125)
    val DEFAULT_COLOR_LIGHT = Color.argb(30, 125, 125, 125)

    const val PI = Math.PI.toFloat()
    const val PI2 = 2f * PI
}