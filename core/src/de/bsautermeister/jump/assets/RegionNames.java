package de.bsautermeister.jump.assets;

public class RegionNames {
    public static String SMALL_PLAYER_WALK_TPL = "small_player_*_walk";
    public static String SMALL_PLAYER_STAND_TPL = "small_player_*_stand";
    public static String SMALL_PLAYER_JUMP_TPL = "small_player_*_jump";
    public static String SMALL_PLAYER_CROUCH_TPL = "small_player_*_crouch";
    public static String SMALL_PLAYER_DEAD_TPL = "small_player_*_dead";
    public static String SMALL_PLAYER_TURN_TPL = "small_player_*_turn";
    public static String SMALL_PLAYER_VICTORY = "small_player_3_victory";
    public static String SMALL_PLAYER_BEER_VICTORY = "small_player_3_beer_victory";
    public static String SMALL_PLAYER_DROWN_TPL = "small_player_*_drown";

    public static String BIG_PLAYER_WALK_TPL = "big_player_*_walk";
    public static String BIG_PLAYER_STAND_TPL = "big_player_*_stand";
    public static String BIG_PLAYER_JUMP_TPL = "big_player_*_jump";
    public static String BIG_PLAYER_CROUCH_TPL = "big_player_*_crouch";
    public static String BIG_PLAYER_TURN_TPL = "big_player_*_turn";
    public static String BIG_PLAYER_DROWN_TPL = "big_player_*_drown";
    public static String BIG_PLAYER_DEAD_TPL = "big_player_*_dead";
    public static String BIG_PLAYER_VICTORY = "big_player_3_victory";
    public static String BIG_PLAYER_BEER_VICTORY = "big_player_3_beer_victory";

    public static String BIG_PLAYER_PREZELIZED_WALK = "big_player_prezelized_walk";
    public static String BIG_PLAYER_PREZELIZED_STAND = "big_player_prezelized_stand";
    public static String BIG_PLAYER_PREZELIZED_JUMP = "big_player_prezelized_jump";
    public static String BIG_PLAYER_PREZELIZED_CROUCH = "big_player_prezelized_crouch";
    public static String BIG_PLAYER_PREZELIZED_TURN = "big_player_prezelized_turn";

    public static String FOX_WALK = "fox_walk";
    public static String FOX_ANGRY_WALK = "fox_angry_walk";
    public static String FOX_STANDING = "fox_standing";
    public static String FOX_ANGRY_STANDING = "fox_angry_standing";
    public static String FOX_STOMP = "fox_stomp";
    public static String HEDGEHOG_WALK = "hedgehog_walk";
    public static String HEDGEHOG_ROLL = "hedgehog_roll";
    public static String DRUNKEN_GUY = "drunken_guy";
    public static String FISH = "fish";
    public static String COIN = "coin";
    public static String BRICK_FRAGMENT_TPL = "brick_fragment*";
    public static String BOX_FRAGMENT_TPL = "box_fragment*";
    public static String WATER = "water";
    public static String PLATFORM2 = "platform2";
    public static String PLATFORM3 = "platform3";
    public static String PLATFORM4 = "platform4";
    public static String BREAK_PLATFORM2 = "break_platform2";
    public static String BREAK_PLATFORM3 = "break_platform3";
    public static String BREAK_PLATFORM4 = "break_platform4";
    public static String BEER = "beer";
    public static String PRETZEL = "pretzel";
    public static String PRETZEL_BULLET = "pretzel_bullet";
    public static String GRILLED_CHICKEN = "grilled_chicken";
    public static String TENT_OPEN = "tent_open";
    public static String TENT_CLOSED = "tent_closed";
    public static String POLE = "pole";
    public static String SNORER = "snorer";
    public static String TABLE_ROW = "table_row";
    public static String BACKGROUND_OVERLAY = "bg-overlay";

    public static String PERSON_1 = "person1_beer_victory";
    public static String PERSON_2 = "person2_beer_victory";
    public static String PERSON_3 = "person3_beer_victory";
    public static String PERSON_4 = "person4_beer_victory";
    public static String PERSON_5 = "person5_beer_victory";
    public static String PERSON_6 = "person6_beer_victory";
    public static String PERSON_7 = "person7_beer_victory";
    public static String PERSON_8 = "person8_beer_victory";
    public static String PERSON_9 = "person9_beer_victory";
    public static String PERSON_10 = "person10_beer_victory";
    public static String GIRL_1 = "girl1_beer_victory";
    public static String GIRL_2 = "girl2_beer_victory";
    public static String GIRL_3 = "girl3_beer_victory";
    public static String GIRL_4 = "girl4_beer_victory";
    public static String GIRL_5 = "girl5_beer_victory";
    public static String GIRL_6 = "girl6_beer_victory";
    public static String GIRLFRIEND = "girlfriend_beer_victory";

    public static String TENT_INSIDE_BACKGROUND = "tent_inside";
    public static String TENT_DECORATION_BACKGROUND = "tent_inside_decoration";

    public static String LOADING_TEXT = "loading_text";
    public static String LOADING_ANIMATION = "loading";
    public static String LOADING_FRAME = "frame";
    public static String LOADING_BAR_HIDDEN = "loading-hidden";
    public static String LOADING_FRAME_BACKGROUND = "frame-bg";

    public static String BEEROMETER_TPL = "beerometer*";

    private RegionNames() {}

    public static String fromTemplate(String template, int value) {
        return template.replace("*", String.valueOf(value));
    }
}
