package ui.modals

import actors.statuses.Status
import render.Screen
import ui.panels.StatusPanel
import util.wrapText

class StatusSidecar(private val parentPanel: StatusPanel) : Modal(0, 200) {

    var status: Status? = null
    private var statusDesc = ArrayList<String>()
    private var statusInfo = ArrayList<String>()

    private val padding = 18
    private val fullWidth = 200
    private val header = 35

    fun showStatus(status: Status?) {
        if (status == this.status) return
        statusDesc.clear()
        statusInfo.clear()
        this.status = status
        adjustSize()
        status?.also {
            statusDesc = wrapText(it.description(), width, padding, Screen.smallFont)
            statusInfo = wrapText(it.panelInfo(), width, padding, Screen.smallFont)
        }
        adjustSize()
    }

    fun adjustSize() {
        width = status?.let { fullWidth } ?: 0
        height = status?.let { header + padding * 2 + (statusDesc.size + statusInfo.size) * 20 + 20 } ?: 0
        y = status?.let { parentPanel.y } ?: -1000
        x = parentPanel.x - width - 20
    }

    override fun drawModalText() {
        super.drawModalText()
        status?.also { status ->
            drawString(status.name(), padding, padding, font = Screen.subTitleFont)
            drawWrappedText(statusDesc, padding, padding + 35, 20, Screen.smallFont)
            if (statusInfo.isNotEmpty()) {
                drawWrappedText(statusInfo, padding, padding + 45 + statusDesc.size * 20, 20, Screen.smallFont)
            }
        }
    }

}
