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
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.AdvancedHapticEngine
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.math.MathUtils
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.TouchUtils
import kotlin.math.cos
import kotlin.math.sin

class StickDial(
    private val id: Int,
    private val keyPressId: Int?,
    private val contentDescription: String? = null,
    private val theme: RadialGamePadTheme
) : MotionDial {

    private val paint = BasePaint()

    private val foregroundColor: Int = theme.normalColor
    private val pressedColor: Int = theme.pressedColor
    private val buttonPressedColor = ColorUtils.blendARGB(foregroundColor, pressedColor, 0.5f)

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

        paint.color = if (firstTouch ?: simulatedFirstTouch != null) pressedColor else foregroundColor
        canvas.drawCircle(
            drawingBox.left + radius + cos(angle) * strength * smallRadius,
            drawingBox.top + radius + sin(angle) * strength * smallRadius,
            smallRadius,
            paint
        )
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>, events: MutableList<Event>) {
        // We ignore touch input when simulating motion externally
        if (simulatedFirstTouch != null) return

        if (fingers.isEmpty()) {
            reset(events)
            return
        }

        if (trackedPointerId == null) {
            val finger = fingers.first()

            if (!isCloseToCenter(finger))
                return

            trackedPointerId = finger.pointerId
            firstTouch = PointF(finger.x, finger.y)

            events.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_PRESS))
            handleTouchEvent(finger.x, finger.y)?.let { events.add(it) }
        } else {
            val finger = fingers
                .firstOrNull { it.pointerId == trackedPointerId }

            if (finger == null) {
                reset(events)
                return
            }

            handleTouchEvent(finger.x, finger.y)?.let { events.add(it) }
        }
    }

    private fun isCloseToCenter(finger: TouchUtils.FingerPosition): Boolean {
        return MathUtils.distance(finger.x, 0.5f, finger.y, 0.5f) < 0.6f
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType, events: MutableList<Event>) {
        if (gestureType == GestureType.SINGLE_TAP && keyPressId != null && firstTouch != null) {
            isButtonPressed = true
            events.add(Event.Button(keyPressId, KeyEvent.ACTION_DOWN, HapticEngine.EFFECT_PRESS))
        } else {
            events.add(Event.Gesture(id, gestureType))
        }
    }

    override fun simulateMotion(id: Int, relativeX: Float, relativeY: Float, events: MutableList<Event>) {
        if (id != this.id) return

        simulatedFirstTouch = PointF(0.5f, 0.5f)

        handleTouchEvent(relativeX, relativeY)?.let { events.add(it) }
    }

    override fun simulateClearMotion(id: Int, events: MutableList<Event>) {
        if (id != this.id) return
        return reset(events)
    }

    private fun handleTouchEvent(touchX: Float, touchY: Float): Event? {
        return (firstTouch ?: simulatedFirstTouch)?.let { firstTouch ->
            angle = -MathUtils.angle(firstTouch.x, touchX, firstTouch.y, touchY)
            strength = MathUtils.clamp(MathUtils.distance(firstTouch.x, touchX, firstTouch.y, touchY) * 2, 0f, 1f)

            val point = MathUtils.convertPolarCoordinatesToSquares(angle, strength)
            Event.Direction(id, point.x, point.y, HapticEngine.EFFECT_NONE)
        }
    }

    private fun reset(events: MutableList<Event>) {
        val isStickActive = firstTouch != null || simulatedFirstTouch != null
        val isStickPressed = isButtonPressed

        strength = 0f
        angle = 0f
        firstTouch = null
        simulatedFirstTouch = null
        trackedPointerId = null
        isButtonPressed = false

        if (isStickActive) {
            events.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_NONE))
        }

        if (keyPressId != null && isStickPressed) {
            events.add(Event.Button(keyPressId, KeyEvent.ACTION_UP, HapticEngine.EFFECT_NONE))
        }
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        return contentDescription?.let {
            listOf(AccessibilityBox(drawingBox.roundToInt(), it))
        } ?: emptyList()
    }

    companion object {
        const val STICK_BACKGROUND_SIZE = 0.75f
    }
}
