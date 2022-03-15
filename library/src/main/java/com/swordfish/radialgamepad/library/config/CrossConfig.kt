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

package com.swordfish.radialgamepad.library.config

import com.swordfish.radialgamepad.library.event.GestureType

/**
 * A DPAD configuration.
 * @property id The id returned when its events are fired.
 * @property shape The shape of a single DPAD button.
 * @property rightDrawableForegroundId The optional drawable that is drawn on top with text color.
 * @property supportsGestures The set of gestures that the button can emit. Defaults to empty.
 * @property useDiagonals The controls will allow diagonal directions.
 * @property contentDescription Content description read by the screen reader. Defaults to "D-Pad".
 * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
 */
// TODO For consistency the theme should be at a config level.
data class CrossConfig(
    val id: Int,
    val shape: Shape = Shape.STANDARD,
    val rightDrawableForegroundId: Int? = null,
    val supportsGestures: Set<GestureType> = emptySet(),
    val contentDescription: CrossContentDescription = CrossContentDescription(),
    val useDiagonals: Boolean = true,
    val theme: RadialGamePadTheme? = null
) {
    enum class Shape {
        STANDARD,
        CIRCLE
    }
}
