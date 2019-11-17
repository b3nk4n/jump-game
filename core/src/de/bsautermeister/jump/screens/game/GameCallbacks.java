package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.math.Vector2;

import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.Fireball;
import de.bsautermeister.jump.sprites.ItemBox;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Player;

public interface GameCallbacks {
    void jump();
    void stomp(Enemy enemy);
    void use(Player player, Item item);
    void hit(Player player, Enemy enemy);
    void hit(Player player, Brick brick, boolean closeEnough);
    void hit(Player player, ItemBox itemBox, Vector2 position, boolean closeEnough);
    void hit(Fireball fireball, Enemy enemy);
    void indirectObjectHit(InteractiveTileObject tileObject, String objectId);
    void hitWall(Enemy enemy);
    void killed(Enemy enemy);
    void kicked(Enemy enemy);
    void touchedWater(Drownable drownable, boolean isBeer);
    void collectCoin();
    void fire();
    void hurry();
    void unlockGoalBrick(Brick brick);
    void goalReached();
    void gameOver();
}
