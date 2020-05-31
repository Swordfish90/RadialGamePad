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
                    4, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START,
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
                        keyCode = KeyEvent.KEYCODE_BUTTON_A,
                        label = "A"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B"
                    )
                ),
                rotationInDegrees = 30f
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                )
            )
        )

    val GBA_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(0),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "SELECT"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 2, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "L"
                    )
                )
            )
        )

    val GBA_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_A,
                        label = "A"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B"
                    )
                ),
                rotationInDegrees = 30f
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 2, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "R"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START,
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
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "SELECT"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "L1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "L2"
                    )
                ),
                SecondaryDialConfig.Stick(9, 1, 1.2f, 1),
                SecondaryDialConfig.SingleButton(10, 1, 1f,
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_MODE,
                        label = "MENU"
                    )
                ),
                SecondaryDialConfig.SingleButton(8, 1, 1f,
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_MODE,
                        label = "MENU"
                    )
                )
            )
        )

    val PSX_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                listOf(
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_A,
                        label = "A"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_X,
                        label = "X"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_Y,
                        label = "Y"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_B,
                        label = "B"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_R2,
                        label = "R2"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "R1"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    4, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                ),
                SecondaryDialConfig.Stick(9, 2, 2f, 1),
                SecondaryDialConfig.SingleButton(8, 1, 1f,
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_MODE,
                        label = "MENU"
                    )
                )
            )
        )

    val N64_LEFT =
        RadialGamePadConfig(
            sockets = 10,
            primaryDial = PrimaryDialConfig.Cross(0),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "Z"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L1,
                        label = "L"
                    )
                ),
                SecondaryDialConfig.Stick(7, 2, 2.0f, 5)
            )
        )

    val N64_RIGHT =
        RadialGamePadConfig(
            sockets = 10,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                listOf(
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_B,
                        label = "A"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_Y,
                        label = "B"
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L2,
                        label = "Z"
                    )
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_R1,
                        label = "R"
                    )
                ),
                SecondaryDialConfig.SingleButton(
                    3, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START,
                        label = "START"
                    )
                ),
                SecondaryDialConfig.Cross(7, 2, 2.0f, 5)
            )
        )

    val ARCADE_LEFT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.Cross(0),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    4, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_SELECT,
                        label = "S"
                    )
                )
            )
        )

    val ARCADE_RIGHT =
        RadialGamePadConfig(
            sockets = 12,
            primaryDial = PrimaryDialConfig.PrimaryButtons(
                dials = listOf(
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_A
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_R1
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_L1
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START
                    ),
                    ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_MODE
                    ),
                    ButtonConfig(keyCode = -1, visible = false)
                ),
                center = ButtonConfig(
                    keyCode = KeyEvent.KEYCODE_BUTTON_B
                )
            ),
            secondaryDials = listOf(
                SecondaryDialConfig.SingleButton(
                    2, 1, 1f, ButtonConfig(
                        keyCode = KeyEvent.KEYCODE_BUTTON_START
                    )
                )
            )
        )
}