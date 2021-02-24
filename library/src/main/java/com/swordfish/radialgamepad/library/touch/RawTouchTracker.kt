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

class RawTouchTracker {

    private var estimatedScaling: Float = 1.0f
    private var viewPosition = intArrayOf(0, 0)
    private val currentPositions: MutableMap<Int, PointF> = mutableMapOf()

    fun onTouch(view: View, event: MotionEvent) {
        view.getLocationOnScreen(viewPosition)

        val pointerId = event.getPointerId(event.actionIndex)
        val pointerIndex = event.findPointerIndex(pointerId)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentPositions[pointerId] = PointF(event.x, event.y)
                estimatedScaling = runCatching {
                    (viewPosition[0] + event.x) / event.rawX
                }.getOrDefault(1.0f)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                currentPositions[pointerId] = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
            }

            MotionEvent.ACTION_UP -> currentPositions.remove(pointerId)

            MotionEvent.ACTION_POINTER_UP -> currentPositions.remove(pointerId)

            MotionEvent.ACTION_MOVE -> {
                iteratePointerIndexes(event)
                    .forEach { (id, index) ->
                        currentPositions[id] = PointF(
                            estimatedScaling * event.getRawX(index) - viewPosition[0],
                            estimatedScaling * event.getRawY(index) - viewPosition[1]
                        )
                    }
            }
        }
    }

    fun getCurrentPositions(): Sequence<FingerPosition> {
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
