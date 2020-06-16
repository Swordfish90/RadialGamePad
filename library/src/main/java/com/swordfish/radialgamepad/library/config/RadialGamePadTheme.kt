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

import com.swordfish.radialgamepad.library.utils.Constants
/**
 * The Cross dial represents a simple DPAD with diagonals.
 * @property normalColor A color which is displayed when controls are in default state
 * @property pressedColor A color which is displayed when controls are pressed
 * @property textColor A color which is used to draw labels or icons on top of controls
 * @property primaryDialBackground A color which is used to draw the circular background behind the primary dial
 * @property secondaryDialBackground A color which is used to draw the circular background behind the secondary dials
 */
data class RadialGamePadTheme(
    val normalColor: Int = Constants.DEFAULT_COLOR_NORMAL,
    val pressedColor: Int = Constants.DEFAULT_COLOR_PRESSED,
    val textColor: Int = Constants.DEFAULT_COLOR_TEXT,
    val primaryDialBackground: Int = Constants.DEFAULT_COLOR_BACKGROUND,
    val secondaryDialBackground: Int = Constants.DEFAULT_COLOR_SECONDARY_BACKGROUND
)