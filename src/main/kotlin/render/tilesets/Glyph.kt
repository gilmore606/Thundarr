package render.tilesets

import kotlinx.serialization.Serializable

@Serializable
enum class Glyph {
    BLANK,

    BRICK_WALL,
    CLIFF_WALL,
    TEMPERATE_FOREST_WALL,
    PINE_FOREST_WALL,
    TROPICAL_FOREST_WALL,
    CAVE_FLOOR,
    STONE_FLOOR,
    DIRT,
    ROCKS,
    CAVE_ROCKS,
    GRASS,
    UNDERGROWTH,
    HARDPAN,
    SWAMP,
    BEACH,
    SHALLOW_WATER,
    DEEP_WATER,
    LAVA,
    PORTAL_DOOR,
    PORTAL_CAVE,
    PAVEMENT,
    HIGHWAY_V,
    HIGHWAY_H,
    CHASM,
    WOOD_FLOOR,
    RUBBLE,
    WOOD_WALL,
    METAL_WALL,
    WINDOW,
    TRAIL,

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
    LAVA_SURF,
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
    INVENTORY_ALL,
    INVENTORY_GEAR,
    INVENTORY_TOOLS,
    INVENTORY_CONSUMABLES,
    INVENTORY_MISC,

    PLAYER,
    PEASANT_1,
    PEASANT_2,
    PEASANT_3,
    PEASANT_4,
    MOK,
    HORSE,
    CATTLE,
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
    SHIELD_GUARD,
    WIZARD_SCYTHE,
    THRALL_ARCHER,
    FURWORM,
    PIG,
    CYCLOX,
    LANDWHALE,
    TAILMANDER,
    DEMONDOG,
    VOLTELOPE,
    TORTLE,
    PIDGEY,
    PIDGEY_BRUTE,
    GOAT,
    MANAPE,
    GATOR,

    OAK_TREE,
    MAPLE_TREE,
    BIRCH_TREE,
    FRUIT_TREE,
    LIGHTBULB,
    AXE,
    TRAIL_SIGN,
    CHEST,
    DEAD_TREE,
    GRAVESTONE,
    BLADE,
    SUNSWORD,
    SUNSWORD_LIT,
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
    HIGHWAY_SIGN,
    GLOWING_CRYSTAL,
    BONEPILE,
    BOULDER,
    WRECKED_CAR,
    WELL,
    SHRINE,
    BOOKSHELF,
    FORGE,
    WARDROBE,
    RAILS_H,
    RAILS_V,
    LEATHER,

    BLOODSTAIN,
    SCORCHMARK,

    MAP_FOREST,
    MAP_PLAIN,
    MAP_WATER,
    MAP_GLACIER,
    MAP_SWAMP,
    MAP_DESERT,
    MAP_MOUNTAIN,
    MAP_HILL,
    MAP_FORESTHILL,
    MAP_SCRUB,
    MAP_RUINS,
    MAP_SUBURB,
    MAP_HABITAT_HOT_A,
    MAP_HABITAT_HOT_B,
    MAP_HABITAT_TEMP_A,
    MAP_HABITAT_TEMP_B,
    MAP_HABITAT_COLD_A,
    MAP_HABITAT_COLD_B,
    MAP_HABITAT_ARCTIC,
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
    MAP_PLAYER,
    MAP_CITY,
    MAP_VILLAGE,
    MAP_CAVE,
    MAP_BUILDING,
    MAP_ROAD_NSE,
    MAP_ROAD_NSW,
    MAP_ROAD_NWE,
    MAP_ROAD_NS,
    MAP_ROAD_WE,
    MAP_ROAD_WES,
    MAP_ROAD_WN,
    MAP_ROAD_NE,
    MAP_ROAD_WS,
    MAP_ROAD_SE,
    MAP_ROAD_NSEW,
    MAP_TRAIL_NSE,
    MAP_TRAIL_NSW,
    MAP_TRAIL_NWE,
    MAP_TRAIL_NS,
    MAP_TRAIL_WE,
    MAP_TRAIL_WES,
    MAP_TRAIL_WN,
    MAP_TRAIL_NE,
    MAP_TRAIL_WS,
    MAP_TRAIL_SE,
    MAP_TRAIL_NSEW,
    MAP_COLOR_0,
    MAP_COLOR_1,
    MAP_COLOR_2,
    MAP_COLOR_3,
    MAP_COLOR_4,
    MAP_COLOR_5,
    MAP_COLOR_6,
    MAP_COLOR_7,
    MAP_COLOR_8,
    MAP_COLOR_9,
    MAP_COLOR_10,
    MAP_COLOR_11,
    MAP_COLOR_12,
    MAP_COLOR_13,
    MAP_COLOR_14,
    MAP_COLOR_15,
}
