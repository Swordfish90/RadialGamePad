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
import android.view.KeyEvent
import com.swordfish.radialgamepad.library.accessibility.AccessibilityBox
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.haptics.HapticEngine
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.PainterPalette
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.simulation.SimulateKeyDial
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.utils.TouchUtils

class ButtonDial(
    context: Context,
    private val config: ButtonConfig,
    private val theme: RadialGamePadTheme
) : SimulateKeyDial {

    private val iconDrawable = config.iconId?.let {
        context.getDrawable(it)?.apply {
            setTint(theme.textColor)
        }
    }

    private val paintPalette = PainterPalette(getTheme())

    private val textPainter = TextPaint()

    private var pressed = false
    private var simulatedPressed: Boolean? = null

    private var radius = 10f
    private var drawingBox = RectF()
    private var labelDrawingBox = RectF()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointersIds(): Set<Int> = emptySet()

    override fun measure(drawingBox: RectF, sector: Sector?) {
        this.drawingBox = drawingBox
        iconDrawable?.bounds = drawingBox.scaleCentered(0.5f).roundToInt()
        radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        labelDrawingBox = drawingBox.scaleCentered(0.6f)
        paintPalette.background.strokeWidth = 0.15f * drawingBox.width()
    }

    override fun draw(canvas: Canvas) {
        val buttonTheme = getTheme()

        drawBackground(canvas)
        drawForeground(canvas, buttonTheme)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawCircle(
            drawingBox.centerX(),
            drawingBox.centerY(),
            radius * (1.0f - 2 * DEFAULT_MARGIN),
            paintPalette.background
        )
    }

    private fun drawForeground(canvas: Canvas, buttonTheme: RadialGamePadTheme) {
        val paint = when {
            simulatedPressed == true || pressed -> paintPalette.pressed
            simulatedPressed == false -> paintPalette.simulated
            else -> paintPalette.normal
        }

        canvas.drawCircle(
            drawingBox.centerX(),
            drawingBox.centerY(),
            radius * (1.0f - 2 * DEFAULT_MARGIN),
            paint
        )

        config.label?.let {
            textPainter.paintText(labelDrawingBox, it, canvas, buttonTheme)
        }

        iconDrawable?.draw(canvas)
    }

    override fun touch(
        fingers: List<TouchUtils.FingerPosition>,
        outEvents: MutableList<Event>
    ): Boolean {
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

    override fun simulateKeyPress(
        id: Int,
        simulatePress: Boolean,
        outEvents: MutableList<Event>
    ): Boolean {
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
        const val DEFAULT_MARGIN = 0.15f
    }
}