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
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.*

class CrossDial(
    context: Context,
    private val motionId: Int,
    normalDrawable: Int,
    pressedDrawable: Int,
    theme: RadialGamePadTheme
) : Dial {

    companion object {
        private const val DRAWABLE_SIZE_SCALING = 0.75
        private const val BUTTON_COUNT = 8
        private const val ROTATE_BUTTONS = 22.5f
        private const val SINGLE_BUTTON_ANGLE = 360f / BUTTON_COUNT

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

    private var normalDrawable: Drawable = context.getDrawable(normalDrawable)!!.apply {
        setTint(theme.normalColor)
    }

    private var pressedDrawable: Drawable = context.getDrawable(pressedDrawable)!!.apply {
        setTint(theme.pressedColor)
    }

    private var trackedPointerId: Int? = null

    private var currentIndex: Int? = null

    private var drawingBox = RectF()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = trackedPointerId

    override fun measure(drawingBox: RectF) {
        this.drawingBox = drawingBox
    }


    override fun draw(canvas: Canvas) {
        val radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        val drawableSize = (radius * DRAWABLE_SIZE_SCALING).roundToInt()

        val pressedButtons = convertDiagonals(currentIndex)

        for (i in 0..BUTTON_COUNT) {
            val cAngle = SINGLE_BUTTON_ANGLE * i

            val isPressed = i in pressedButtons

            getStateDrawable(i, isPressed)?.let {
                val height = drawableSize
                val width = drawableSize
                val angle =
                    Math.toRadians((cAngle - ROTATE_BUTTONS + SINGLE_BUTTON_ANGLE / 2f).toDouble())
                val left = drawingBox.left + (radius * buttonCenterDistance * cos(angle) + radius).toInt() - width / 2
                val top = drawingBox.top + (radius * buttonCenterDistance * sin(angle) + radius).toInt() - height / 2
                val xPivot = left + width / 2f
                val yPivot = top + height / 2f

                canvas.rotate(i * SINGLE_BUTTON_ANGLE, xPivot, yPivot)

                it.setBounds(left.roundToInt(), top.roundToInt(), (left + width).roundToInt(), (top + height).roundToInt())
                it.draw(canvas)

                canvas.rotate(-i * SINGLE_BUTTON_ANGLE, xPivot, yPivot)
            }
        }
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        if (fingers.isEmpty() && currentIndex == null) {
            return false
        } else if (fingers.isEmpty()) {
            currentIndex = null
            trackedPointerId = null
            eventsRelay.accept(Event.Direction(motionId, 0f, 0f, false))
            return true
        }


        if (trackedPointerId == null) {
            val finger = fingers.first()
            trackedPointerId = finger.pointerId
            return handleTouchEvent(finger.x - 0.5f, finger.y - 0.5f)
        } else {
            val finger = fingers
                .filter { it.pointerId == trackedPointerId }
                .firstOrNull()

            if (finger == null) {
                trackedPointerId = null
                return true
            }

            return handleTouchEvent(finger.x - 0.5f, finger.y - 0.5f)
        }
    }

    private fun handleTouchEvent(x: Float, y: Float): Boolean {

        val angle = (atan2(y, x) * 180 / Math.PI + 360f) % 360f
        val index = (angle / SINGLE_BUTTON_ANGLE).roundToInt() % BUTTON_COUNT

        if (index != currentIndex) {
            val haptic = currentIndex?.let { prevIndex -> (prevIndex % 2) == 0 } ?: true

            currentIndex = index
            // TODO FILIPPO... We might avoid this radians conversion
            eventsRelay.accept(Event.Direction(
                motionId,
                cos(index * Math.toRadians(SINGLE_BUTTON_ANGLE.toDouble())).toFloat(),
                sin(index * Math.toRadians(SINGLE_BUTTON_ANGLE.toDouble())).toFloat(),
                haptic
            ))
            return true
        }

        return false
    }

    override fun events(): Observable<Event> = eventsRelay.distinctUntilChanged()

    private fun getStateDrawable(index: Int, isPressed: Boolean): Drawable? {
        return if (index in DRAWABLE_BUTTONS) {
            if (isPressed) { pressedDrawable } else { normalDrawable }
        } else {
            null
        }
    }

    private fun convertDiagonals(currentIndex: Int?): Set<Int> {
        return when (currentIndex) {
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
            else -> setOf(currentIndex)
        }
    }
}