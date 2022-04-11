/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2022. This file is part of RadialGamePad.
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

package com.swordfish.radialgamepad.library.haptics.actuators

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.swordfish.radialgamepad.library.haptics.HapticEngine

@RequiresApi(Build.VERSION_CODES.Q)
class VibrationEffectActuator(context: Context) : HapticActuator {

    private val vibrationService = getSystemService(context, Vibrator::class.java)
    private val pressEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
    private val releaseEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)

    override fun performHaptic(hapticEffect: Int) {
        val effect = when (hapticEffect) {
            HapticEngine.EFFECT_PRESS -> pressEffect
            HapticEngine.EFFECT_RELEASE -> releaseEffect
            else -> null
        }
        effect?.let {
            vibrationService?.vibrate(it)
        }
    }
}
