package ui.input

import com.badlogic.gdx.Input.Keys.*
import kotlinx.serialization.Serializable

@Serializable
enum class Keydef(
    val description: String,
    val defaultKey: Int?,
) {
    MOVE_N("Move north", W),
    MOVE_NE("Move northeast", E),
    MOVE_E("Move east", D),
    MOVE_SE("Move southeast", C),
    MOVE_S("Move south", X),
    MOVE_SW("Move southwest", Z),
    MOVE_W("Move west", A),
    MOVE_NW("Move northwest", Q),
    AGGRO("Toggle aggro mode", SLASH),

    INTERACT("Interact", S),
    WAIT("Wait", SPACE),
    SLEEP("Sleep", PERIOD),

    CURSOR_TOGGLE("Cursor toggle", null),
    CURSOR_NEXT("Cursor next target", PAGE_DOWN),
    CURSOR_PREV("Cursor previous target", PAGE_UP),

    ZOOM_IN("Zoom in", EQUALS),
    ZOOM_OUT("Zoom out", MINUS),

    SEEN_TOGGLE("Toggle seen areas", V),
    OPEN_INV("Open inventory", TAB),
    OPEN_GEAR("Open gear", BACKSLASH),
    OPEN_SKILLS("Open skills", BACKSPACE),
    OPEN_MAP("Open map", M),
    OPEN_JOURNAL("Open journal", O),
    TOOLBAR_SHOW("Show toolbar", GRAVE),

    CANCEL("Cancel / Menu", ESCAPE),

    SHORTCUT1("Shortcut 1", NUM_1),
    SHORTCUT2("Shortcut 2", NUM_2),
    SHORTCUT3("Shortcut 3", NUM_3),
    SHORTCUT4("Shortcut 4", NUM_4),
    SHORTCUT5("Shortcut 5", NUM_5),
    SHORTCUT6("Shortcut 6", NUM_6),
    SHORTCUT7("Shortcut 7", NUM_7),
    SHORTCUT8("Shortcut 8", NUM_8),
    SHORTCUT9("Shortcut 9", NUM_9),

    DEBUG_F5("Debug_F5", F5),
    DEBUG_F6("Debug_F6", F6),
    DEBUG_F7("Debug_F7", F7),
    DEBUG_F8("Debug_F8", F8),
}
