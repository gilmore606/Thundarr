package ui.input

import com.badlogic.gdx.Input.Keys.*
import kotlinx.serialization.Serializable

@Serializable
enum class KeydefSection(
    val title: String,
) {
    MOVE("Movement"),
    INTERACT("Interaction"),
    AWARE("Awareness"),
    UI("Interface"),
    HIDDEN(""),
}

@Serializable
enum class Keydef(
    val description: String,
    val defaultKey: Int?,
    val remappable: Boolean,
    val uiSection: KeydefSection,
) {
    MOVE_N("Move north", W, true, KeydefSection.MOVE),
    MOVE_NE("Move northeast", E, true, KeydefSection.MOVE),
    MOVE_E("Move east", D, true, KeydefSection.MOVE),
    MOVE_SE("Move southeast", C, true, KeydefSection.MOVE),
    MOVE_S("Move south", X, true, KeydefSection.MOVE),
    MOVE_SW("Move southwest", Z, true, KeydefSection.MOVE),
    MOVE_W("Move west", A, true, KeydefSection.MOVE),
    MOVE_NW("Move northwest", Q, true, KeydefSection.MOVE),

    INTERACT("Interact", S, true, KeydefSection.INTERACT),
    WAIT("Wait", SPACE, true, KeydefSection.INTERACT),
    SLEEP("Sleep", PERIOD, true, KeydefSection.INTERACT),
    AGGRO("Toggle aggro mode", SLASH, true, KeydefSection.INTERACT),

    CURSOR_TOGGLE("Cursor toggle", null, true, KeydefSection.AWARE),
    CURSOR_NEXT("Cursor next target", PAGE_DOWN, true, KeydefSection.AWARE),
    CURSOR_PREV("Cursor prev target", PAGE_UP, true, KeydefSection.AWARE),
    ZOOM_IN("Zoom in", EQUALS, true, KeydefSection.AWARE),
    ZOOM_OUT("Zoom out", MINUS, true, KeydefSection.AWARE),
    SEEN_TOGGLE("Toggle seen areas", V, true, KeydefSection.AWARE),

    OPEN_INV("Open inventory", TAB, true, KeydefSection.UI),
    OPEN_GEAR("Open gear", BACKSLASH, true, KeydefSection.UI),
    OPEN_SKILLS("Open skills", BACKSPACE, true, KeydefSection.UI),
    OPEN_MAP("Open map", M, true, KeydefSection.UI),
    OPEN_JOURNAL("Open journal", O, true, KeydefSection.UI),
    TOOLBAR_SHOW("Show toolbar", GRAVE, true, KeydefSection.UI),

    CANCEL("Cancel / Menu", ESCAPE, false, KeydefSection.HIDDEN),
    SHORTCUT1("Shortcut 1", NUM_1, false, KeydefSection.HIDDEN),
    SHORTCUT2("Shortcut 2", NUM_2, false, KeydefSection.HIDDEN),
    SHORTCUT3("Shortcut 3", NUM_3, false, KeydefSection.HIDDEN),
    SHORTCUT4("Shortcut 4", NUM_4, false, KeydefSection.HIDDEN),
    SHORTCUT5("Shortcut 5", NUM_5, false, KeydefSection.HIDDEN),
    SHORTCUT6("Shortcut 6", NUM_6, false, KeydefSection.HIDDEN),
    SHORTCUT7("Shortcut 7", NUM_7, false, KeydefSection.HIDDEN),
    SHORTCUT8("Shortcut 8", NUM_8, false, KeydefSection.HIDDEN),
    SHORTCUT9("Shortcut 9", NUM_9, false, KeydefSection.HIDDEN),
    DEBUG_F5("Debug_F5", F5, false, KeydefSection.HIDDEN),
    DEBUG_F6("Debug_F6", F6, false, KeydefSection.HIDDEN),
    DEBUG_F7("Debug_F7", F7, false, KeydefSection.HIDDEN),
    DEBUG_F8("Debug_F8", F8, false, KeydefSection.HIDDEN),
}
