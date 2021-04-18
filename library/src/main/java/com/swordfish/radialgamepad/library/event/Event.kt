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

package com.swordfish.radialgamepad.library.event

abstract class Event(open val haptic: Boolean = false) {

    /**
     * Represents an composite gesture such as tap, or double tap.
     * @property id The control id that was passed in RadialGamePadConfig.
     * @property type The type of gesture.
     */
    data class Gesture(
        val id: Int,
        val type: GestureType,
        override val haptic: Boolean
    ) : Event()

    /**
     * Represents a low level button event.
     * @property id The control id that was passed in RadialGamePadConfig.
     * @property action The type of action: KeyEvent.ACTION_UP or KeyEvent.ACTION_DOWN
     */
    data class Button(
        val id: Int,
        val action: Int,
        override val haptic: Boolean
    ) : Event()

    /**
     * Represents direction event. Fired by Cross or Stick dials.
     * @property id The control id that was passed in RadialGamePadConfig.
     * @property xAxis Value of the x axis
     * @property yAxis Value of the y axis
     */
    data class Direction(
        val id: Int,
        val xAxis: Float,
        val yAxis: Float,
        override val haptic: Boolean
    ) : Event()
}