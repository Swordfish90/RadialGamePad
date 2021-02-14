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

package com.swordfish.radialgamepad.library.haptics

import android.view.HapticFeedbackConstants
import android.view.ViewConfiguration
import com.swordfish.radialgamepad.library.event.Event

class AdvancedHapticEngine : HapticEngine {

    private val longTimeout = ViewConfiguration.getDoubleTapTimeout()
    private val shortTimeout = ViewConfiguration.getTapTimeout()

    private var lastPress = 0L

    override fun retrieveHaptics(events: List<Event>): Int? {
        val currentTime = System.currentTimeMillis()

        val types = events.asSequence()
            .map { it.haptic }
            .toSet()

        if (types.contains(HapticEngine.EFFECT_PRESS)) {
            lastPress = currentTime
            return KEYBOARD_PRESS
        } else if (types.contains(HapticEngine.EFFECT_TICK) && currentTime - lastPress > shortTimeout) {
            lastPress = currentTime
            return KEYBOARD_TICK
        } else if (types.contains(HapticEngine.EFFECT_RELEASE) && currentTime - lastPress > longTimeout) {
            return KEYBOARD_RELEASE
        }

        return null
    }

    companion object {
        private val KEYBOARD_PRESS = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.KEYBOARD_PRESS
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }

        private val KEYBOARD_TICK = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.CLOCK_TICK
        } else {
            null
        }

        private val KEYBOARD_RELEASE = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            HapticFeedbackConstants.KEYBOARD_RELEASE
        } else {
            null
        }
    }
}
