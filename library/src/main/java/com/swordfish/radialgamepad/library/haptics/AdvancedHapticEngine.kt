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

import android.view.View
import android.view.ViewConfiguration
import com.swordfish.radialgamepad.library.event.Event

class AdvancedHapticEngine : HapticEngine() {

    private var lastPress = 0L
    private val timeout = ViewConfiguration.getTapTimeout()

    override fun performHapticForEvents(events: List<Event>, view: View) {
        val currentTime = System.currentTimeMillis()

        val strongestEffect = events.asSequence()
            .map { it.haptic }
            .max() ?: EFFECT_NONE

        if (strongestEffect == EFFECT_PRESS) {
            lastPress = currentTime
        }

        if (requiresThrottling(strongestEffect, currentTime)) {
            return
        }

        performHaptic(strongestEffect, view)
    }

    private fun requiresThrottling(effect: Int, currentTime: Long) =
        effect == EFFECT_RELEASE && currentTime < lastPress + timeout
}
