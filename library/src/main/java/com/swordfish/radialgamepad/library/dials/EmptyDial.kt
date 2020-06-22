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

package com.swordfish.radialgamepad.library.dials

import android.graphics.Canvas
import android.graphics.RectF
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable

class EmptyDial : Dial {

    override fun trackedPointerId(): Int? = null

    override fun drawingBox(): RectF = RectF()

    override fun measure(drawingBox: RectF) {}

    override fun draw(canvas: Canvas) {}

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean = false

    override fun events(): Observable<Event> = Observable.empty()

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType): Boolean = false
}
