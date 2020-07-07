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
 * Configuration for a single button.
 * @property id The control id. It is passed back to discriminate events.
 * @property label A text string to be displayed on top of the button
 * @property visible Prevent this button from being shown on screen
 * @property iconId A drawable resource id to an icon which is displayed on top of the button
 * @property contentDescription Content description read by screen reader. By default is label.
 * @property theme A RadialGamePadTheme specific for this dial. If omitted the RadialGamePad one is used.
 */
data class ButtonConfig(
    val id: Int,
    val label: String? = null,
    val visible: Boolean = true,
    val iconId: Int? = null,
    val contentDescription: String? = label,
    val theme: RadialGamePadTheme? = null
)