package ui.modals

import actors.actors.NPC

class TradeModal(
    private val trader: NPC,
    position: Modal.Position = Modal.Position.LEFT,
) : Modal(
    650, 400, trader.iname(),
    position = position,
) {



}
