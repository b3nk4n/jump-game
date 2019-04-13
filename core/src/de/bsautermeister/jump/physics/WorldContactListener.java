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

        switch (collisionDef) {
            case JumpGame.MARIO_HEAD_BIT | JumpGame.BRICK_BIT:
            case JumpGame.MARIO_HEAD_BIT | JumpGame.COIN_BIT:
                if (fixtureA.getFilterData().categoryBits == JumpGame.MARIO_HEAD_BIT) {
                    ((InteractiveTileObject) fixtureB.getUserData()).onHeadHit((Mario) fixtureA.getUserData());
                } else {
                    ((InteractiveTileObject) fixtureA.getUserData()).onHeadHit((Mario) fixtureB.getUserData());
                }
                break;
            case JumpGame.ENEMY_HEAD_BIT | JumpGame.MARIO_BIT: // TODO: check, if mario is landing very fast, he could also touch the body instead of thus the head, and die
                if (fixtureA.getFilterData().categoryBits == JumpGame.ENEMY_HEAD_BIT) {
                    ((Enemy) fixtureA.getUserData()).onHeadHit((Mario) fixtureB.getUserData());
                } else {
                    ((Enemy) fixtureB.getUserData()).onHeadHit((Mario) fixtureA.getUserData());
                }
                break;
            case JumpGame.ENEMY_BIT | JumpGame.OBJECT_BIT: // TODO: enemy vs. ground collusion? Using side-sensors? or a bottom shape below the circle?
                if (fixtureA.getFilterData().categoryBits == JumpGame.ENEMY_BIT) {
                    ((Enemy) fixtureA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Enemy) fixtureB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case JumpGame.ENEMY_BIT: // enemy with enemy
                ((Enemy) fixtureA.getUserData()).onEnemyHit((Enemy) fixtureB.getUserData());
                ((Enemy) fixtureB.getUserData()).onEnemyHit((Enemy) fixtureA.getUserData());
                break;
            case JumpGame.MARIO_BIT | JumpGame.ENEMY_BIT:
                if (fixtureA.getFilterData().categoryBits == JumpGame.MARIO_BIT) {
                    ((Mario) fixtureA.getUserData()).hit((Enemy) fixtureB.getUserData());
                } else {
                    ((Mario) fixtureB.getUserData()).hit((Enemy) fixtureA.getUserData());
                }
                break;
            case JumpGame.ITEM_BIT | JumpGame.OBJECT_BIT:
                if (fixtureA.getFilterData().categoryBits == JumpGame.ITEM_BIT) {
                    ((Item) fixtureA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Item) fixtureB.getUserData()).reverseVelocity(true, false); // TODO de.bsautermeister.jump.sprites.Goomba cannot be cast to de.bsautermeister.jump.sprites.Item (when enemy jumped on head)
                }
                break;
            case JumpGame.ITEM_BIT | JumpGame.MARIO_BIT:
                if (fixtureA.getFilterData().categoryBits == JumpGame.ITEM_BIT) {
                    ((Item) fixtureA.getUserData()).use((Mario) fixtureB.getUserData());
                } else {
                    ((Item) fixtureB.getUserData()).use((Mario) fixtureA.getUserData()); // TODO here was a crash: ClassCastException: java.lang.String cannot be cast to de.bsautermeister.jump.sprites.Mario (on head as well?)
                }
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
