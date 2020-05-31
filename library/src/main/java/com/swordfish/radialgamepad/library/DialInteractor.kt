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
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.touchbound.EmptyTouchBound
import com.swordfish.radialgamepad.library.touchbound.TouchBound
import com.swordfish.radialgamepad.library.utils.TouchUtils

internal class DialInteractor(val dial: Dial, var touchBound: TouchBound = EmptyTouchBound) {

    fun trackedPointerId() = dial.trackedPointerId()

    fun measure(drawingRect: RectF) {
        dial.measure(drawingRect)
    }

    fun draw(canvas: Canvas) {
        dial.draw(canvas)
    }

    fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        val goodFingers = fingers
            .filter { touchBound.contains(it.x, it.y) || it.pointerId == trackedPointerId() }

        return dial.touch(TouchUtils.computeRelativeFingerPosition(goodFingers, dial.drawingBox()))
    }

    fun gesture(x: Float, y: Float, gestureType: GestureType) {
        if (touchBound.contains(x, y)) {
            val relativePosition = TouchUtils.computeRelativePosition(x, y, dial.drawingBox())
            dial.gesture(relativePosition.x, relativePosition.y, gestureType)
        }
    }
}