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
 * The base secondary dial configuration.
 * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
 * @property spread Defines how many secondary dials is occupies
 * @property scale Defines a scaling factor. Used to make some controls more prominent.
 * @property distance Defines a distance factor. Used to move the control away from the primary dial.
 * @property avoidClipping When measuring, the library is not allowed to clip the area not occupied by the drawing box.
 * @property processSecondaryDialRotation When set you can modify the final rotation value that will be applied to this dial. Rotation is expressed in radians.
 */
sealed class SecondaryDialConfig(
    val index: Int,
    val spread: Int,
    val scale: Float,
    val distance: Float,
    val avoidClipping: Boolean = false,
    val processSecondaryDialRotation: (Float) -> (Float) = { it }
) {
    /**
     * A simple, single button secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a distance factor. Used to move the control away from the primary dial.
     * @property processSecondaryDialRotation When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     * @property buttonConfig The button configuration
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    // TODO Make processSecondaryDialRotation consistent in positioning.
    // TODO Split the large and small button into separate configurations.
    class SingleButton(
        index: Int,
        spread: Int,
        distance: Float,
        val buttonConfig: ButtonConfig,
        processSecondaryDialRotation: (Float) -> (Float) = { it },
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, spread, 1f, distance, spread != 1, processSecondaryDialRotation)

    /**
     * A secondary Stick dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a distance factor. Used to move the control away from the primary dial.
     * @property id The id returned when its events are fired.
     * @property buttonPressId The optional id fired when the stick is double tapped.
     * @property supportsGestures The set of gestures that the button can emit. Defaults to empty.
     * @property contentDescription Content description read by the screen reader. Defaults to "Stick".
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     * @property processSecondaryDialRotation When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Stick(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        val id: Int,
        val buttonPressId: Int? = null,
        val supportsGestures: Set<GestureType> = emptySet(),
        val contentDescription: String = "Stick",
        val theme: RadialGamePadTheme? = null,
        processSecondaryDialRotation: (Float) -> (Float) = { it }
    ) : SecondaryDialConfig(index, spread, scale, distance, false, processSecondaryDialRotation)

    /**
     * Represents a simple DPAD secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a distance factor. Used to move the control away from the primary dial.
     * @property crossConfig The cross configuration.
     * @property processSecondaryDialRotation When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Cross(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        val crossConfig: CrossConfig,
        processSecondaryDialRotation: (Float) -> (Float) = { it }
    ) : SecondaryDialConfig(index, spread, scale, distance, false, processSecondaryDialRotation)

    /**
     * An empty dial, that gets considered when measuring the gamepad. Useful for creating symmetric pads.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a distance factor. Used to move the control away from the primary dial.
     * @property processSecondaryDialRotation When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Empty(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        processSecondaryDialRotation: (Float) -> (Float) = { it }
    ) : SecondaryDialConfig(index, spread, scale, distance, false, processSecondaryDialRotation)
}
