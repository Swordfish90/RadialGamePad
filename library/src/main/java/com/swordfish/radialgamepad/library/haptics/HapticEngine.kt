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
import android.view.View
import com.swordfish.radialgamepad.library.event.Event

abstract class HapticEngine {

    abstract fun performHapticForEvents(events: List<Event>, view: View)

    fun performHaptic(hapticEffect: Int, view: View) {
        HAPTIC_CONSTANTS_MAPPINGS[hapticEffect]?.let {
            view.performHapticFeedback(it, VIEW_FLAGS)
        }
    }

    companion object {
        const val EFFECT_NONE = 0
        const val EFFECT_RELEASE = 1
        const val EFFECT_PRESS = 2

        private const val VIEW_FLAGS = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING

        private val HAPTIC_CONSTANTS_MAPPINGS = mapOf(
            EFFECT_NONE to null,
            EFFECT_PRESS to HapticFeedbackConstants.VIRTUAL_KEY,
            EFFECT_RELEASE to findReleaseHapticConstant()
        )

        private fun findReleaseHapticConstant(): Int? {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                HapticFeedbackConstants.VIRTUAL_KEY_RELEASE
            } else {
                null
            }
        }
    }
}
