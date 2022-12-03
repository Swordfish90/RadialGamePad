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

import com.swordfish.radialgamepad.library.haptics.HapticConfig

/**
 * The configuration object for a RadialGamePad.
 * @property sockets The maximum number of secondary dials the pad has
 * @property primaryDial Configuration for the central dial
 * @property secondaryDials List of configurations for the surrounding secondary dials
 * @property haptic Choose which effects will be performed
 * @property theme RadialGamePadTheme for the whole view
 * @property preferScreenTouchCoordinates Use an alternative sampling methods for touch coordinates
 */
data class RadialGamePadConfig(
    val sockets: Int,
    val primaryDial: PrimaryDialConfig,
    val secondaryDials: List<SecondaryDialConfig>,
    val haptic: HapticConfig = HapticConfig.PRESS,
    val theme: RadialGamePadTheme = RadialGamePadTheme(),
    val preferScreenTouchCoordinates: Boolean = false
)
