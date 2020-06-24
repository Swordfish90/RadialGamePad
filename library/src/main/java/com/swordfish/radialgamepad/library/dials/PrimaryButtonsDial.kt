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
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.utils.Constants
import com.swordfish.radialgamepad.library.math.MathUtils
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class PrimaryButtonsDial(
    context: Context,
    private val circleActions: List<ButtonConfig>,
    private val centerAction: ButtonConfig?,
    private val rotationRadians: Float = 0f,
    private val theme: RadialGamePadTheme
) : Dial {

    private val actionAngle = Constants.PI2 / circleActions.size

    private val eventsRelay = PublishRelay.create<Event>()

    private val paint = BasePaint()
    private val textPaint = TextPaint()

    private var pressed: Set<Int> = setOf()

    private val drawables = loadRequiredDrawables(context)

    private var drawingBox = RectF()
    private var buttonRadius = 0f
    private var distanceToCenter = 0f
    private var center: PointF = PointF(0f, 0f)
    private var normalizedCenter: PointF = PointF(0.5f, 0.5f)
    private var normalizedButtonRadius = 0.5f
    private var centerLabelDrawingBox: RectF = RectF()
    private var labelsDrawingBoxes: MutableMap<Int, RectF> = mutableMapOf()

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
        val dialDiameter = minOf(drawingBox.width(), drawingBox.height())
        distanceToCenter = dialDiameter / 4f
        buttonRadius = computeButtonRadius(dialDiameter)

        center = PointF(
            drawingBox.left + drawingBox.width() / 2,
            drawingBox.top + drawingBox.height() / 2
        )

        if (centerAction != null && circleActions.isNotEmpty()) {
            distanceToCenter += buttonRadius * 0.5f
        } else {
            buttonRadius *= BUTTON_SCALING
        }

        centerLabelDrawingBox = RectF(
            center.x - buttonRadius,
            center.y - buttonRadius,
            center.x + buttonRadius,
            center.y + buttonRadius
        )

        normalizedButtonRadius = buttonRadius / drawingBox.width()

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

    private fun computeButtonRadius(dialDiameter: Float): Float {
        val numButtons = maxOf(circleActions.size, 2)
        val radialMaxSize = dialDiameter * sin(Math.PI / numButtons).toFloat() / 4
        val linearMaxSize = BUTTON_SCALING * if (centerAction != null && circleActions.isNotEmpty()) dialDiameter / 6 else Float.MAX_VALUE
        return minOf(radialMaxSize, linearMaxSize)
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        val newPressed = fingers
            .mapNotNull { getAssociatedId(it.x, it.y) }
            .toSet()

        if (newPressed != pressed) {
            sendNewActionDowns(newPressed, pressed)
            sendNewActionUps(newPressed, pressed)
            pressed = newPressed
            return true
        }

        return false
    }

    private fun getAssociatedId(x: Float, y: Float): Int? {
        if (centerAction != null && MathUtils.distance(normalizedCenter.x, x, normalizedCenter.y, y) < normalizedButtonRadius) {
            return centerAction.id
        }

        if (circleActions.isNotEmpty()) {
            val index = (floor(computeTouchAngle(x, y) / actionAngle).toInt())
            return circleActions[index].id
        }

        return null
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType): Boolean {
        getAssociatedId(relativeX, relativeY)?.let {
            eventsRelay.accept(Event.Gesture(it, gestureType))
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

    private fun sendNewActionDowns(newPressed: Set<Int>, oldPressed: Set<Int>) {
        newPressed.asSequence()
            .filter { it !in oldPressed }
            .forEach { eventsRelay.accept(Event.Button(it, KeyEvent.ACTION_DOWN, true)) }
    }

    private fun sendNewActionUps(newPressed: Set<Int>, oldPressed: Set<Int>) {
        oldPressed.asSequence()
            .filter { it !in newPressed }
            .forEach { eventsRelay.accept(Event.Button(it, KeyEvent.ACTION_UP, false)) }
    }

    private fun computeTouchAngle(x: Float, y: Float): Float {
        return (MathUtils.angle(0.5f, x, 0.5f, y) + actionAngle / 2 - rotationRadians) % Constants.PI2
    }

    override fun events(): Observable<Event> = eventsRelay.distinctUntilChanged()

    companion object {
        private const val BUTTON_SCALING = 0.8f
    }
}