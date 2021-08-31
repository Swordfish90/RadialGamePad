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

package com.swordfish.radialgamepad

import android.view.KeyEvent
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.PrimaryDialConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadConfig
import com.swordfish.radialgamepad.library.config.SecondaryDialConfig

object SamplePadConfigs {

    val GB_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(0),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    10, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "SELECT"
                    )
                )
            )
        )

    val GB_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        label = "A"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B"
                    )
                ),
                rotationInDegrees = 30f
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    8, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                )
            )
        )
    val PSX_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(0),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "SELECT"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "L1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "L2"
                    )
                ),
                // When this stick is double tapped, it's going to fire a Button event
                SecondaryDialConfig.Stick(
                    9,
                    2.2f,
                    1,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    contentDescription = "Left Stick"
                )
            )
        )

    val PSX_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                listOf(
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_A,
                        iconId = R.drawable.psx_circle,
                        contentDescription = "Circle"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_X,
                        iconId = R.drawable.psx_triangle,
                        contentDescription = "Triangle"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_Y,
                        iconId = R.drawable.psx_square,
                        contentDescription = "Square"
                    ),
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_B,
                        iconId = R.drawable.psx_cross,
                        contentDescription = "Cross"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R2,
                        label = "R2"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "R1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                ),
                // When this stick is double tapped, it's going to fire a Button event
                SecondaryDialConfig.Stick(
                    8,
                    2.2f,
                    2,
                    KeyEvent.KEYCODE_BUTTON_THUMBL,
                    contentDescription = "Right Stick"
                )
            )
        )

    val REMOTE =
        RadialGamePadConfig(
            sockets = 6,
            primaryDial = PrimaryDialConfig.Cross(0, useDiagonals = false),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    1, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_SELECT,
                        iconId = R.drawable.ic_play
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    2, 1, ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_L1,
                        iconId = R.drawable.ic_stop
                    )
                ),
                SecondaryDialConfig.SingleButton(4, 1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_MODE,
                        iconId = R.drawable.ic_volume_down
                    )
                ),
                SecondaryDialConfig.SingleButton(5, 1,
                    ButtonConfig(
                        id = KeyEvent.KEYCODE_BUTTON_MODE,
                        iconId = R.drawable.ic_volume_up
                    )
                )
            )
        )
}