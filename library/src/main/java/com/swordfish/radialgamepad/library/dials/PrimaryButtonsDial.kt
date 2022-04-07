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
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.appcompat.content.res.AppCompatResources
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.CompositeButtonPaint
import com.swordfish.radialgamepad.library.paint.FillStrokePaint
import com.swordfish.radialgamepad.library.paint.PainterPalette
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.touch.TouchAnchor
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils
import com.swordfish.radialgamepad.library.utils.neighborsPairs
import kotlin.math.sin

class PrimaryButtonsDial(
    context: Context,
    private val circleActions: List<ButtonConfig>,
    private val centerAction: ButtonConfig?,
    private val rotationRadians: Float = 0f,
    private val allowMultiplePressesSingleFinger: Boolean,
    private val theme: RadialGamePadTheme
) : Dial {

    private val painterPalette = PainterPalette(context, theme)
    private val textPaint = TextPaint()
    private val compositeButtonPaint = CompositeButtonPaint(context, theme)
    private val drawables = loadRequiredDrawables(context)

    private var pressed: Set<Int> = setOf()
    private var trackedPointerIds: MutableSet<Int> = mutableSetOf()

    private val idButtonConfigsMapping: Map<Int, ButtonConfig> = buildIdButtonsAssociations()

    private var drawingBox = RectF()
    private val actionAngle = Constants.PI2 / circleActions.size
    private var buttonRadius = 0f
    private var distanceToCenter = 0f
    private var center: PointF = PointF(0f, 0f)
    private var labelsDrawingBoxes: MutableMap<Int, RectF> = mutableMapOf()
    private val touchAnchors: List<TouchAnchor> = buildTouchAnchors()

    private fun loadRequiredDrawables(context: Context): Map<Int, Drawable?> {
        val iconDrawablePairs = (circleActions + centerAction).mapNotNull { buttonConfig ->
            buttonConfig?.iconId?.let { iconId ->
                val drawable = AppCompatResources.getDrawable(context, iconId)!!
                val buttonTheme = buttonConfig.theme ?: theme
                drawable.setTint(buttonTheme.textColor)
                iconId to drawable
            }
        }
        return iconDrawablePairs.toMap()
    }

    private fun getButtonForId(id: Int): ButtonConfig? {
        return idButtonConfigsMapping[id]
    }

    private fun buildIdButtonsAssociations(): Map<Int, ButtonConfig> {
        return (circleActions + listOf(centerAction))
            .filterNotNull().associateBy { it.id }
    }

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointersIds(): Set<Int> = trackedPointerIds

    private fun buildTouchAnchors(): List<TouchAnchor> {
        return mutableListOf<TouchAnchor>().apply {
            addAll(buildCenterButtonAnchors())
            addAll(buildCircleButtonAnchors())

            if (allowsMultiplePressed()) {
                addAll(buildCompositeButtonAnchors())
            }
        }
    }

    private fun buildCenterButtonAnchors(): List<TouchAnchor> {
        return centerAction?.let {
            listOf(TouchAnchor.fromPolar(0f, 0f, 2f, setOf(it.id)))
        } ?: listOf()
    }

    private fun buildCircleButtonAnchors(): List<TouchAnchor> {
        return circleActions
            .filter { it.visible }
            .mapIndexed { index, buttonConfig ->
                val angle = Constants.PI2 + actionAngle * index + rotationRadians
                TouchAnchor.fromPolar(angle, 0.25f, 2f, setOf(buttonConfig.id))
            }
    }

    private fun buildCompositeButtonAnchors(): List<TouchAnchor> {
        if (!allowsMultiplePressed()) {
            return listOf()
        }

        return (circleActions + listOf(circleActions[0]))
            .filter { it.visible }
            .mapIndexed { index, buttonConfig ->
                val angle = Constants.PI2 + actionAngle * index + rotationRadians
                val buttonId = buttonConfig.id
                angle to buttonId
            }
            .neighborsPairs()
            .map { (first, second) ->
                val averageAngle = arrayOf(first.first, second.first).average().toFloat()
                TouchAnchor.fromPolar(
                    averageAngle,
                    ANCHOR_COMPOSITE_DISTANCE,
                    ANCHOR_COMPOSITE_STRENGTH,
                    setOf(first.second, second.second)
                )
            }
    }

    override fun measure(drawingBox: RectF, secondarySector: Sector?) {
        this.drawingBox = drawingBox
        val dialDiameter = minOf(drawingBox.width(), drawingBox.height()) * OUTER_CIRCLE_SCALING
        buttonRadius = computeButtonRadius(dialDiameter / 2)
        distanceToCenter = dialDiameter / 2 - buttonRadius

        center = PointF(drawingBox.centerX(), drawingBox().centerY())

        buttonRadius *= BUTTON_SCALING

        compositeButtonPaint.updateDrawingBox(drawingBox)

        getSingleButtonAnchors()
            .forEach { anchor ->
                val button = getButtonForId(anchor.ids.first()) ?: return@forEach

                val subDialX = center.x + anchor.getX() * distanceToCenter * 4f
                val subDialY = center.y - anchor.getY() * distanceToCenter * 4f

                labelsDrawingBoxes[button.id] = RectF(
                    subDialX - buttonRadius,
                    subDialY - buttonRadius,
                    subDialX + buttonRadius,
                    subDialY + buttonRadius
                )

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
        drawBackground(canvas, radius)
        drawSingleActions(canvas)
        drawCompositeActions(canvas, radius)
    }

    private fun drawBackground(canvas: Canvas, radius: Float) {
        painterPalette.background.paint {
            canvas.drawCircle(center.x, center.y, radius, it)
        }
    }

    private fun drawCompositeActions(canvas: Canvas, outerRadius: Float) {
        getCompositeTouchAnchors()
            .forEach {
                compositeButtonPaint.drawCompositeButton(
                    canvas,
                    center.x + it.getNormalizedX() * outerRadius * 0.75f,
                    center.y - it.getNormalizedY() * outerRadius * 0.75f,
                    pressed.containsAll(it.ids)
                )
            }
    }

    private fun drawSingleActions(canvas: Canvas) {
        getSingleButtonAnchors()
            .forEach { anchor ->
                val button = getButtonForId(anchor.ids.first()) ?: return@forEach

                val painter = getPainterForIds(setOf(button.id))

                val subDialX = center.x + anchor.getX() * distanceToCenter * 4f
                val subDialY = center.y - anchor.getY() * distanceToCenter * 4f

                painter.paint {
                    canvas.drawCircle(subDialX, subDialY, buttonRadius, it)
                }

                if (button.label != null) {
                    textPaint.paintText(
                        labelsDrawingBoxes[button.id]!!,
                        button.label,
                        canvas,
                        button.theme ?: theme
                    )
                }

                drawables[button.iconId]?.draw(canvas)
            }
    }

    private fun getSingleButtonAnchors(): List<TouchAnchor> {
        return touchAnchors.filter { it.ids.size == 1 }
    }

    private fun getCompositeTouchAnchors(): List<TouchAnchor> {
        return touchAnchors.filter { it.ids.size > 1 }
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

    override fun touch(
        fingers: List<TouchUtils.FingerPosition>,
        outEvents: MutableList<Event>
    ): Boolean {
        trackedPointerIds.clear()
        trackedPointerIds.addAll(fingers.map { it.pointerId })

        val newPressed = fingers.asSequence()
            .flatMap { getAssociatedIds(it.x, it.y) }
            .toSet()

        if (newPressed != pressed) {
            sendNewActionDowns(newPressed, pressed, outEvents)
            sendNewActionUps(newPressed, pressed, outEvents)
            pressed = newPressed
            return true
        }

        return false
    }

    private fun getAssociatedIds(x: Float, y: Float): Sequence<Int> {
        return touchAnchors
            .minByOrNull {
                it.getNormalizedDistance(
                    (x - 0.5f).coerceIn(-0.5f, 0.5f),
                    (-y + 0.5f).coerceIn(-0.5f, 0.5f)
                )
            }
            ?.ids
            ?.asSequence() ?: sequenceOf()
    }

    private fun allowsMultiplePressed() = allowMultiplePressesSingleFinger && centerAction == null

    override fun gesture(
        relativeX: Float,
        relativeY: Float,
        gestureType: GestureType,
        outEvents: MutableList<Event>
    ): Boolean {
        getAssociatedIds(relativeX, relativeY)
            .mapNotNull { getButtonForId(it) }
            .filter { gestureType in it.supportsGestures }
            .forEach {
                outEvents.add(Event.Gesture(it.id, gestureType))
            }
        return false
    }

    private fun getPainterForIds(buttonIds: Set<Int>): FillStrokePaint {
        return when {
            pressed.containsAll(buttonIds) -> painterPalette.pressed
            buttonIds.size == 1 -> painterPalette.normal
            else -> painterPalette.background
        }
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        return circleActions
            .filter { it.visible && it.contentDescription != null }
            .mapNotNull { button ->
                labelsDrawingBoxes[button.id]?.let {
                    AccessibilityBox(it.roundToInt(), button.contentDescription ?: "")
                }
            }
    }

    private fun sendNewActionDowns(
        newPressed: Set<Int>,
        oldPressed: Set<Int>,
        outEvents: MutableList<Event>
    ) {
        newPressed.asSequence()
            .filter { it !in oldPressed && getButtonForId(it)?.supportsButtons == true }
            .forEach { outEvents.add(Event.Button(it, KeyEvent.ACTION_DOWN, HapticEngine.EFFECT_PRESS)) }
    }

    private fun sendNewActionUps(
        newPressed: Set<Int>,
        oldPressed: Set<Int>,
        outEvents: MutableList<Event>
    ) {
        oldPressed.asSequence()
            .filter { it !in newPressed && getButtonForId(it)?.supportsButtons == true }
            .forEach { outEvents.add(Event.Button(it, KeyEvent.ACTION_UP, HapticEngine.EFFECT_RELEASE)) }
    }

    companion object {
        private const val OUTER_CIRCLE_SCALING = 0.95f
        private const val BUTTON_SCALING = 0.8f

        private const val ANCHOR_COMPOSITE_DISTANCE = 0.5f
        private const val ANCHOR_COMPOSITE_STRENGTH = 1.1f
    }
}