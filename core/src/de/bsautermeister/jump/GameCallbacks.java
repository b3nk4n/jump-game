package de.bsautermeister.jump;

import com.badlogic.gdx.math.Vector2;

import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Coin;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Mario;

public interface GameCallbacks {
    void jump();
    void stomp(Enemy enemy);
    void use(Mario mario, Item item);
    void hit(Mario mario, Enemy enemy);
    void hit(Mario mario, Brick brick, boolean closeEnough);
    void hit(Mario mario, Coin coin, Vector2 position, boolean closeEnough);
    void indirectEnemyHit(InteractiveTileObject tileObject, String enemyId);
    void indirectItemHit(InteractiveTileObject tileObject, String itemId);
    void hitWall(Enemy enemy);
    void killed(Enemy enemy);
    void kicked(Enemy enemy);
    void touchedWater();
    void gameOver();
}
