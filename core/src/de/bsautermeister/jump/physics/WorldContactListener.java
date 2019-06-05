package de.bsautermeister.jump.physics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Mario;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        Item item;
        Mario mario;
        Enemy enemy;
        InteractiveTileObject tileObject;
        switch (collisionDef) {
            case JumpGame.MARIO_HEAD_BIT | JumpGame.BRICK_BIT:
            case JumpGame.MARIO_HEAD_BIT | JumpGame.COIN_BIT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_HEAD_BIT);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, JumpGame.COIN_BIT | JumpGame.BRICK_BIT);
                tileObject.onHeadHit(mario);
                break;
            case JumpGame.ENEMY_HEAD_BIT | JumpGame.MARIO_BIT: // TODO: check, if mario is landing very fast, he could also touch the body instead of thus the head, and die
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_BIT);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_HEAD_BIT);
                enemy.onHeadHit(mario);
                break;
            case JumpGame.ENEMY_BIT | JumpGame.OBJECT_BIT:
            case JumpGame.ENEMY_BIT | JumpGame.COLLIDER_BIT:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_BIT);
                enemy.reverseVelocity(true, false);
                break;
            case JumpGame.ENEMY_SIDE_BIT | JumpGame.GROUND_BIT:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_SIDE_BIT);
                enemy.reverseVelocity(true, false);
                break;
            case JumpGame.ENEMY_BIT: // enemy with enemy
                ((Enemy) fixtureA.getUserData()).onEnemyHit((Enemy) fixtureB.getUserData());
                ((Enemy) fixtureB.getUserData()).onEnemyHit((Enemy) fixtureA.getUserData());
                break;
            case JumpGame.MARIO_BIT | JumpGame.ENEMY_BIT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_BIT);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_BIT);
                mario.hit(enemy);
                break;
            case JumpGame.ITEM_BIT | JumpGame.OBJECT_BIT:
                item = (Item) resolveUserData(fixtureA, fixtureB, JumpGame.ITEM_BIT);
                item.reverseVelocity(true, false);
                break;
            case JumpGame.ITEM_BIT | JumpGame.MARIO_BIT:
                item = (Item) resolveUserData(fixtureA, fixtureB, JumpGame.ITEM_BIT);
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_BIT);
                item.usedBy(mario);
                break;
            case JumpGame.MARIO_FEET_BIT | JumpGame.GROUND_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.COIN_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.BRICK_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.OBJECT_BIT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_FEET_BIT);
                mario.touchGround();
                break;
            case JumpGame.BLOCK_TOP_BIT | JumpGame.ENEMY_BIT:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_BIT);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, JumpGame.BLOCK_TOP_BIT);
                tileObject.enemySteppedOn(enemy);
                break;
            case JumpGame.BLOCK_TOP_BIT | JumpGame.ITEM_BIT:
                item = (Item) resolveUserData(fixtureA, fixtureB, JumpGame.ITEM_BIT);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, JumpGame.BLOCK_TOP_BIT);
                tileObject.itemSteppedOn(item);
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        Item item;
        Mario mario;
        Enemy enemy;
        InteractiveTileObject tileObject;
        switch (collisionDef) {
            case JumpGame.MARIO_FEET_BIT | JumpGame.GROUND_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.COIN_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.BRICK_BIT:
            case JumpGame.MARIO_FEET_BIT | JumpGame.OBJECT_BIT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, JumpGame.MARIO_FEET_BIT);
                mario.leftGround();
                break;
            case JumpGame.BLOCK_TOP_BIT | JumpGame.ENEMY_BIT:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, JumpGame.ENEMY_BIT);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, JumpGame.BLOCK_TOP_BIT);
                tileObject.enemySteppedOff(enemy);
                break;
            case JumpGame.BLOCK_TOP_BIT | JumpGame.ITEM_BIT:
                item = (Item) resolveUserData(fixtureA, fixtureB, JumpGame.ITEM_BIT);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, JumpGame.BLOCK_TOP_BIT);
                tileObject.itemSteppedOff(item);
                break;
        }
    }

    private Object resolveUserData(Fixture fixtureA, Fixture fixtureB, int categoryBits) {
        return ((fixtureA.getFilterData().categoryBits & categoryBits) != 0)
                ? fixtureA.getUserData() : fixtureB.getUserData();
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
