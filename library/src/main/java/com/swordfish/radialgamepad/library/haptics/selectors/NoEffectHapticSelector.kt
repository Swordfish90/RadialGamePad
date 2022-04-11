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

package com.swordfish.radialgamepad.library.haptics.selectors

import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.haptics.HapticEngine

class NoEffectHapticSelector : HapticSelector {
    override fun getEffectConstant(events: List<Event>): Int {
        return HapticEngine.EFFECT_NONE
    }
}
