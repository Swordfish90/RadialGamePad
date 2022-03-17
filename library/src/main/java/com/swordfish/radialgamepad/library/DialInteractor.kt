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

package com.swordfish.radialgamepad.library

import android.graphics.Canvas
import android.graphics.RectF
import com.swordfish.radialgamepad.library.dials.Dial
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.touchbound.EmptyTouchBound
import com.swordfish.radialgamepad.library.touchbound.TouchBound
import com.swordfish.radialgamepad.library.utils.TouchUtils

// TODO Get rid of this class
internal class DialInteractor(val dial: Dial, var touchBound: TouchBound = EmptyTouchBound) {

    fun measure(drawingRect: RectF, secondarySector: Sector? = null) {
        dial.measure(drawingRect, secondarySector)
    }

    fun draw(canvas: Canvas) {
        dial.draw(canvas)
    }

    fun trackedPointersIds(): Set<Int> {
        return dial.trackedPointersIds()
    }

    fun touch(fingers: List<TouchUtils.FingerPosition>, outEvents: MutableList<Event>): Boolean {
        val relativePositions = TouchUtils.computeRelativeFingerPosition(fingers, dial.drawingBox())
        return dial.touch(relativePositions, outEvents)
    }

    fun gesture(
        x: Float,
        y: Float,
        gestureType: GestureType,
        outEvents: MutableList<Event>
    ): Boolean {
        if (touchBound.contains(x, y)) {
            val relativePosition = TouchUtils.computeRelativePosition(x, y, dial.drawingBox())
            return dial.gesture(relativePosition.x, relativePosition.y, gestureType, outEvents)
        }

        return false
    }
}
