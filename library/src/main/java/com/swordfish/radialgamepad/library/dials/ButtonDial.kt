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
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.radialgamepad.library.config.ButtonConfig
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.event.Event
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable

class ButtonDial(
    context: Context,
    private val config: ButtonConfig,
    private val theme: RadialGamePadTheme
) : Dial {

    private val events = PublishRelay.create<Event>()

    private val iconDrawable = config.iconId?.let {
        context.getDrawable(it)?.apply {
            setTint(theme.textColor)
        }
    }

    private val paint = BasePaint().apply {
        color = getTheme().normalColor
    }

    private val textPainter = TextPaint()

    private var pressed = false

    private var radius = 10f
    private var drawingBox = RectF()
    private var labelDrawingBox = RectF()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = null

    override fun measure(drawingBox: RectF) {
        this.drawingBox = drawingBox
        iconDrawable?.bounds = drawingBox.scaleCentered(0.5f).roundToInt()
        radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        labelDrawingBox = drawingBox.scaleCentered(0.8f)
    }

    override fun draw(canvas: Canvas) {
        val buttonTheme = getTheme()

        paint.color = if (pressed) buttonTheme.pressedColor else buttonTheme.normalColor
        canvas.drawCircle(
            drawingBox.left + radius,
            drawingBox.top + radius,
            radius * 0.8f,
            paint
        )

        if (config.label != null) {
            textPainter.paintText(labelDrawingBox, config.label, canvas, buttonTheme)
        }

        iconDrawable?.draw(canvas)
    }

    override fun touch(fingers: List<TouchUtils.FingerPosition>): Boolean {
        val newPressed = fingers.isNotEmpty()
        if (newPressed != pressed) {
            pressed = newPressed

            val action = if (pressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP
            events.accept(Event.Button(action, config.keyCode, pressed))

            return true
        }
        return false
    }

    override fun events(): Observable<Event> = events.distinctUntilChanged()

    private fun getTheme() = (config.theme ?: theme)
}