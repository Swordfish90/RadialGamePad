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

// TODO Check ordering of parameters and documentation

/**
 * The base secondary dial configuration.
 * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
 * @property spread Defines how many secondary dials is occupies
 * @property scale Defines a scaling factor. Used to make some controls more prominent.
 * @property avoidClipping When measuring, the library is not allowed to clip the area not occupied by the drawing box.
 * @property rotationProcessor When set you can modify the final rotation value that will be applied to this dial. Rotation is expressed in radians.
 */
sealed class SecondaryDialConfig(
    val index: Int,
    val spread: Int,
    val scale: Float,
    val spacing: Float,
    val avoidClipping: Boolean = false,
    val rotationProcessor: RotationProcessor = RotationProcessor()
) {
    /**
     * A simple, single button secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property rotationProcessor When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     * @property buttonConfig The button configuration
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    class SingleButton(
        index: Int,
        scale: Float,
        spacing: Float,
        val buttonConfig: ButtonConfig,
        rotationProcessor: RotationProcessor = RotationProcessor(),
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, 1, scale, spacing, false, rotationProcessor)

    /**
     * A button with a double spread shaped like a bean.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property rotationProcessor When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     * @property buttonConfig The button configuration
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     */
    class DoubleButton(
        index: Int,
        spacing: Float,
        val buttonConfig: ButtonConfig,
        rotationProcessor: RotationProcessor = RotationProcessor(),
        val theme: RadialGamePadTheme? = null
    ) : SecondaryDialConfig(index, 2, 1f, spacing, true, rotationProcessor)

    /**
     * A secondary Stick dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property id The id returned when its events are fired.
     * @property buttonPressId The optional id fired when the stick is double tapped.
     * @property supportsGestures The set of gestures that the button can emit. Defaults to empty.
     * @property contentDescription Content description read by the screen reader. Defaults to "Stick".
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     * @property rotationProcessor When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Stick(
        index: Int,
        spread: Int,
        scale: Float,
        spacing: Float,
        val id: Int,
        val buttonPressId: Int? = null,
        val supportsGestures: Set<GestureType> = emptySet(),
        val contentDescription: String = "Stick",
        val theme: RadialGamePadTheme? = null,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, spacing, false, rotationProcessor)

    /**
     * Represents a simple DPAD secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property crossConfig The cross configuration.
     * @property rotationProcessor When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Cross(
        index: Int,
        spread: Int,
        scale: Float,
        spacing: Float,
        val crossConfig: CrossConfig,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, spacing, false, rotationProcessor)

    /**
     * An empty dial, that gets considered when measuring the gamepad. Useful for creating symmetric pads.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property rotationProcessor When set you can modify the secondaryDialRotation value that will be applied to this dial. Rotation is expressed in degrees.
     */
    class Empty(
        index: Int,
        spread: Int,
        scale: Float,
        spacing: Float,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, spacing, false, rotationProcessor)

    open class RotationProcessor {
        open fun getRotation(rotation: Float): Float {
            return rotation
        }

        open fun getSpacing(originalSpacing: Float, rotation: Float): Float {
            return originalSpacing
        }
    }
}
