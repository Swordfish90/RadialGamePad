/*
 * Created by Filippo Scognamiglio.
 * Copyright (c) 2021. This file is part of RadialGamePad.
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

package com.swordfish.radialgamepad.library.touch

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlin.math.roundToInt

class DebugTouchTracker : TouchTracker {

    private var estimatedScaling: PointF = PointF(1.0f, 1.0f)
    private var viewPosition = intArrayOf(0, 0)
    private var touchDownRawAbsolutePosition: PointF = PointF(0f, 0f)
    private var touchDownAbsolutePosition: PointF = PointF(0f, 0f)
    private var touchDownRelativePosition: PointF = PointF(0f, 0f)
    private val currentPositions: MutableMap<Int, PointF> = mutableMapOf()

    @RequiresApi(29)
    override fun onTouch(view: View, event: MotionEvent) {
        view.getLocationOnScreen(viewPosition)

        val pointerId = event.getPointerId(event.actionIndex)
        val pointerIndex = event.findPointerIndex(pointerId)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Huge leap of faith. Let's assume these values are correct.
                touchDownRawAbsolutePosition = PointF(event.rawX, event.rawY)
                touchDownAbsolutePosition = PointF(viewPosition[0] + event.x, viewPosition[1] + event.y)
                touchDownRelativePosition = PointF(event.x, event.y)

                Toast.makeText(view.context, "Down(relative: ${pp(touchDownRelativePosition)}, absolute: ${pp(touchDownAbsolutePosition)}, view: ${pp(PointF(viewPosition[0].toFloat(), viewPosition[1].toFloat()))}", Toast.LENGTH_SHORT).show()
            }

//            MotionEvent.ACTION_POINTER_DOWN -> {
//                currentPositions[pointerId] = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
//            }

            MotionEvent.ACTION_CANCEL -> currentPositions.clear()

            MotionEvent.ACTION_UP -> currentPositions.clear()

            MotionEvent.ACTION_POINTER_UP -> currentPositions.remove(pointerId)

            MotionEvent.ACTION_MOVE -> {
                val points = iteratePointerIndexes(event)
                    .map { (id, index) ->
                        "$index: p(${event.getX(index)}, ${event.getY(index)}), rp:(${event.getRawX(index)}, ${event.getRawY(index)}))"
                    }
                    .joinToString(", ")

                Toast.makeText(view.context, "Move($points)", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun pp(p: PointF): String {
        return "(${p.x.roundToInt()}, ${p.y.roundToInt()})"
    }

    override fun getCurrentPositions(): Sequence<FingerPosition> {
        return currentPositions.asSequence()
            .map { (key, value) -> FingerPosition(key, value.x, value.y) }
    }

    private fun iteratePointerIndexes(event: MotionEvent): Sequence<Pair<Int, Int>> {
        return (0 until event.pointerCount)
            .asSequence()
            .map { event.getPointerId(it) }
            .map { id -> id to event.findPointerIndex(id) }
    }
}
