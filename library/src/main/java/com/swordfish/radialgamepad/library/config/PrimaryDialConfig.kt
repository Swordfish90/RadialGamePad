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

import com.swordfish.radialgamepad.library.event.GestureType

/**
 * The configuration object for a PrimaryDial.
 */
sealed class PrimaryDialConfig {

    /**
     * The Cross dial represents a simple DPAD with.
     * @property crossConfig The cross configuration.
     */
    data class Cross(val crossConfig: CrossConfig) : PrimaryDialConfig()

    /**
     * The Stick dial represents a simple touch stick.
     * @property id The control id. It is passed back to discriminate events.
     * @property buttonPressId Specify a button action when the Stick is double pressed. Useful for platforms with clickable thumb sticks.
     * @property supportsGestures The set of gestures that the button can emit. Defaults to empty.
     * @property contentDescription Specify the content description name.
     * @property theme A RadialGamePadTheme specific for this dial. If omitted the RadialGamePad one is used.
     */
    data class Stick(
        val id: Int,
        val buttonPressId: Int? = null,
        val supportsGestures: Set<GestureType> = emptySet(),
        val contentDescription: String = "Stick",
        val theme: RadialGamePadTheme? = null
    ) : PrimaryDialConfig()

    /**
     * The PrimaryButtonDial represents the primary set of action buttons.
     * @property dials A list of buttons, distributed on a circle around the center.
     * @property center A single, optional button to be displayed at the center.
     * @property rotationInDegrees Optional rotation (in degrees) for the button disposed in circle.
     * @property allowMultiplePressesSingleFinger Allow pressing multiple buttons with the same finger.
     * @property theme A RadialGamePadTheme specific for this dial. If omitted the RadialGamePad one is used.
     */
    data class PrimaryButtons(
        val dials: List<ButtonConfig>,
        val center: ButtonConfig? = null,
        val rotationInDegrees: Float = 0f,
        val allowMultiplePressesSingleFinger: Boolean = true,
        val theme: RadialGamePadTheme? = null
    ) : PrimaryDialConfig()
}