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
import androidx.annotation.RequiresApi

/**
 *  Apparently some devices like to make things hard. After the Android 11 update we're having
 *  a couple of issues on Samsung devices:
 *
 *      1. When in multitouch MotionEvent.getX(index) no longer returns absolute coordinates wrt the
 *         view, but relative coordinates wrt the first touch event. Sadly there is no way to
 *         discriminate between the two kinds, so MotionEvent.getX(index) is basically useless.
 *
 *      2. Samsung Game Mode Plus can scale the screen resolution, by default it's set to 75% to
 *         save some battery. When doing so MotionEvent.getX(index) correctly returns appropriate
 *         scaled coordinates, but MotionEvent.getRawX(index) still returns coordinates in the full
 *         screen resolution.
 *
 *  This generates a bit of trust issues with the data reported in touch events.
 *  The current solution relies on the fact that MotionEvent.getX(index) returns scaled absolute
 *  View coordinates, summing the View position on the screen we get scaled absolute screen
 *  coordinates. Dividing this value by MotionEvent.getRawX(index) which are non-scaled absolute
 *  screen coordinates we can infer scaling, and later compute scaled absolute screen coordinates
 *  from ACTION_MOVE with MotionEvent.getRawX(index) values. */

class FixSamsung2TouchTracker : TouchTracker {

    private var touchDownRawAbsolutePosition: PointF = PointF(0f, 0f)
    private var touchDownRelativePosition: PointF = PointF(0f, 0f)
    private val currentPositions: MutableMap<Int, PointF> = mutableMapOf()

    @RequiresApi(29)
    override fun onTouch(view: View, event: MotionEvent) {
        val pointerId = event.getPointerId(event.actionIndex)
        val pointerIndex = event.findPointerIndex(pointerId)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                // Huge leap of faith. Let's assume these values are correct.
                touchDownRawAbsolutePosition = PointF(event.rawX, event.rawY)
                touchDownRelativePosition = PointF(event.x, event.y)

                currentPositions[pointerId] = touchDownRelativePosition
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                currentPositions[pointerId] = PointF(event.getX(pointerIndex), event.getY(pointerIndex))
            }

            MotionEvent.ACTION_CANCEL -> currentPositions.clear()

            MotionEvent.ACTION_UP -> currentPositions.clear()

            MotionEvent.ACTION_POINTER_UP -> currentPositions.remove(pointerId)

            MotionEvent.ACTION_MOVE -> {
                iteratePointerIndexes(event)
                    .forEach { (id, index) ->
                        val deltaX = event.getRawX(index) - touchDownRawAbsolutePosition.x
                        val deltaY = event.getRawY(index) - touchDownRawAbsolutePosition.y
                        currentPositions[id] = PointF(
                            touchDownRelativePosition.x + deltaX,
                            touchDownRelativePosition.y + deltaY
                        )
                    }
            }
        }
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
