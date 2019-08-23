package de.bsautermeister.jump.physics;

public interface Bits {
    short NOTHING = 0;
    short GROUND = 1;
    short MARIO = 2;
    short BRICK = 4;
    short ITEM_BOX = 8;
    short DESTROYED = 16;
    short ENEMY = 32;
    short OBJECT = 64;
    short ENEMY_HEAD = 128;
    short ITEM = 256;
    short MARIO_HEAD = 512;
    short MARIO_FEET = 1024;
    short ENEMY_SIDE = 2048;
    short BLOCK_TOP = 4096;
    short COLLIDER = 8192;
    short PLATFORM = 16384;
}
