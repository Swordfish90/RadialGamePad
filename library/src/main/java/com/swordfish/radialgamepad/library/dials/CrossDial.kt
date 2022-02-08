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
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.CrossConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.CompositeButtonPaint
import com.swordfish.radialgamepad.library.paint.FillStrokePaint
import com.swordfish.radialgamepad.library.path.ArrowPathBuilder
import com.swordfish.radialgamepad.library.path.CirclePathBuilder
import com.swordfish.radialgamepad.library.simulation.SimulateMotionDial
import com.swordfish.radialgamepad.library.touch.TouchAnchor
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils
import java.lang.Math.toDegrees
import kotlin.math.*

class CrossDial(
    context: Context,
    private val config: CrossConfig,
    private val theme: RadialGamePadTheme
) : SimulateMotionDial {

    companion object {
        private const val ACCESSIBILITY_BOX_SCALE = 0.33f
        private const val DRAWABLE_SIZE_SCALING = 0.8f
        private const val DRAWABLE_COUNT = 4
        private const val SINGLE_DRAWABLE_ANGLE = Constants.PI2 / DRAWABLE_COUNT

        private const val DEAD_ZONE = 0.1f

        private const val DRAWABLE_INDEX_RIGHT = 0
        private const val DRAWABLE_INDEX_DOWN = 1
        private const val DRAWABLE_INDEX_LEFT = 2
        private const val DRAWABLE_INDEX_UP = 3

        private const val DIAGONAL_DISTANCE = 0.5f
        private const val DIAGONAL_STRENGTH = 1.25f

        private const val MAIN_DISTANCE = 0.5f
        private const val MAIN_STRENGTH = 2f
    }

    private enum class State(val anchor: TouchAnchor, val outEvent: PointF) {
        CROSS_STATE_CENTER(
            TouchAnchor.fromPolar(
                0.0f,
                0.0f,
                0.75f,
                setOf()
            ),
            PointF(0f, 0f)
        ),
        CROSS_STATE_RIGHT(
            TouchAnchor.fromPolar(
                0.0f * Constants.PI,
                MAIN_DISTANCE,
                MAIN_STRENGTH,
                setOf(DRAWABLE_INDEX_RIGHT)
            ),
            PointF(1f, 0f)
        ),
        CROSS_STATE_DOWN_RIGHT(
            TouchAnchor.fromPolar(
                0.25f * Constants.PI,
                DIAGONAL_DISTANCE,
                DIAGONAL_STRENGTH,
                setOf(DRAWABLE_INDEX_DOWN, DRAWABLE_INDEX_RIGHT)
            ),
            PointF(1f, 1f)
        ),
        CROSS_STATE_DOWN(
            TouchAnchor.fromPolar(
                0.5f * Constants.PI,
                MAIN_DISTANCE,
                MAIN_STRENGTH,
                setOf(DRAWABLE_INDEX_DOWN)
            ),
            PointF(0f, 1f)
        ),
        CROSS_STATE_DOWN_LEFT(
            TouchAnchor.fromPolar(
                0.75f * Constants.PI,
                DIAGONAL_DISTANCE,
                DIAGONAL_STRENGTH,
                setOf(DRAWABLE_INDEX_DOWN, DRAWABLE_INDEX_LEFT)
            ),
            PointF(-1f, 1f)
        ),
        CROSS_STATE_LEFT(
            TouchAnchor.fromPolar(
                1.00f * Constants.PI,
                MAIN_DISTANCE,
                MAIN_STRENGTH,
                setOf(DRAWABLE_INDEX_LEFT)
            ),
            PointF(-1f, 0f)
        ),
        CROSS_STATE_UP_LEFT(
            TouchAnchor.fromPolar(
                1.25f * Constants.PI,
                DIAGONAL_DISTANCE,
                DIAGONAL_STRENGTH,
                setOf(DRAWABLE_INDEX_UP, DRAWABLE_INDEX_LEFT)
            ),
            PointF(-1f, -1f)
        ),
        CROSS_STATE_UP(
            TouchAnchor.fromPolar(
                1.50f * Constants.PI,
                MAIN_DISTANCE,
                MAIN_STRENGTH,
                setOf(DRAWABLE_INDEX_UP)
            ),
            PointF(0f, -1f)
        ),
        CROSS_STATE_UP_RIGHT(
            TouchAnchor.fromPolar(
                1.75f * Constants.PI,
                DIAGONAL_DISTANCE,
                DIAGONAL_STRENGTH,
                setOf(DRAWABLE_INDEX_UP, DRAWABLE_INDEX_RIGHT)
            ),
            PointF(1f, -1f)
        );

        fun isDiagonal() = anchor.ids.size > 1
    }

    val id = config.id

    private val normalPaint = FillStrokePaint(context, theme).apply {
        setFillColor(theme.normalColor)
    }

    private val pressedPaint = FillStrokePaint(context, theme).apply {
        setFillColor(theme.pressedColor)
    }

    private val simulatedPaint = FillStrokePaint(context, theme).apply {
        setFillColor(theme.simulatedColor)
    }

    private var foregroundDrawable: Drawable? = config.rightDrawableForegroundId?.let {
        getDrawableWithColor(context, it, theme.textColor)
    }

    private var trackedPointersIds: MutableSet<Int> = mutableSetOf()

    private var touchState: State? = null
    private var simulatedState: State? = null

    private var drawingBox = RectF()
    private var drawableRect = Rect()

    private var shapePath: Path = Path()

    private val backgroundPaint = FillStrokePaint(context, theme).apply {
        setStrokeColor(theme.strokeLightColor)
        setFillColor(theme.primaryDialBackground)
    }

    private val compositeButtonPaint = CompositeButtonPaint(context, theme)

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointersIds(): Set<Int> = trackedPointersIds

    private fun composeDescriptionString(direction: String): String {
        return "${config.contentDescription.baseName} $direction"
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

        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        val drawableSize = (radius * DRAWABLE_SIZE_SCALING).roundToInt()

        this.drawableRect = Rect(
            -drawableSize / 2 + (radius).roundToInt() / 2,
            -drawableSize / 2,
            drawableSize / 2 + (radius).roundToInt() / 2,
            drawableSize / 2
        )

        shapePath = buildPath(config.shape, drawableRect)

        compositeButtonPaint.updateDrawingBox(drawingBox)
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

        if (shouldFireGesture && gestureType in config.supportsGestures) {
            outEvents.add(Event.Gesture(id, gestureType))
            return false
        }

        return false
    }

    override fun draw(canvas: Canvas) {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2

        drawBackground(canvas, radius)

        if (config.useDiagonals) {
            drawDiagonalDirections(canvas, radius)
        }

        drawMainDirections(canvas)
    }

    private fun drawMainDirections(canvas: Canvas) {
        canvas.save()
        canvas.translate(drawingBox.centerX(), drawingBox.centerY())

        val pressedButtons = currentState()?.anchor?.ids ?: setOf()

        getMainStates()
            .forEach { state ->
                val drawableId = state.anchor.ids.first()

                val paint = getPaint(drawableId in pressedButtons)
                val rotationInDegrees =
                    drawableId * toDegrees(SINGLE_DRAWABLE_ANGLE.toDouble()).toFloat()

                canvas.save()
                canvas.rotate(rotationInDegrees, 0f, 0f)

                paint.paint {
                    canvas.drawPath(shapePath, it)
                }

                foregroundDrawable?.apply {
                    bounds = drawableRect
                    draw(canvas)
                }

                canvas.restore()
            }

        canvas.restore()
    }

    private fun drawBackground(canvas: Canvas, radius: Float) {
        backgroundPaint.paint {
            canvas.drawCircle(drawingBox.centerX(), drawingBox.centerY(), radius, it)
        }
    }

    private fun getMainStates() = State.values()
        .filter { it.anchor.ids.size == 1 }

    private fun drawDiagonalDirections(canvas: Canvas, radius: Float) {
        getDiagonalStates()
            .forEach {
                compositeButtonPaint.drawCompositeButton(
                    canvas,
                    drawingBox.centerX() + it.anchor.getNormalizedX() * radius * 0.75f,
                    drawingBox.centerY() + it.anchor.getNormalizedY() * radius * 0.75f,
                    it == currentState()
                )
            }
    }

    private fun getDiagonalStates() = State.values().asSequence()
        .filter { it.anchor.ids.size > 1 }

    override fun touch(fingers: List<TouchUtils.FingerPosition>, outEvents: MutableList<Event>): Boolean {
        if (isTouchDisabled()) return false

        if (fingers.isEmpty()) return reset(outEvents)

        if (trackedPointersIds.isEmpty()) {
            val finger = fingers.first()
            trackedPointersIds.add(finger.pointerId)
            return updateState(
                computeStateForPosition(
                    (finger.x - 0.5f).coerceIn(-0.5f, 0.5f),
                    (finger.y - 0.5f).coerceIn(-0.5f, 0.5f)
                ),
                simulatedState,
                outEvents
            )
        } else {
            val trackedFinger = fingers
                .firstOrNull { it.pointerId in trackedPointersIds } ?: return reset(outEvents)

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
        trackedPointersIds.clear()
        simulatedState = null

        if (emitUpdate) {
            outEvents.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_RELEASE))
        }

        return emitUpdate
    }

    private fun updateState(touchState: State?, simulatedState: State?, outEvents: MutableList<Event>): Boolean {
        val endState = simulatedState ?: touchState
        val startState = currentState()

        if (endState != startState) {
            sendStateUpdateEvent(endState, startState, outEvents)
        }

        this.touchState = touchState
        this.simulatedState = simulatedState

        return endState != startState
    }

    private fun sendStateUpdateEvent(endState: State?, startState: State?, outEvents: MutableList<Event>) {
        if (endState == null || !isActiveState(endState)) {
            outEvents.add(Event.Direction(id, 0f, 0f, HapticEngine.EFFECT_NONE))
        } else {
            val haptic = endState.anchor.ids.size >= startState?.anchor?.ids?.size ?: 0
            val hapticEffect = if (haptic) HapticEngine.EFFECT_PRESS else HapticEngine.EFFECT_RELEASE
            outEvents.add(Event.Direction(id, endState.outEvent.x, endState.outEvent.y, hapticEffect))
        }
    }

    private fun currentState(): State? = simulatedState ?: touchState

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

    private fun computeStateForPosition(x: Float, y: Float): State {
        return State.values().asSequence()
            .filter { config.useDiagonals || !it.isDiagonal()  }
            .minBy { it.anchor.getNormalizedDistance(x, y) }
            ?: State.CROSS_STATE_CENTER
    }

    private fun isInsideDeadZone(x: Float, y: Float) = abs(x) < DEAD_ZONE && abs(y) < DEAD_ZONE

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
            AccessibilityBox(upRect.roundToInt(), composeDescriptionString(config.contentDescription.up)),
            AccessibilityBox(leftRect.roundToInt(), composeDescriptionString(config.contentDescription.left)),
            AccessibilityBox(rightRect.roundToInt(), composeDescriptionString(config.contentDescription.right)),
            AccessibilityBox(downRect.roundToInt(), composeDescriptionString(config.contentDescription.down))
        )
    }

    private fun getPaint(isPressed: Boolean): FillStrokePaint {
        return when {
            isPressed -> pressedPaint
            simulatedState != null -> simulatedPaint
            else -> normalPaint
        }
    }

    private fun isActiveState(state: State?) = state != null && state != State.CROSS_STATE_CENTER

    private fun buildPath(shape: CrossConfig.Shape, rect: Rect): Path {
        return when (shape) {
            CrossConfig.Shape.STANDARD -> ArrowPathBuilder.build(rect)
            CrossConfig.Shape.CIRCLE -> CirclePathBuilder.build(rect)
        }
    }
}
