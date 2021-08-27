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
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.CrossContentDescription
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.MathUtils
import com.swordfish.radialgamepad.library.math.MathUtils.fmod
import com.swordfish.radialgamepad.library.math.MathUtils.isOdd
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.simulation.SimulateMotionDial
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils
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
    private val diagonalRatio: Int,
    distanceFromCenter: Float = 0.5f,
    theme: RadialGamePadTheme
) : SimulateMotionDial {

    companion object {
        private const val ACCESSIBILITY_BOX_SCALE = 0.33f
        private const val DRAWABLE_SIZE_SCALING = 0.8f
        private const val BUTTON_COUNT = 8
        private const val DRAWABLE_COUNT = 4
        private const val SINGLE_BUTTON_ANGLE = Constants.PI2 / BUTTON_COUNT
        private const val SINGLE_DRAWABLE_ANGLE = Constants.PI2 / DRAWABLE_COUNT
        private const val ROTATE_BUTTONS = Constants.PI2 / 8f

        private const val DEAD_ZONE = 0.1f

        const val CROSS_STATE_CENTER = -2
        const val CROSS_STATE_RIGHT = 0
        const val CROSS_STATE_DOWN_RIGHT = 1
        const val CROSS_STATE_DOWN = 2
        const val CROSS_STATE_DOWN_LEFT = 3
        const val CROSS_STATE_LEFT = 4
        const val CROSS_STATE_UP_LEFT = 5
        const val CROSS_STATE_UP = 6
        const val CROSS_STATE_UP_RIGHT = 7

        const val DRAWABLE_RIGHT = 0
        const val DRAWABLE_DOWN = 1
        const val DRAWABLE_LEFT = 2
        const val DRAWABLE_UP = 3
    }

    private var buttonCenterDistance: Float = MathUtils.lint(distanceFromCenter, 0.4f, 0.6f)

    private var normalDrawable: Drawable = getDrawableWithColor(context, normalDrawableId, theme.normalColor)

    private var pressedDrawable: Drawable = getDrawableWithColor(context, pressedDrawableId, theme.pressedColor)

    private var simulatedDrawable: Drawable = getDrawableWithColor(context, pressedDrawableId, theme.simulatedColor)

    private var foregroundDrawable: Drawable? = foregroundDrawableId?.let {
        getDrawableWithColor(context, it, theme.textColor)
    }

    private val paint = BasePaint().apply {
        color = theme.primaryDialBackground
    }

    private val sectorToStateMap: Map<Int, Int> = buildSectorToStateMap()
    private val sectorAngleSize = Constants.PI2 / sectorToStateMap.size
    private val sectorAngleOffset = if (diagonalRatio.isOdd()) sectorAngleSize / 2 else 0f

    private var trackedPointerId: Int? = null

    private var touchState: Int? = null
    private var simulatedState: Int? = null

    private var drawingBox = RectF()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = trackedPointerId

    private fun composeDescriptionString(direction: String): String {
        return "${contentDescription.baseName} $direction"
    }

    private fun getDrawableWithColor(context: Context, drawableId: Int, color: Int): Drawable {
        return context.getDrawable(drawableId)!!.apply {
            setTint(color)
        }
    }

    // We disable touch events when simulating external events.
    private fun isTouchDisabled() = simulatedState != null

    override fun measure(drawingBox: RectF, secondarySector: Sector?) {
        this.drawingBox = drawingBox
    }

    override fun gesture(
        relativeX: Float,
        relativeY: Float,
        gestureType: GestureType,
        outEvents: MutableList<Event>
    ): Boolean {
        // Firing gestures outside the deadzone is very dangerous, as the user tends to click a
        // lot on the DPAD. That's why we disable them when touch is active.
        val shouldFireGesture = isTouchDisabled() || isInsideDeadZone(relativeX - 0.5f, relativeY - 0.5f)

        if (shouldFireGesture && gestureType in supportsGestures) {
            outEvents.add(Event.Gesture(id, gestureType))
            return false
        }

        return false
    }

    override fun draw(canvas: Canvas) {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        val drawableSize = (radius * DRAWABLE_SIZE_SCALING).roundToInt()

        canvas.drawCircle(drawingBox.centerX(), drawingBox.centerY(), radius, paint)

        val pressedButtons = stateToDrawables(currentState())

        for (i in 0..DRAWABLE_COUNT) {
            val cAngle = SINGLE_DRAWABLE_ANGLE * i

            val isPressed = i in pressedButtons

            getStateDrawable(i, isPressed)?.let {
                val angle = (cAngle - ROTATE_BUTTONS + SINGLE_DRAWABLE_ANGLE / 2f).toDouble()
                val left = drawingBox.left + (radius * buttonCenterDistance * cos(angle) + radius).toInt() - drawableSize / 2
                val top = drawingBox.top + (radius * buttonCenterDistance * sin(angle) + radius).toInt() - drawableSize / 2
                val xPivot = left + drawableSize / 2f
                val yPivot = top + drawableSize / 2f

                val rotationInDegrees = i * toDegrees(SINGLE_DRAWABLE_ANGLE.toDouble()).toFloat()

                canvas.save()

                canvas.rotate(rotationInDegrees, xPivot, yPivot)
                it.setBounds(
                    left.roundToInt(),
                    top.roundToInt(),
                    (left + drawableSize).roundToInt(),
                    (top + drawableSize).roundToInt()
                )
                it.draw(canvas)

                foregroundDrawable?.apply {
                    setBounds(
                        left.roundToInt(),
                        top.roundToInt(),
                        (left + drawableSize).roundToInt(),
                        (top + drawableSize).roundToInt()
                    )
                    draw(canvas)
                }

                canvas.restore()
            }
        }
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>, outEvents: MutableList<Event>): Boolean {
        if (isTouchDisabled()) return false

        if (fingers.isEmpty()) return reset(outEvents)

        if (trackedPointerId == null) {
            val finger = fingers.first()
            trackedPointerId = finger.pointerId
            return updateState(
                computeStateForPosition(finger.x - 0.5f, finger.y - 0.5f),
                simulatedState,
                outEvents
            )
        } else {
            val trackedFinger = fingers
                .firstOrNull { it.pointerId == trackedPointerId } ?: return reset(outEvents)

            return updateState(
                computeStateForPosition(trackedFinger.x - 0.5f, trackedFinger.y - 0.5f),
                simulatedState,
                outEvents
            )
        }
    }

    private fun reset(outEvents: MutableList<Event>): Boolean {
        val emitUpdate = currentState() != null

        touchState = null
        trackedPointerId = null
        simulatedState = null

        if (emitUpdate) {
            outEvents.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_RELEASE))
        }

        return emitUpdate
    }

    private fun updateState(touchState: Int?, simulatedState: Int?, outEvents: MutableList<Event>): Boolean {
        val endState = simulatedState ?: touchState
        val startState = currentState()

        if (endState != startState) {
            sendStateUpdateEvent(endState, startState, outEvents)
        }

        this.touchState = touchState
        this.simulatedState = simulatedState

        return endState != startState
    }

    private fun sendStateUpdateEvent(endState: Int?, startState: Int?, outEvents: MutableList<Event>) {
        if (endState == null || !isActiveState(endState)) {
            outEvents.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_NONE))
        } else {
            val haptic = startState?.let { (it % 2) == 0 } ?: true
            val hapticEffect = if (haptic) HapticEngine.EFFECT_PRESS else HapticEngine.EFFECT_RELEASE
            outEvents.add(
                Event.Direction(
                    id,
                    cos(endState * SINGLE_BUTTON_ANGLE),
                    sin(endState * SINGLE_BUTTON_ANGLE),
                    hapticEffect
                )
            )
        }
    }

    private fun currentState(): Int? = simulatedState ?: touchState

    override fun simulateMotion(
        id: Int,
        relativeX: Float,
        relativeY: Float,
        outEvents: MutableList<Event>
    ): Boolean {
        if (id != this.id) return false
        val simulatedState = computeStateForPosition(relativeX - 0.5f, relativeY - 0.5f)
        updateState(touchState, simulatedState, outEvents)
        return true
    }

    override fun clearSimulatedMotion(id: Int, outEvents: MutableList<Event>): Boolean {
        if (id != this.id) return false
        return reset(outEvents)
    }

    private fun computeStateForPosition(x: Float, y: Float): Int {
        if (isInsideDeadZone(x, y)) {
            return CROSS_STATE_CENTER
        }

        val angle = atan2(y, x).fmod(Constants.PI2)
        return computeStateForAngle(angle)
    }

    private fun isInsideDeadZone(x: Float, y: Float) = abs(x) < DEAD_ZONE && abs(y) < DEAD_ZONE

    private fun computeStateForAngle(angle: Float): Int {
        val sectorIndex = floor((angle + sectorAngleOffset) / sectorAngleSize)
            .toInt()
            .fmod(sectorToStateMap.size)

        return sectorToStateMap[sectorIndex] ?: CROSS_STATE_CENTER
    }

    private fun buildSectorToStateMap(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()

        // Diagonals should be smaller and harder to choose compared to primary directions. We divide
        // the 360 radius into sectors and assign them to states in a non uniform way. Primary directions
        // get proportionally "diagonalRatio" more sectors. Looks a bit magic but works and it's fast.
        val totalSectors = 4 + 4 * diagonalRatio
        (0 until totalSectors)
            .toList()
            .chunked(diagonalRatio + 1)
            .flatMap { listOf(it.subList(0, diagonalRatio), it.subList(diagonalRatio, it.size)) }
            .mapIndexed { index, sectors ->
                sectors.forEach { result[(it - diagonalRatio / 2).fmod(totalSectors)] = index }
            }

        return result
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

    private fun getStateDrawable(index: Int, isPressed: Boolean): Drawable? {
        if (index !in 0 until DRAWABLE_COUNT)
            return null

        return when {
            isPressed -> pressedDrawable
            simulatedState != null -> simulatedDrawable
            else -> normalDrawable
        }
    }

    private fun isActiveState(state: Int?) = state != null && state != CROSS_STATE_CENTER

    private fun stateToDrawables(state: Int?): Set<Int> {
        return when (state) {
            CROSS_STATE_DOWN_RIGHT -> setOf(
                DRAWABLE_DOWN,
                DRAWABLE_RIGHT
            )
            CROSS_STATE_DOWN_LEFT -> setOf(
                DRAWABLE_DOWN,
                DRAWABLE_LEFT
            )
            CROSS_STATE_UP_LEFT -> setOf(
                DRAWABLE_UP,
                DRAWABLE_LEFT
            )
            CROSS_STATE_UP_RIGHT -> setOf(
                DRAWABLE_UP,
                DRAWABLE_RIGHT
            )
            CROSS_STATE_DOWN -> setOf(DRAWABLE_DOWN)
            CROSS_STATE_UP -> setOf(DRAWABLE_UP)
            CROSS_STATE_LEFT -> setOf(DRAWABLE_LEFT)
            CROSS_STATE_RIGHT -> setOf(DRAWABLE_RIGHT)
            else -> setOf()
        }
    }
}