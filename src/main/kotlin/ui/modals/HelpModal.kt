package ui.modals

import render.Screen

class HelpModal : Modal(330, 550, "- hELp! -") {

    override fun newThingBatch() = null
    override fun newActorBatch() = null

    override fun drawModalText() {
        val left = 25
        val top = 55
        val col = 100
        drawString("num", left+10, top, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("7 8 9        Q W E", left, top+20, color = Screen.fontColorBold)
        drawString("4 _ 6        A _ D", left, top+50, color = Screen.fontColorBold)
        drawString("1 2 3        Z X C", left+3, top+80, color = Screen.fontColorBold)
        drawString("or", left+62, top+50, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("Movement", left+160, top+50, font=Screen.smallFont)

        drawString("CTRL", left, top+120, color=Screen.fontColorBold)
        drawString("+move", left+40, top+120, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("Interact to direction", left+col, top+120, font=Screen.smallFont)

        drawString("     5    S", left, top+150, color = Screen.fontColorBold)
        drawString("num    or", left, top+150, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("Interact here", left+col, top+150, font=Screen.smallFont)

        drawString("-  =", left, top+180, color=Screen.fontColorBold)
        drawString("/", left+12, top+180, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("Zoom in/out", left+col, top+180, font=Screen.smallFont)

        drawString("TAB", left, top+210, color=Screen.fontColorBold)
        drawString("Inventory", left+col, top+210, font=Screen.smallFont)

        drawString("\\", left, top+240, color=Screen.fontColorBold)
        drawString("Gear", left+col, top+240, font=Screen.smallFont)

        drawString("1  9", left, top+270, color=Screen.fontColorBold)
        drawString("-", left+12, top+270, color = Screen.fontColorDull, font = Screen.smallFont)
        drawString("Use toolbar items", left+col, top+270, font=Screen.smallFont)

        drawString("`", left, top+300, color=Screen.fontColorBold)
        drawString("Show toolbar", left+col, top+300, font=Screen.smallFont)

        drawString("BACKSPACE", left, top+330, color=Screen.fontColorBold)
        drawString("Skills", left+col, top+330, font=Screen.smallFont)

        drawString("/", left, top+360, color=Screen.fontColorBold)
        drawString("Toggle aggression", left+col, top+360, font=Screen.smallFont)

        drawString(".", left, top+390, color=Screen.fontColorBold)
        drawString("Toggle sleep", left+col, top+390, font=Screen.smallFont)

        drawString("V", left, top+420, color=Screen.fontColorBold)
        drawString("Toggle memory vision", left+col, top+420, font=Screen.smallFont)

        drawString("ESC", left, top+450, color=Screen.fontColorBold)
        drawString("Main menu", left+col, top+450, font=Screen.smallFont)
    }

}
