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

import android.graphics.*
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.utils.MathUtils
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.sin

class StickDial(private val id: Int, private val theme: RadialGamePadTheme) : MotionDial {

    private val paint = BasePaint()

    private val foregroundColor: Int = theme.normalColor
    private val pressedColor: Int = theme.pressedColor

    private val eventsRelay = PublishRelay.create<Event>()

    private var firstTouch: PointF? = null
    private var trackedPointerId: Int? = null

    private var angle: Float = 0f
    private var strength: Float = 0f

    private var drawingBox = RectF()
    private var radius = 0f

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = trackedPointerId

    override fun measure(drawingBox: RectF) {
        this.drawingBox = drawingBox
        this.radius = minOf(drawingBox.width(), drawingBox.height()) / 2
    }

    override fun draw(canvas: Canvas) {
        paint.color = theme.primaryDialBackground
        canvas.drawCircle(
            drawingBox.left + radius,
            drawingBox.top + radius,
            radius * STICK_BACKGROUND_SIZE,
            paint
        )

        val smallRadius = 0.5f * radius

        paint.color = if (firstTouch != null) pressedColor else foregroundColor
        canvas.drawCircle(
            drawingBox.left + radius + cos(angle) * strength * smallRadius,
            drawingBox.top + radius + sin(angle) * strength * smallRadius,
            smallRadius,
            paint
        )
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        if (fingers.isEmpty() && firstTouch == null) {
            return false
        } else if (fingers.isEmpty()) {
            reset()
            return true
        }

        if (trackedPointerId == null) {
            val finger = fingers.first()

            trackedPointerId = finger.pointerId
            firstTouch = PointF(finger.x, finger.y)
            eventsRelay.accept(Event.Direction(id, 0f, 0f, true))
            handleTouchEvent(finger.x, finger.y)
            return true
        } else {
            val finger = fingers
                .firstOrNull { it.pointerId == trackedPointerId }

            if (finger == null) {
                reset()
                return true
            }

            handleTouchEvent(finger.x, finger.y)
            return true
        }
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType) {
        eventsRelay.accept(Event.Gesture(id, gestureType))
    }

    override fun events(): Observable<Event> = eventsRelay

    override fun simulateMotion(id: Int, relativeX: Float, relativeY: Float): Boolean {
        if (id != this.id) return false

        firstTouch = PointF(0.5f, 0.5f)

        handleTouchEvent(relativeX, relativeY)
        return true
    }

    override fun simulateClearMotion(id: Int): Boolean {
        if (id != this.id) return false
        reset()
        return true
    }

    private fun handleTouchEvent(touchX: Float, touchY: Float) {
        firstTouch?.let { firstTouch ->
            angle = -MathUtils.angle(firstTouch.x, touchX, firstTouch.y, touchY)
            strength = MathUtils.clamp(MathUtils.distance(firstTouch.x, touchX, firstTouch.y, touchY) * 2, 0f, 1f)

            val point = MathUtils.convertPolarCoordinatesToSquares(angle, strength)
            eventsRelay.accept(Event.Direction(id, point.x, point.y, false))
        }
    }

    private fun reset() {
        strength = 0f
        angle = 0f
        firstTouch = null
        trackedPointerId = null
        eventsRelay.accept(Event.Direction(id, 0f, 0f, false))
    }

    companion object {
        const val STICK_BACKGROUND_SIZE = 0.75f
    }
}