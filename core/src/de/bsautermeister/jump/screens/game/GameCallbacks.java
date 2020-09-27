package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.math.Vector2;

import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.sprites.Brick;
import de.bsautermeister.jump.sprites.PretzelBullet;
import de.bsautermeister.jump.sprites.ItemBox;
import de.bsautermeister.jump.sprites.enemies.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Player;

public interface GameCallbacks {
    void started();
    void jump(float volumeFactor);
    void landed(float landingHeight);
    void stomp(Enemy enemy);
    void attack(Enemy enemy);
    void hit(Player player);
    void use(Player player, Item item);
    void hit(Player player, Brick brick, boolean closeEnough);
    void hit(Player player, ItemBox itemBox, Vector2 position, boolean closeEnough);
    void hit(PretzelBullet pretzelBullet, Enemy enemy);
    void indirectObjectHit(InteractiveTileObject tileObject, String objectId);
    void hitWall(Enemy enemy);
    void hitWall(PretzelBullet pretzelBullet);
    void spotted(ItemBox itemBox);
    void killed(Enemy enemy);
    void kicked(Enemy enemy);
    void touchedWater(Drownable drownable);
    void collectCoin();
    void fire();
    void hurry();
    void unlockGoalBrick(Brick brick);
    void goalReached();
    void playerDied();
    void startPlayerDrowning();
    void endPlayerDrowning();
    void playerCausedDrown(Enemy enemy);
}
