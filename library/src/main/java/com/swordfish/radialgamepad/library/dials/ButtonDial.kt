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
import com.swordfish.radialgamepad.library.event.GestureType
import com.swordfish.radialgamepad.library.math.Sector
import com.swordfish.radialgamepad.library.paint.BasePaint
import com.swordfish.radialgamepad.library.utils.PaintUtils.roundToInt
import com.swordfish.radialgamepad.library.utils.PaintUtils.scaleCentered
import com.swordfish.radialgamepad.library.paint.TextPaint
import com.swordfish.radialgamepad.library.utils.TouchUtils
import io.reactivex.Observable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

class ButtonDial(
    context: Context,
    private val spread: Int,
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
    private var beanPath: Path = Path()

    override fun drawingBox(): RectF = drawingBox

    override fun trackedPointerId(): Int? = null

    override fun measure(drawingBox: RectF, sector: Sector?) {
        this.drawingBox = drawingBox
        iconDrawable?.bounds = drawingBox.scaleCentered(0.5f).roundToInt()
        radius = minOf(drawingBox.width(), drawingBox.height()) / 2
        labelDrawingBox = drawingBox.scaleCentered(0.8f)

        if (requiresDrawingBeanPath() && sector != null) {
            setupBeanPath(sector)
        }
    }

    private fun setupBeanPath(sector: Sector) {
        beanPath.reset()

        val beanRadius = radius * (1.0f - 2 * DEFAULT_MARGIN)

        val maxRadius = lint(1.0f - DEFAULT_MARGIN, sector.minRadius, sector.maxRadius)
        val middleRadius = lint(0.5f, sector.minRadius, sector.maxRadius)
        val minRadius = lint(DEFAULT_MARGIN, sector.minRadius, sector.maxRadius)

        val spreadMargin = 2f * asin(radius * DEFAULT_MARGIN / middleRadius)
        val spreadAngle = 2f * asin(beanRadius / middleRadius)

        val startAngle = sector.minAngle + spreadAngle / 2 + spreadMargin
        val middleAngle = lint(0.5f, sector.minAngle, sector.maxAngle)
        val endAngle = sector.maxAngle - spreadAngle / 2 - spreadMargin

        beanPath.addCircle(
            sector.center.x + cos(startAngle) * middleRadius,
            sector.center.y - sin(startAngle) * middleRadius,
            beanRadius,
            Path.Direction.CCW
        )
        beanPath.addCircle(
            sector.center.x + cos(endAngle) * middleRadius,
            sector.center.y - sin(endAngle) * middleRadius,
            beanRadius,
            Path.Direction.CCW
        )
        beanPath.moveTo(
            sector.center.x + cos(startAngle) * maxRadius,
            sector.center.y - sin(startAngle) * maxRadius
        )
        beanPath.quadTo(
            sector.center.x + cos(middleAngle) * maxRadius / cos((endAngle - startAngle) / 2f),
            sector.center.y - sin(middleAngle) * maxRadius / cos((endAngle - startAngle) / 2f),
            sector.center.x + cos(endAngle) * maxRadius,
            sector.center.y - sin(endAngle) * maxRadius
        )
        beanPath.lineTo(
            sector.center.x + cos(endAngle) * minRadius,
            sector.center.y - sin(endAngle) * minRadius
        )
        beanPath.quadTo(
            sector.center.x + cos(middleAngle) * minRadius / cos((endAngle - startAngle) / 2f),
            sector.center.y - sin(middleAngle) * minRadius / cos((endAngle - startAngle) / 2f),
            sector.center.x + cos(startAngle) * minRadius,
            sector.center.y - sin(startAngle) * minRadius
        )
        beanPath.close()
    }

    private fun requiresDrawingBeanPath(): Boolean = spread > 1

    private fun lint(t: Float, a: Float, b: Float): Float {
        return a * (1.0f - t) + b * t
    }

    override fun draw(canvas: Canvas) {
        val buttonTheme = getTheme()

        paint.color = if (pressed) buttonTheme.pressedColor else buttonTheme.normalColor

        // Drawing a simple circle is faster with hw canvases so we only use it when spread is greater than one
        if (requiresDrawingBeanPath()) {
            canvas.drawPath(beanPath, paint)
        } else {
            canvas.drawCircle(
                drawingBox.centerX(),
                drawingBox.centerY(),
                radius * (1.0f - 2 * DEFAULT_MARGIN),
                paint
            )
        }

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
            events.accept(Event.Button(config.id, action, pressed))

            return true
        }
        return false
    }

    override fun gesture(relativeX: Float, relativeY: Float, gestureType: GestureType): Boolean {
        events.accept(Event.Gesture(config.id, gestureType))
        return false
    }

    override fun events(): Observable<Event> = events.distinctUntilChanged()

    private fun getTheme() = (config.theme ?: theme)

    companion object {
        const val DEFAULT_MARGIN = 0.1f
    }
}