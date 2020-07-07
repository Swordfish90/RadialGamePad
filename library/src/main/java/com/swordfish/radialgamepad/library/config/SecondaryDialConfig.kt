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

import kotlin.math.roundToInt

/**
 * The base secondary dial configuration.
 * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
 * @property spread Defines how many secondary dials is occupies
 * @property scale Defines a scaling factor. Used to make some controls more prominent.
 */
sealed class SecondaryDialConfig(val index: Int, val spread: Int, val scale: Float) {
    /**
     * A simple, single button secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property buttonConfig The button configuration
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    class SingleButton(
        index: Int,
        spread: Int,
        val buttonConfig: ButtonConfig,
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, spread, 1f)

    /**
     * A secondary Stick dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property id The id returned when its events are fired.
     * @property id The id returned when its events are fired.
     * @property buttonPressId The optional id fired when the stick is double tapped.
     * @property contentDescription Content description read by the screen reader. Defaults to "Stick".
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    class Stick(
        index: Int,
        scale: Float,
        val id: Int,
        val buttonPressId: Int? = null,
        val contentDescription: String = "Stick",
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, scale.roundToInt(), scale)

    /**
     * A DPAD secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property id The id returned when its events are fired.
     * @property rightDrawableId The optional drawable that define the shape of the right button.
     * @property rightDrawableForegroundId The optional drawable that is drawn on top with text color.
     * @property contentDescription Content description read by the screen reader. Defaults to "D-Pad".
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    class Cross(
        index: Int,
        scale: Float,
        val id: Int,
        val rightDrawableId: Int? = null,
        val rightDrawableForegroundId: Int? = null,
        val contentDescription: String = "D-Pad",
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, scale.roundToInt(), scale)

    /**
     * An empty dial, that gets considered when measuring the gamepad. Useful for creating symmetric pads.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     */
    class Empty(
        index: Int,
        spread: Int,
        scale: Float
    ) : SecondaryDialConfig(index, spread, scale)
}