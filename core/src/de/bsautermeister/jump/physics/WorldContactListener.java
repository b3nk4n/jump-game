package de.bsautermeister.jump.physics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import de.bsautermeister.jump.sprites.CollectableItem;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.Fireball;
import de.bsautermeister.jump.sprites.Flower;
import de.bsautermeister.jump.sprites.Goomba;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Mario;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.Spiky;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        Item item;
        Mario mario;
        Enemy enemy;
        CollectableItem collectableItem;
        InteractiveTileObject tileObject;
        TaggedUserData<Enemy> taggedUserData;
        switch (collisionDef) {
            case Bits.MARIO_HEAD | Bits.BRICK:
            case Bits.MARIO_HEAD | Bits.ITEM_BOX:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO_HEAD);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.ITEM_BOX | Bits.BRICK);
                tileObject.onHeadHit(mario);
                break;
            case Bits.ENEMY_HEAD | Bits.MARIO: // TODO: check, if mario is landing very fast, he could also touch the body instead of thus the head, and die
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_HEAD);
                if (mario.getBody().getLinearVelocity().y < 0) {
                    enemy.onHeadHit(mario);
                    mario.pumpUp();
                }
                break;
            case Bits.ENEMY_SIDE | Bits.OBJECT:
            case Bits.ENEMY_SIDE | Bits.COLLIDER:
            case Bits.ENEMY_SIDE | Bits.GROUND:
            case Bits.ENEMY_SIDE | Bits.PLATFORM:
                taggedUserData = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedUserData.getUserData() instanceof Goomba) {
                    ((Goomba) taggedUserData.getUserData()).changeDirectionBySideSensorTag(taggedUserData.getTag());
                } else if (taggedUserData.getUserData() instanceof Koopa) {
                    ((Koopa) taggedUserData.getUserData()).changeDirectionBySideSensorTag(taggedUserData.getTag());
                } else if (taggedUserData.getUserData() instanceof Spiky) {
                    ((Spiky) taggedUserData.getUserData()).changeDirectionBySideSensorTag(taggedUserData.getTag());
                }
                break;
            case Bits.ENEMY: // enemy with enemy
                ((Enemy) fixtureA.getUserData()).onEnemyHit((Enemy) fixtureB.getUserData());
                ((Enemy) fixtureB.getUserData()).onEnemyHit((Enemy) fixtureA.getUserData());
                break;
            case Bits.MARIO | Bits.ENEMY:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                mario.hit(enemy);
                break;
            case Bits.ITEM | Bits.OBJECT:
                item = (Item) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                item.reverseVelocity(true, false);
                break;
            case Bits.ITEM | Bits.MARIO:
                collectableItem = (CollectableItem) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO);
                collectableItem.collectBy(mario);
                break;
            case Bits.MARIO_FEET | Bits.GROUND:
            case Bits.MARIO_FEET | Bits.PLATFORM:
            case Bits.MARIO_FEET | Bits.ITEM_BOX:
            case Bits.MARIO_FEET | Bits.BRICK:
            case Bits.MARIO_FEET | Bits.OBJECT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO_FEET);
                Object other = resolveUserData(fixtureA, fixtureB, ~Bits.MARIO_FEET);
                mario.touchGround(other);
                if (other instanceof Platform) {
                    if (Math.abs(mario.getVelocityRelativeToGround().y) < 0.1)
                    ((Platform) other).touch(); // TODO: make sure we are standing still on the platform
                }
                break;
            case Bits.BLOCK_TOP | Bits.ENEMY:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOn(enemy.getId());
                break;
            case Bits.BLOCK_TOP | Bits.ITEM:
                item = (Item) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOn(item.getId());
                break;
            case Bits.MARIO | Bits.ENEMY_SIDE:
                taggedUserData = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedUserData.getUserData() instanceof Flower) {
                    Flower flower = (Flower) taggedUserData.getUserData();
                    flower.setBlocked(true);
                }
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
        Platform platform;
        Enemy enemy;
        InteractiveTileObject tileObject;
        TaggedUserData<Enemy> taggedUserData;
        switch (collisionDef) {
            case Bits.MARIO_FEET | Bits.GROUND:
            case Bits.MARIO_FEET | Bits.PLATFORM:
            case Bits.MARIO_FEET | Bits.ITEM_BOX:
            case Bits.MARIO_FEET | Bits.BRICK:
            case Bits.MARIO_FEET | Bits.OBJECT:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO_FEET);
                Object other = resolveUserData(fixtureA, fixtureB, ~Bits.MARIO_FEET);
                mario.leftGround(other);
                break;
            case Bits.BLOCK_TOP | Bits.ENEMY:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOff(enemy.getId());
                break;
            case Bits.BLOCK_TOP | Bits.ITEM:
                item = (Item) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOff(item.getId());
                break;
            case Bits.MARIO | Bits.PLATFORM:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO);
                platform = (Platform) resolveUserData(fixtureA, fixtureB, Bits.PLATFORM);
                if (!mario.getBoundingRectangle().overlaps(platform.getBoundingRectangle())) {
                    // TODO why is this overlap check required? Because endContact should not be called when they are still in contact
                    mario.setLastJumpThroughPlatformId(null);
                }
                break;
            case Bits.MARIO | Bits.ENEMY_SIDE:
                taggedUserData = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedUserData.getUserData() instanceof Flower) {
                    Flower flower = (Flower) taggedUserData.getUserData();
                    flower.setBlocked(false);
                }
        }
    }

    private Object resolveUserData(Fixture fixtureA, Fixture fixtureB, int categoryBits) {
        return ((fixtureA.getFilterData().categoryBits & categoryBits) != 0)
                ? fixtureA.getUserData() : fixtureB.getUserData();
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        Mario mario;
        Enemy enemy;
        Fireball fireball;
        Platform platform;
        switch (collisionDef) {
            case Bits.MARIO | Bits.PLATFORM:
                mario = (Mario) resolveUserData(fixtureA, fixtureB, Bits.MARIO);
                platform = (Platform) resolveUserData(fixtureA, fixtureB, Bits.PLATFORM);

                if (mario.hasLastJumpThroughPlatformId()) {
                    if (platform.getId().equals(mario.getLastJumpThroughPlatformId())) {
                        contact.setEnabled(false);
                    }
                }
                else if (mario.getVelocityRelativeToGround().y > 0.5) {
                    mario.setLastJumpThroughPlatformId(platform.getId());
                    contact.setEnabled(false);
                }
                break;

            case Bits.ENEMY | Bits.FIREBALL:
                // done in pre-solve to don't have an impulse from the fireball to the other object
                fireball = (Fireball) resolveUserData(fixtureA, fixtureB, Bits.FIREBALL);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                fireball.resetLater();
                enemy.kill(true);
                contact.setEnabled(false);
                break;
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
