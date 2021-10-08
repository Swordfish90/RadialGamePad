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
import android.view.KeyEvent
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.FillStrokePaint
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.path.BeanPathBuilder
import com.swordfish.radialgamepad.library.simulation.SimulateKeyDial
import com.swordfish.radialgamepad.library.utils.TouchUtils

class ButtonDial(
    context: Context,
    private val spread: Int,
    private val config: ButtonConfig,
    private val theme: RadialGamePadTheme
) : SimulateKeyDial {

    private val iconDrawable = config.iconId?.let {
        context.getDrawable(it)?.apply {
            setTint(theme.textColor)
        }
    }

    private val paint = FillStrokePaint(context, theme).apply {
        setFillColor(getTheme().normalColor)
    }

    private val textPainter = TextPaint()

    private var pressed = false
    private var simulatedPressed: Boolean? = null

    private var radius = 10f
    private var drawingBox = RectF()
    private var labelDrawingBox = RectF()
    private var beanPath: Path = Path()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = null

    override fun measure(drawingBox: RectF, sector: Sector?) {
        this.drawingBox = drawingBox
        iconDrawable?.bounds = drawingBox.scaleCentered(0.5f).roundToInt()
        radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        labelDrawingBox = drawingBox.scaleCentered(0.8f)

        if (requiresDrawingBeanPath() && sector != null) {
            beanPath = buildBeanPath(sector)
        }
    }

    private fun buildBeanPath(sector: Sector): Path {
        return BeanPathBuilder.build(drawingBox.roundToInt(), sector)
    }

    private fun requiresDrawingBeanPath(): Boolean = spread > 1

    private fun lint(t: Float, a: Float, b: Float): Float {
        return a * (1.0f - t) + b * t
    }

    override fun draw(canvas: Canvas) {
        val buttonTheme = getTheme()

        paint.setFillColor(when {
            simulatedPressed == true || pressed -> buttonTheme.pressedColor
            simulatedPressed == false -> buttonTheme.simulatedColor
            else -> buttonTheme.normalColor
        })

        // Drawing a simple circle is faster with hw canvases so we only use it when spread is greater than one
        if (requiresDrawingBeanPath()) {
            paint.paint {
                canvas.drawPath(beanPath, it)
            }
        } else {
            paint.paint {
                canvas.drawCircle(
                    drawingBox.centerX(),
                    drawingBox.centerY(),
                    radius * (1.0f - 2 * DEFAULT_MARGIN),
                    it
                )
            }
        }

        if (config.label != null) {
            textPainter.paintText(labelDrawingBox, config.label, canvas, buttonTheme)
        }

        iconDrawable?.draw(canvas)
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>, outEvents: MutableList<Event>): Boolean {
        return updatePressed(fingers.isNotEmpty(), simulatedPressed, outEvents)
    }

    private fun updatePressed(
        newPressed: Boolean,
        newSimulatedPressed: Boolean?,
        outEvents: MutableList<Event>
    ): Boolean {
        if (pressed == newPressed && newSimulatedPressed == simulatedPressed)
            return false

        val newPressedState = newSimulatedPressed ?: newPressed
        val oldPressedState = simulatedPressed ?: pressed

        if (newPressedState != oldPressedState && config.supportsButtons) {
            val action = if (newPressedState) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
            val haptic = if (newPressedState) HapticEngine.EFFECT_PRESS else HapticEngine.EFFECT_RELEASE
            outEvents.add(Event.Button(config.id, action, haptic))
        }

        pressed = newPressed
        simulatedPressed = newSimulatedPressed

        return true
    }

    override fun simulateKeyPress(id: Int, simulatePress: Boolean, outEvents: MutableList<Event>): Boolean {
        if (id != config.id) return false
        return updatePressed(pressed, simulatePress, outEvents)
    }

    override fun clearSimulateKeyPress(id: Int, outEvents: MutableList<Event>): Boolean {
        if (id != config.id) return false
        return updatePressed(pressed, null, outEvents)
    }

    override fun gesture(
        relativeX: Float,
        relativeY: Float,
        gestureType: GestureType,
        outEvents: MutableList<Event>
    ): Boolean {
        if (gestureType in config.supportsGestures) {
            outEvents.add(Event.Gesture(config.id, gestureType))
        }
        return false
    }

    override fun accessibilityBoxes(): List<AccessibilityBox> {
        return config.contentDescription?.let {
            return listOf(AccessibilityBox(drawingBox.roundToInt(), it))
        } ?: listOf()
    }

    private fun getTheme() = (config.theme ?: theme)

    companion object {
        const val DEFAULT_MARGIN = 0.1f
    }
}