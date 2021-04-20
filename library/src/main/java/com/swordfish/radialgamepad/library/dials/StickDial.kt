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
import android.view.KeyEvent
import androidx.core.graphics.ColorUtils
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.math.MathUtils
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.simulation.SimulateMotionDial
import com.swordfish.radialgamepad.library.touch.FingerPosition
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.sin

class StickDial(
    private val id: Int,
    private val keyPressId: Int?,
    private val supportsGestures: Set<GestureType>,
    private val contentDescription: String? = null,
    private val theme: RadialGamePadTheme
) : SimulateMotionDial {

    private val paint = BasePaint()

    private val foregroundColor: Int = theme.normalColor
    private val pressedColor: Int = theme.pressedColor
    private val simulatedColor: Int = theme.simulatedColor
    private val buttonPressedColor = ColorUtils.blendARGB(foregroundColor, pressedColor, 0.5f)

    private val eventsRelay = PublishRelay.create<Event>()

    private var isButtonPressed: Boolean = false
    private var firstTouch: PointF? = null
    private var simulatedFirstTouch: PointF? = null
    private var trackedPointerId: Int? = null

    private var angle: Float = 0f
    private var strength: Float = 0f

    private var drawingBox = RectF()
    private var radius = 0f

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = trackedPointerId

    override fun measure(drawingBox: RectF, secondarySector: Sector?) {
        this.drawingBox = drawingBox
        this.radius = minOf(drawingBox.width(), drawingBox.height()) / 2
    }

    override fun draw(canvas: Canvas) {
        paint.color = if (isButtonPressed) buttonPressedColor else theme.primaryDialBackground
        canvas.drawCircle(
            drawingBox.left + radius,
            drawingBox.top + radius,
            radius * STICK_BACKGROUND_SIZE,
            paint
        )

        val smallRadius = 0.5f * radius

        paint.color = when {
            firstTouch != null -> pressedColor
            simulatedFirstTouch != null -> simulatedColor
            else -> foregroundColor
        }

        canvas.drawCircle(
            drawingBox.left + radius + cos(angle) * strength * smallRadius,
            drawingBox.top + radius + sin(angle) * strength * smallRadius,
            smallRadius,
            paint
        )
    }

    override fun touch(fingers: List<FingerPosition>): Boolean {
        // We ignore touch input when simulating motion externally
        if (simulatedFirstTouch != null) return false

        if (fingers.isEmpty()) return reset()

        if (trackedPointerId == null) {
            val finger = fingers.first()

            if (!isCloseToCenter(finger))
                return false

            trackedPointerId = finger.pointerId
            firstTouch = PointF(finger.x, finger.y)
            eventsRelay.accept(Event.Direction(id, 0f, 0f, true))
            handleTouchEvent(finger.x, finger.y)
            return true
        } else {
            val finger = fingers
                .firstOrNull { it.pointerId == trackedPointerId }
                ?: return reset()

            handleTouchEvent(finger.x, finger.y)
            return true
        }
    }

    private fun isCloseToCenter(finger: FingerPosition): Boolean {
        return MathUtils.distance(finger.x, 0.5f, finger.y, 0.5f) < 0.6f
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType): Boolean {
        return if (gestureType == GestureType.SINGLE_TAP && keyPressId != null && firstTouch != null) {
            isButtonPressed = true
            eventsRelay.accept(Event.Button(keyPressId, KeyEvent.ACTION_DOWN, true))
            true
        } else if (gestureType in supportsGestures) {
            eventsRelay.accept(Event.Gesture(id, gestureType))
            false
        } else {
            false
        }
    }

    override fun events(): Observable<Event> = eventsRelay

    override fun simulateMotion(id: Int, relativeX: Float, relativeY: Float): Boolean {
        if (id != this.id) return false

        simulatedFirstTouch = PointF(0.5f, 0.5f)

        handleTouchEvent(relativeX, relativeY)
        return true
    }

    override fun clearSimulatedMotion(id: Int): Boolean {
        if (id != this.id) return false
        reset()
        return true
    }

    private fun handleTouchEvent(touchX: Float, touchY: Float) {
        (firstTouch ?: simulatedFirstTouch)?.let { firstTouch ->
            angle = -MathUtils.angle(firstTouch.x, touchX, firstTouch.y, touchY)
            strength = MathUtils.clamp(MathUtils.distance(firstTouch.x, touchX, firstTouch.y, touchY) * 2, 0f, 1f)

            val point = MathUtils.convertPolarCoordinatesToSquares(angle, strength)
            eventsRelay.accept(Event.Direction(id, point.x, point.y, false))
        }
    }

    private fun reset(): Boolean {
        val isStickActive = firstTouch != null || simulatedFirstTouch != null
        val isStickPressed = isButtonPressed

        strength = 0f
        angle = 0f
        firstTouch = null
        simulatedFirstTouch = null
        trackedPointerId = null
        isButtonPressed = false

        if (isStickActive) {
            eventsRelay.accept(Event.Direction(id, 0f, 0f, false))
        }

        if (keyPressId != null && isStickPressed) {
            eventsRelay.accept(Event.Button(keyPressId, KeyEvent.ACTION_UP, false))
        }

        return isStickActive || isStickPressed
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        return contentDescription?.let {
            listOf(AccessibilityBox(drawingBox.roundToInt(), it))
        } ?: listOf()
    }

    companion object {
        const val STICK_BACKGROUND_SIZE = 0.75f
    }
}