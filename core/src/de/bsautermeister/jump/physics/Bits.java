package de.bsautermeister.jump.physics;

public interface Bits {
    short NOTHING = 0;
    short GROUND = 1;
    short PLAYER = 1 << 1;
    short BRICK = 1 << 2;
    short ITEM_BOX = 1 << 3;
    short BULLET = 1 << 4;
    short ENEMY = 1 << 5;
    short ENEMY_HEAD = 1 << 6;
    short ITEM = 1 << 7;
    short PLAYER_HEAD = 1 << 8;
    short PLAYER_GROUND = 1 << 9;
    short PLAYER_FEET = 1 << 10;
    short ENEMY_SIDE = 1 << 11;
    short BLOCK_TOP = 1 << 12;
    short COLLIDER = 1 << 13;
    short PLATFORM = 1 << 14;

    short ENVIRONMENT_ONLY = GROUND | BRICK | ITEM_BOX | PLATFORM;
}
