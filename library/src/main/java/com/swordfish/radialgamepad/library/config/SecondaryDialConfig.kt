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

package com.swordfish.radialgamepad.library.config

/**
 * The base secondary dial configuration.
 * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
 * @property spread Defines how many secondary dials is occupies
 * @property scale Defines a scaling factor. Used to make some controls more prominent.
 */
sealed class SecondaryDialConfig(val index: Int, val spread: Int, val scale: Float) {
    class SingleButton(
        index: Int,
        spread: Int,
        scale: Float,
        val buttonConfig: ButtonConfig,
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, spread, scale)

    class Stick(
        index: Int,
        spread: Int,
        scale: Float,
        val id: Int,
        val buttonPressId: Int? = null,
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, spread, scale)

    class Cross(
        index: Int,
        spread: Int,
        scale: Float,
        val id: Int,
        val rightDrawableId: Int? = null,
        val rightDrawableForegroundId: Int? = null,
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, spread, scale)

    class Empty(
        index: Int,
        spread: Int,
        scale: Float
    ) : SecondaryDialConfig(index, spread, scale)
}