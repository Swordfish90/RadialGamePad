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
 * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
 * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
 * @property avoidClipping When measuring, the library is not allowed to clip the area not occupied by the drawing box.
 */
sealed class SecondaryDialConfig(
    val index: Int,
    val spread: Int,
    val scale: Float,
    val distance: Float,
    val rotationProcessor: RotationProcessor = RotationProcessor(),
    internal val avoidClipping: Boolean = false
) {
    /**
     * A simple, single button secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
     * @property buttonConfig The button configuration.
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
     */
    class SingleButton(
        index: Int,
        scale: Float,
        distance: Float,
        val buttonConfig: ButtonConfig,
        val theme: RadialGamePadTheme? = null,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, 1, scale, distance, rotationProcessor)

    /**
     * A button with a double spread shaped like a bean.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
     * @property buttonConfig The button configuration.
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
     */
    class DoubleButton(
        index: Int,
        distance: Float,
        val buttonConfig: ButtonConfig,
        val theme: RadialGamePadTheme? = null,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, 2, 1f, distance, rotationProcessor, true)

    /**
     * A secondary Stick dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
     * @property id The id returned when its events are fired.
     * @property buttonPressId The optional id fired when the stick is double tapped.
     * @property theme A theme for this specific dial. By default it inherits the gamepad theme.
     * @property supportsGestures The set of gestures that the button can emit. Defaults to empty.
     * @property contentDescription Content description read by the screen reader. Defaults to "Stick".
     * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
     */
    class Stick(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        val id: Int,
        val buttonPressId: Int? = null,
        val theme: RadialGamePadTheme? = null,
        val supportsGestures: Set<GestureType> = emptySet(),
        val contentDescription: String = "Stick",
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, distance, rotationProcessor)

    /**
     * Represents a simple DPAD secondary dial.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
     * @property crossConfig The cross configuration.
     * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
     */
    class Cross(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        val crossConfig: CrossConfig,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, distance, rotationProcessor)

    /**
     * An empty dial, that gets considered when measuring the gamepad. Useful for creating symmetric pads.
     * @property index The position of the control in the outer circle. It starts from 3:00 and increases counterclockwise.
     * @property spread Defines how many secondary dials is occupies.
     * @property scale Defines a scaling factor. Used to make some controls more prominent.
     * @property distance Defines a displacement factor. Used to control the distance from the primary dial.
     * @property rotationProcessor When set you can modify the final rotation and distance value that will be applied to this dial.
     */
    class Empty(
        index: Int,
        spread: Int,
        scale: Float,
        distance: Float,
        rotationProcessor: RotationProcessor = RotationProcessor()
    ) : SecondaryDialConfig(index, spread, scale, distance, rotationProcessor)

    open class RotationProcessor {
        open fun getRotation(rotation: Float): Float {
            return rotation
        }

        open fun getSpacing(originalSpacing: Float, rotation: Float): Float {
            return originalSpacing
        }
    }
}
