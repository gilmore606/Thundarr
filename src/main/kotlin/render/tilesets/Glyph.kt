package render.tilesets

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    BLANK,

    BRICK_WALL,
    CLIFF_WALL,
    FOREST_WALL,
    CAVE_FLOOR,
    STONE_FLOOR,
    DIRT,
    GRASS,
    SWAMP,
    BEACH,
    SHALLOW_WATER,
    DEEP_WATER,
    PORTAL_DOOR,
    WALL_DAMAGE,

    CURSOR,
    BOX_BG,
    BOX_BORDER,
    BOX_SHADOW,
    LOGO_MOON,
    LOGO_OOKLA,
    OCCLUSION_SHADOWS_V,
    OCCLUSION_SHADOWS_H,
    SURF,
    PANEL_SHADOW,
    BUTTON_INVENTORY,
    BUTTON_MAP,
    BUTTON_SYSTEM,
    BUTTON_JOURNAL,
    BUTTON_GEAR,
    BUTTON_SKILLS,
    BUTTON_PAUSE,
    BUTTON_PLAY,
    BUTTON_FFWD,
    BUTTON_BLANK,
    COLOR_BARS,
    WINDOW_SHADE,
    HEALTH_ICON,
    SLEEP_ICON,
    ANGRY_THUNDARR,
    SPEECH_BUBBLE,
    POW_ICON,
    HOSTILE_ICON,
    QUESTION_ICON,

    PLAYER,
    MOK,
    HORSE,
    CATTLE,
    PEASANT,
    CITIZEN,
    TRIBAL,
    HERDER,
    WOLFMAN,
    MOB_SHADOW,
    SNAKEMAN,
    KARROK,
    THRALL,
    RATMAN,
    RATTHING,
    RATLORD,
    FLOATING_EYE,
    WIZARD_SHIELD,
    WIZARD_SCYTHE,
    THRALL_ARCHER,
    FURWORM,

    TREE,
    LIGHTBULB,
    AXE,
    SIGN,
    CHEST,
    DEAD_TREE,
    TOMBSTONE,
    BLADE,
    HILT,
    HILT_LIT,
    PINE_TREE,
    PALM_TREE,
    BOTTLE,
    FRUIT,
    TORCH,
    TORCH_LIT,
    SMOKE_PUFF,
    DUST_PUFF,
    CORPSE,
    MEAT,
    FILING_CABINET,
    HARD_HAT,
    HORNED_HAT,
    HELMET,
    LOG,
    BRICK,
    RAINDROP,
    LIGHTER,
    CAMPFIRE,
    DOOR_CLOSED,
    DOOR_OPEN,
    WOOD_DOOR_CLOSED,
    WOOD_DOOR_OPEN,
    TABLE,
    BOARD,
    BROWN_BOOTS,
    BROWN_BOOTS_WORN,
    GRAY_BOOTS,
    GRAY_BOOTS_WORN,
    RED_TUNIC,
    RED_TUNIC_WORN,
    RED_JACKET,
    RED_JACKET_WORN,
    NECK_CHARM,
    NECK_CHARM_WORN,
    CEILING_LIGHT,
    CLOTH_ROLL,
    MEDPACK,
    PILL_BOTTLE,
    PACKAGE_BAG,
    BATTERY,
    FLASHLIGHT,
    COMPUTER_OFF,
    COMPUTER_ON,
    STRONGBOX,
    PORTABLE_DEVICE_1,
    PORTABLE_DEVICE_2,
    FOOD_CAN,
    JEWELED_RING,
    CHICKEN_LEG,
    CHEESE,
    JAR,
    BOOK_GRAY,
    BOOK_PURPLE,
    KEY,
    STEW,
    BEDROLL,
    FRIDGE,
    GRAY_TUNIC,
    GRAY_TUNIC_WORN,
    STOVE,
    SLINGSHOT,
    ROCK,
    TRIDENT,
    SPEAR,
    BUSH_1,
    BUSH_1_FRUIT,
    BUSH_2,
    BUSH_2_FRUIT,
    HANGFLOWER,
    SUNFLOWER,
    SUCCULENT,
    FLOWERS,
    TOADSTOOLS,
    MUSHROOM,
    CACTUS_SMALL,
    CACTUS_BIG,
    HERB_PLANT_1,

    BLOODSTAIN,
    SCORCHMARK,

    MAP_FOREST,
    MAP_PLAIN,
    MAP_WATER,
    MAP_GLACIER,
    MAP_SWAMP,
    MAP_DESERT,
    MAP_MOUNTAIN,
    MAP_RIVER_NSE,
    MAP_RIVER_NSW,
    MAP_RIVER_NWE,
    MAP_RIVER_NS,
    MAP_RIVER_WE,
    MAP_RIVER_WES,
    MAP_RIVER_WN,
    MAP_RIVER_NE,
    MAP_RIVER_WS,
    MAP_RIVER_SE,
    MAP_PLAYER
}
