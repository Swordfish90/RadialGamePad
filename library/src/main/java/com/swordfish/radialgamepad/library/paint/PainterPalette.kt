package com.swordfish.radialgamepad.library.paint

import android.graphics.Paint
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme

class PainterPalette(theme: RadialGamePadTheme) {

    val normal = standard(theme.normalColor)
    val pressed = standard(theme.pressedColor)
    val simulated = standard(theme.simulatedColor)
    val background = standard(theme.primaryDialBackground)
    val light = standard(theme.lightColor)

    private fun standard(mainColor: Int): Paint {
        return BasePaint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = mainColor
            strokeCap = Paint.Cap.ROUND
        }
    }
}