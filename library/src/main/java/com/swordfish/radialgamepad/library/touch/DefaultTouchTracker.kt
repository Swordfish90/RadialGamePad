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

import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(29)
class DefaultTouchTracker : TouchTracker {

    private var positionOnScreen = intArrayOf(0, 0)
    private val currentPositions: MutableList<FingerPosition> = mutableListOf()

    override fun getCurrentPositions(): Sequence<FingerPosition> {
        return currentPositions.asSequence()
    }

    override fun onTouch(view: View, event: MotionEvent) {
        view.getLocationOnScreen(positionOnScreen)
        currentPositions.clear()
        currentPositions.addAll(extractRawFingersPositions(event, positionOnScreen[0], positionOnScreen[1]))
    }

    @RequiresApi(29)
    fun extractRawFingersPositions(event: MotionEvent, offsetX: Int = 0, offsetY: Int = 0): Sequence<FingerPosition> {
        return iteratePointerIndexes(event)
            .map { (id, index) ->
                FingerPosition(id, event.getRawX(index) - offsetX, event.getRawY(index) - offsetY)
            }
    }

    private fun iteratePointerIndexes(event: MotionEvent): Sequence<Pair<Int, Int>> {
        return (0 until event.pointerCount)
            .asSequence()
            .map { event.getPointerId(it) }
            .map { id -> id to event.findPointerIndex(id) }
            .filter { (_, index) -> !isCancelEvent(event, index) }
    }

    private fun isCancelEvent(event: MotionEvent, pointerIndex: Int): Boolean {
        val isUpAction = event.actionMasked in setOf(MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL)
        val isRelatedToCurrentIndex = event.actionIndex == pointerIndex
        return isUpAction && isRelatedToCurrentIndex
    }
}