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
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.MathUtils
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class PrimaryButtonsDial(
    context: Context,
    private val circleActions: List<ButtonConfig>,
    private val centerAction: ButtonConfig?,
    private val rotationRadians: Float = 0f,
    private val allowMultiplePressesSingleFinger: Boolean,
    private val theme: RadialGamePadTheme
) : Dial {

    private val actionAngle = Constants.PI2 / circleActions.size

    private val paint = BasePaint()
    private val textPaint = TextPaint()

    private var pressed: Set<Int> = setOf()

    private val idToConfigMap: Map<Int, ButtonConfig> = (circleActions + listOf(centerAction))
        .filterNotNull()
        .map { it.id to it }
        .toMap()

    private val drawables = loadRequiredDrawables(context)

    private var drawingBox = RectF()
    private var buttonRadius = 0f
    private var distanceToCenter = 0f
    private var center: PointF = PointF(0f, 0f)
    private var secondaryActivationButtonRadius = 0.5f
    private var centerLabelDrawingBox: RectF = RectF()
    private var labelsDrawingBoxes: MutableMap<Int, RectF> = mutableMapOf()
    private var normalizedActionCenters: MutableMap<Int, PointF> = mutableMapOf()
    private val actionCentersDistances: SortedMap<Float, Int> = sortedMapOf()

    private fun loadRequiredDrawables(context: Context): Map<Int, Drawable?> {
        val iconDrawablePairs = (circleActions + centerAction).mapNotNull { buttonConfig ->
            buttonConfig?.iconId?.let { iconId ->
                val drawable = context.getDrawable(iconId)!!
                val buttonTheme = buttonConfig.theme ?: theme
                drawable.setTint(buttonTheme.textColor)
                iconId to drawable
            }
        }
        return iconDrawablePairs.toMap()
    }

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = null

    override fun measure(drawingBox: RectF, secondarySector: Sector?) {
        this.drawingBox = drawingBox
        val dialDiameter = minOf(drawingBox.width(), drawingBox.height()) * OUTER_CIRCLE_SCALING
        buttonRadius = computeButtonRadius(dialDiameter / 2)
        distanceToCenter = dialDiameter / 2 - buttonRadius

        center = PointF(
            drawingBox.left + drawingBox.width() / 2,
            drawingBox.top + drawingBox.height() / 2
        )

        buttonRadius *= BUTTON_SCALING

        centerLabelDrawingBox = RectF(
            center.x - buttonRadius,
            center.y - buttonRadius,
            center.x + buttonRadius,
            center.y + buttonRadius
        )

        if (centerAction != null && centerAction.visible) {
            normalizedActionCenters[centerAction.id] = PointF(0.5f, 0.5f)
        }

        secondaryActivationButtonRadius = (buttonRadius / drawingBox.width()) * 1.5f

        circleActions
            .filter { it.visible }
            .forEachIndexed { index, button ->
                val buttonAngle = actionAngle * index + rotationRadians
                updatePainterForButton(button)

                val subDialX = center.x + cos(buttonAngle) * distanceToCenter
                val subDialY = center.y - sin(buttonAngle) * distanceToCenter

                labelsDrawingBoxes[index] = RectF(
                    subDialX - buttonRadius,
                    subDialY - buttonRadius,
                    subDialX + buttonRadius,
                    subDialY + buttonRadius
                )

                normalizedActionCenters[button.id] =
                    TouchUtils.computeRelativePosition(subDialX, subDialY, drawingBox())

                drawables[button.iconId]?.let {
                    it.bounds = RectF(
                        subDialX - buttonRadius,
                        subDialY - buttonRadius,
                        subDialX + buttonRadius,
                        subDialY + buttonRadius
                    ).scaleCentered(0.5f).roundToInt()
                }
            }
    }

    override fun draw(canvas: Canvas) {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2

        paint.color = theme.primaryDialBackground
        canvas.drawCircle(drawingBox.centerX(), drawingBox.centerY(), radius, paint)

        if (centerAction != null && centerAction.visible) {
            updatePainterForButton(centerAction)
            canvas.drawCircle(center.x, center.y, buttonRadius, paint)

            if (centerAction.label != null) {
                textPaint.paintText(
                    centerLabelDrawingBox,
                    centerAction.label,
                    canvas,
                    centerAction.theme ?: theme
                )
            }

            drawables[centerAction.iconId]?.let {
                it.bounds = RectF(
                    center.x - buttonRadius,
                    center.y - buttonRadius,
                    center.x + buttonRadius,
                    center.y + buttonRadius
                ).scaleCentered(0.5f).roundToInt()
                it.draw(canvas)
            }
        }

        circleActions
            .filter { it.visible }
            .forEachIndexed { index, button ->
                val buttonAngle = actionAngle * index + rotationRadians
                updatePainterForButton(button)

                val subDialX = center.x + cos(buttonAngle) * distanceToCenter
                val subDialY = center.y - sin(buttonAngle) * distanceToCenter

                canvas.drawCircle(subDialX, subDialY, buttonRadius, paint)

                if (button.label != null) {
                    textPaint.paintText(labelsDrawingBoxes[index]!!, button.label, canvas, button.theme ?: theme)
                }

                drawables[button.iconId]?.draw(canvas)
            }
    }

    private fun computeButtonRadius(outerRadius: Float): Float {
        val numButtons = maxOf(circleActions.size, 2)
        val sectorRadiansSin = sin(Math.PI / numButtons).toFloat()
        val radialMaxSize = outerRadius * sectorRadiansSin / (1f + sectorRadiansSin)
        val linearMaxSize = if (centerAction != null && circleActions.isNotEmpty()) {
            outerRadius / 3
        } else {
            Float.MAX_VALUE
        }
        return minOf(radialMaxSize, linearMaxSize)
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>, outEvents: MutableList<Event>): Boolean {
        val newPressed = fingers.asSequence()
            .flatMap { getAssociatedId(it.x, it.y) }
            .toSet()

        if (newPressed != pressed) {
            sendNewActionDowns(newPressed, pressed, outEvents)
            sendNewActionUps(newPressed, pressed, outEvents)
            pressed = newPressed
            return true
        }

        return false
    }

    private fun getAssociatedId(x: Float, y: Float): Sequence<Int> {
        actionCentersDistances.clear()
        normalizedActionCenters.asSequence()
            .forEach { actionCentersDistances[MathUtils.distance(x, it.value.x, y, it.value.y)] = it.key }

        val minDistance = actionCentersDistances.firstKey()
        val distanceThreshold = 0.5 * minDistance

        return actionCentersDistances.asSequence().withIndex()
            .takeWhile { (index, it) ->
                index == 0 || (allowMultiplePressesSingleFinger && abs(it.key - minDistance) < distanceThreshold)
            }
            .map { (_, it) -> it.value }
    }

    override fun gesture(
        relativeX: Float,
        relativeY: Float,
        gestureType: GestureType,
        outEvents: MutableList<Event>
    ): Boolean {
        getAssociatedId(relativeX, relativeY)
            .mapNotNull { idToConfigMap[it] }
            .filter { gestureType in it.supportsGestures }
            .forEach {
                outEvents.add(Event.Gesture(it.id, gestureType))
            }
        return false
    }

    private fun updatePainterForButton(buttonConfig: ButtonConfig) {
        val buttonTheme = buttonConfig.theme ?: theme
        if (buttonConfig.id in pressed) {
            paint.color = buttonTheme.pressedColor
        } else {
            paint.color = buttonTheme.normalColor
        }
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        val result = mutableListOf<AccessibilityBox>()

        result += circleActions
            .filter { it.visible && it.contentDescription != null }
            .mapIndexedNotNull { index, button ->
                labelsDrawingBoxes[index]?.let {
                    AccessibilityBox(it.roundToInt(), button.contentDescription ?: "")
                }
            }

        if (centerAction?.contentDescription != null) {
            result += AccessibilityBox(centerLabelDrawingBox.roundToInt(), centerAction.contentDescription)
        }

        return result
    }

    private fun sendNewActionDowns(newPressed: Set<Int>, oldPressed: Set<Int>, outEvents: MutableList<Event>) {
        newPressed.asSequence()
            .filter { it !in oldPressed && idToConfigMap[it]?.supportsButtons == true }
            .forEach { outEvents.add(Event.Button(it, KeyEvent.ACTION_DOWN, HapticEngine.EFFECT_PRESS)) }
    }

    private fun sendNewActionUps(newPressed: Set<Int>, oldPressed: Set<Int>, outEvents: MutableList<Event>) {
        oldPressed.asSequence()
            .filter { it !in newPressed && idToConfigMap[it]?.supportsButtons == true }
            .forEach { outEvents.add(Event.Button(it, KeyEvent.ACTION_UP, HapticEngine.EFFECT_RELEASE)) }
    }

    companion object {
        private const val OUTER_CIRCLE_SCALING = 0.95f
        private const val BUTTON_SCALING = 0.8f
    }
}