package com.swordfish.radialgamepad.library.paint

import android.content.Context
import com.swordfish.radialgamepad.library.config.RadialGamePadTheme
import com.swordfish.radialgamepad.library.utils.PaintUtils

class PainterPalette(context: Context, theme: RadialGamePadTheme) {

    private val strokeSizePx = PaintUtils.convertDpToPixel(theme.strokeWidthDp, context)

    val normal = standard(theme.normalColor, theme.normalStrokeColor)
    val pressed = standard(theme.pressedColor, theme.normalStrokeColor)
    val simulated = standard(theme.simulatedColor, theme.normalStrokeColor)
    val background = standard(theme.backgroundColor, theme.backgroundStrokeColor)
    val light = standard(theme.lightColor, theme.lightStrokeColor)

    private fun standard(standardColor: Int, strokeColor: Int): FillStrokePaint {
        return FillStrokePaint(standardColor, strokeColor, strokeSizePx)
    }
}