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

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.simulation.SimulateMotionDial
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import java.lang.Math.toDegrees
import kotlin.math.*

class CrossDial(
    context: Context,
    private val id: Int,
    normalDrawableId: Int,
    pressedDrawableId: Int,
    foregroundDrawableId: Int?,
    private val supportsGestures: Set<GestureType>,
    private val contentDescription: CrossContentDescription,
    theme: RadialGamePadTheme
) : SimulateMotionDial {

    companion object {
        private const val ACCESSIBILITY_BOX_SCALE = 0.33f
        private const val DRAWABLE_SIZE_SCALING = 0.75
        private const val BUTTON_COUNT = 8
        private const val SINGLE_BUTTON_ANGLE = Constants.PI2 / BUTTON_COUNT
        private const val ROTATE_BUTTONS = Constants.PI2 / 16f

        private const val DEAD_ZONE = 0.1f

        const val BUTTON_RIGHT = 0
        const val BUTTON_DOWN_RIGHT = 1
        const val BUTTON_DOWN = 2
        const val BUTTON_DOWN_LEFT = 3
        const val BUTTON_LEFT = 4
        const val BUTTON_UP_LEFT = 5
        const val BUTTON_UP = 6
        const val BUTTON_UP_RIGHT = 7

        private val DRAWABLE_BUTTONS = setOf(
            BUTTON_RIGHT,
            BUTTON_DOWN,
            BUTTON_LEFT,
            BUTTON_UP
        )
    }

    private val eventsRelay = PublishRelay.create<Event>()

    private var buttonCenterDistance: Float = 0.45f

    private var normalDrawable: Drawable = context.getDrawable(normalDrawableId)!!.apply {
        setTint(theme.normalColor)
    }

    private var pressedDrawable: Drawable = context.getDrawable(pressedDrawableId)!!.apply {
        setTint(theme.pressedColor)
    }

    private var simulatedDrawable: Drawable = context.getDrawable(pressedDrawableId)!!.apply {
        setTint(theme.simulatedColor)
    }

    private var foregroundDrawable: Drawable? = foregroundDrawableId?.let {
        context.getDrawable(it)!!.apply { setTint(theme.textColor) }
    }

    private val paint = BasePaint().apply {
        color = theme.primaryDialBackground
    }

    private var trackedPointerId: Int? = null

    private var currentIndex: Int? = null

    private var simulatedCurrentIndex: Int? = null

    private var drawingBox = RectF()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = trackedPointerId

    private fun composeDescriptionString(direction: String): String {
        return "${contentDescription.baseName} $direction"
    }

    override fun measure(drawingBox: RectF, secondarySector: Sector?) {
        this.drawingBox = drawingBox
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType): Boolean {
        // Gestures are fired only when happening in the dead zone.
        // There is a huge risk of false events in CrossDials.
        if (isInsideDeadZone(relativeX - 0.5f, relativeY - 0.5f) && gestureType in supportsGestures) {
            eventsRelay.accept(Event.Gesture(id, gestureType))
            return false
        }

        return false
    }

    override fun draw(canvas: Canvas) {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        val drawableSize = (radius * DRAWABLE_SIZE_SCALING).roundToInt()

        canvas.drawCircle(drawingBox.centerX(), drawingBox.centerY(), radius, paint)

        val pressedButtons = convertDiagonals(simulatedCurrentIndex ?: currentIndex)

        for (i in 0..BUTTON_COUNT) {
            val cAngle = SINGLE_BUTTON_ANGLE * i

            val isPressed = i in pressedButtons

            getStateDrawable(i, isPressed)?.let {
                val angle = (cAngle - ROTATE_BUTTONS + SINGLE_BUTTON_ANGLE / 2f).toDouble()
                val left = drawingBox.left + (radius * buttonCenterDistance * cos(angle) + radius).toInt() - drawableSize / 2
                val top = drawingBox.top + (radius * buttonCenterDistance * sin(angle) + radius).toInt() - drawableSize / 2
                val xPivot = left + drawableSize / 2f
                val yPivot = top + drawableSize / 2f

                val rotationInDegrees = i * toDegrees(SINGLE_BUTTON_ANGLE.toDouble()).toFloat()

                canvas.save()

                canvas.rotate(rotationInDegrees, xPivot, yPivot)
                it.setBounds(left.roundToInt(), top.roundToInt(), (left + drawableSize).roundToInt(), (top + drawableSize).roundToInt())
                it.draw(canvas)

                foregroundDrawable?.apply {
                    setBounds(left.roundToInt(), top.roundToInt(), (left + drawableSize).roundToInt(), (top + drawableSize).roundToInt())
                    draw(canvas)
                }

                canvas.restore()
            }
        }
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        if (fingers.isEmpty()) return reset()

        if (trackedPointerId == null) {
            val finger = fingers.first()
            trackedPointerId = finger.pointerId
            return handleTouchEvent(
                computeIndexForPosition(finger.x - 0.5f, finger.y - 0.5f),
                simulatedCurrentIndex
            )
        } else {
            val trackedFinger = fingers
                .firstOrNull { it.pointerId == trackedPointerId } ?: return reset()

            return handleTouchEvent(
                computeIndexForPosition(trackedFinger.x - 0.5f, trackedFinger.y - 0.5f),
                simulatedCurrentIndex
            )
        }
    }

    private fun reset(): Boolean {
        val emitUpdate = simulatedCurrentIndex ?: currentIndex != null

        currentIndex = null
        trackedPointerId = null
        simulatedCurrentIndex = null

        if (emitUpdate) {
            eventsRelay.accept(Event.Direction(id, 0f, 0f, false))
        }

        return emitUpdate
    }

    private fun handleTouchEvent(index: Int?, simulatedIndex: Int?): Boolean {
        val finalIndex = simulatedIndex ?: index
        val finalCurrentIndex = simulatedCurrentIndex ?: currentIndex

        if (finalIndex != finalCurrentIndex) {
            if (finalIndex == null) {
                eventsRelay.accept(Event.Direction(id, 0f, 0f, false))
            } else {
                val haptic = finalCurrentIndex?.let { prevIndex -> (prevIndex % 2) == 0 } ?: true
                eventsRelay.accept(
                    Event.Direction(
                        id,
                        cos(finalIndex * SINGLE_BUTTON_ANGLE),
                        sin(finalIndex * SINGLE_BUTTON_ANGLE),
                        haptic
                    )
                )
            }
        }

        currentIndex = index
        simulatedCurrentIndex = simulatedIndex

        return finalIndex != finalCurrentIndex
    }

    override fun simulateMotion(id: Int, relativeX: Float, relativeY: Float): Boolean {
        if (id != this.id) return false

        handleTouchEvent(currentIndex, computeIndexForPosition(relativeX - 0.5f, relativeY - 0.5f))
        return true
    }

    override fun clearSimulatedMotion(id: Int): Boolean {
        if (id != this.id) return false
        reset()
        return true
    }

    private fun computeIndexForPosition(x: Float, y: Float): Int? {
        if (isInsideDeadZone(x, y)) {
            return null
        }

        val angle = (atan2(y, x) + Constants.PI2) % Constants.PI2
        return angleToIndex(angle)
    }

    private fun isInsideDeadZone(x: Float, y: Float) = abs(x) < DEAD_ZONE && abs(y) < DEAD_ZONE

    private fun angleToIndex(angle: Float): Int {
        val sector = Constants.PI2 / 12f
        return when (floor(angle / sector).toInt()) {
            1 -> BUTTON_DOWN_RIGHT
            2, 3 -> BUTTON_DOWN
            4 -> BUTTON_DOWN_LEFT
            5, 6 -> BUTTON_LEFT
            7 -> BUTTON_UP_LEFT
            8, 9 -> BUTTON_UP
            10 -> BUTTON_UP_RIGHT
            else -> BUTTON_RIGHT
        }
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        val offsetSize = drawingBox.width() * 0.25f

        val upRect = drawingBox.scaleCentered(ACCESSIBILITY_BOX_SCALE).apply {
            offset(0f, -offsetSize)
        }

        val leftRect = drawingBox.scaleCentered(ACCESSIBILITY_BOX_SCALE).apply {
            offset(-offsetSize, 0f)
        }

        val rightRect = drawingBox.scaleCentered(ACCESSIBILITY_BOX_SCALE).apply {
            offset(offsetSize, 0f)
        }

        val downRect = drawingBox.scaleCentered(ACCESSIBILITY_BOX_SCALE).apply {
            offset(0f, offsetSize)
        }


        return listOf(
            AccessibilityBox(upRect.roundToInt(), composeDescriptionString(contentDescription.up)),
            AccessibilityBox(leftRect.roundToInt(), composeDescriptionString(contentDescription.left)),
            AccessibilityBox(rightRect.roundToInt(), composeDescriptionString(contentDescription.right)),
            AccessibilityBox(downRect.roundToInt(), composeDescriptionString(contentDescription.down))
        )
    }

    override fun events(): Observable<Event> = eventsRelay

    private fun getStateDrawable(index: Int, isPressed: Boolean): Drawable? {
        return if (index in DRAWABLE_BUTTONS) {
            when {
                isPressed -> pressedDrawable
                simulatedCurrentIndex != null -> simulatedDrawable
                else -> normalDrawable
            }
        } else {
            null
        }
    }

    private fun convertDiagonals(index: Int?): Set<Int> {
        return when (index) {
            BUTTON_DOWN_RIGHT -> setOf(
                BUTTON_DOWN,
                BUTTON_RIGHT
            )
            BUTTON_DOWN_LEFT -> setOf(
                BUTTON_DOWN,
                BUTTON_LEFT
            )
            BUTTON_UP_LEFT -> setOf(
                BUTTON_UP,
                BUTTON_LEFT
            )
            BUTTON_UP_RIGHT -> setOf(
                BUTTON_UP,
                BUTTON_RIGHT
            )
            null -> setOf()
            else -> setOf(index)
        }
    }
}