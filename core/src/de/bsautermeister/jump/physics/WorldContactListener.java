package de.bsautermeister.jump.physics;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import de.bsautermeister.jump.sprites.CollectableItem;
import de.bsautermeister.jump.sprites.Enemy;
import de.bsautermeister.jump.sprites.PretzelBullet;
import de.bsautermeister.jump.sprites.Flower;
import de.bsautermeister.jump.sprites.Goomba;
import de.bsautermeister.jump.sprites.InteractiveTileObject;
import de.bsautermeister.jump.sprites.Item;
import de.bsautermeister.jump.sprites.Koopa;
import de.bsautermeister.jump.sprites.Platform;
import de.bsautermeister.jump.sprites.Player;
import de.bsautermeister.jump.sprites.Spiky;

public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionDef = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        Player player;
        Enemy enemy;
        InteractiveTileObject tileObject;
        TaggedUserData<Item> taggedItem;
        TaggedUserData<CollectableItem> taggedCollectableItem;
        TaggedUserData<Enemy> taggedEnemy;
        switch (collisionDef) {
            case Bits.PLAYER_HEAD | Bits.BRICK:
            case Bits.PLAYER_HEAD | Bits.ITEM_BOX:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER_HEAD);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.ITEM_BOX | Bits.BRICK);
                tileObject.onHeadHit(player);
                break;
            case Bits.ENEMY_HEAD | Bits.PLAYER:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_HEAD);
                if (player.getBody().getLinearVelocity().y < 0) {
                    enemy.onHeadHit(player);
                    player.pumpUp();
                }
                break;
            case Bits.ENEMY_SIDE | Bits.COLLIDER:
            case Bits.ENEMY_SIDE | Bits.GROUND:
            case Bits.ENEMY_SIDE | Bits.PLATFORM:
            case Bits.ENEMY_SIDE | Bits.BRICK:
            case Bits.ENEMY_SIDE | Bits.ITEM_BOX:
                taggedEnemy = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedEnemy.getUserData() instanceof Goomba) {
                    ((Goomba) taggedEnemy.getUserData()).changeDirectionBySideSensorTag(taggedEnemy.getTag());
                } else if (taggedEnemy.getUserData() instanceof Koopa) {
                    ((Koopa) taggedEnemy.getUserData()).changeDirectionBySideSensorTag(taggedEnemy.getTag());
                } else if (taggedEnemy.getUserData() instanceof Spiky) {
                    ((Spiky) taggedEnemy.getUserData()).changeDirectionBySideSensorTag(taggedEnemy.getTag());
                }
                break;
            case Bits.ENEMY: // enemy with enemy
                ((Enemy) fixtureA.getUserData()).onEnemyHit((Enemy) fixtureB.getUserData());
                ((Enemy) fixtureB.getUserData()).onEnemyHit((Enemy) fixtureA.getUserData());
                break;
            case Bits.PLAYER | Bits.ENEMY:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                player.hit(enemy);
                break;
            case Bits.ITEM | Bits.BRICK:
            case Bits.ITEM | Bits.ITEM_BOX:
            case Bits.ITEM | Bits.GROUND:
                taggedItem = (TaggedUserData<Item>) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                if (!Item.TAG_BASE.equals(taggedItem.getTag())) {
                    taggedItem.getUserData().reverseVelocity(true, false);
                }
                break;
            case Bits.ITEM | Bits.PLAYER:
                taggedCollectableItem = (TaggedUserData<CollectableItem>) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER);
                taggedCollectableItem.getUserData().collectBy(player);
                break;
            case Bits.PLAYER_FEET | Bits.GROUND:
            case Bits.PLAYER_FEET | Bits.PLATFORM:
            case Bits.PLAYER_FEET | Bits.ITEM_BOX:
            case Bits.PLAYER_FEET | Bits.BRICK:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER_FEET);
                Object other = resolveUserData(fixtureA, fixtureB, ~Bits.PLAYER_FEET);
                player.touchGround(other);
                break;
            case Bits.BLOCK_TOP | Bits.ENEMY:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOn(enemy.getId());
                break;
            case Bits.BLOCK_TOP | Bits.ITEM:
                taggedItem = (TaggedUserData<Item>) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOn(taggedItem.getUserData().getId());
                break;
            case Bits.PLAYER | Bits.ENEMY_SIDE:
                taggedEnemy = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedEnemy.getUserData() instanceof Flower) {
                    Flower flower = (Flower) taggedEnemy.getUserData();
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

        Player player;
        Enemy enemy;
        InteractiveTileObject tileObject;
        TaggedUserData<Item> taggedItem;
        TaggedUserData<Enemy> taggedEnemy;
        switch (collisionDef) {
            case Bits.PLAYER_FEET | Bits.GROUND:
            case Bits.PLAYER_FEET | Bits.PLATFORM:
            case Bits.PLAYER_FEET | Bits.ITEM_BOX:
            case Bits.PLAYER_FEET | Bits.BRICK:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER_FEET);
                Object other = resolveUserData(fixtureA, fixtureB, ~Bits.PLAYER_FEET);
                player.leftGround(other);
                break;
            case Bits.BLOCK_TOP | Bits.ENEMY:
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOff(enemy.getId());
                break;
            case Bits.BLOCK_TOP | Bits.ITEM:
                taggedItem = (TaggedUserData<Item>) resolveUserData(fixtureA, fixtureB, Bits.ITEM);
                tileObject = (InteractiveTileObject) resolveUserData(fixtureA, fixtureB, Bits.BLOCK_TOP);
                tileObject.steppedOff(taggedItem.getUserData().getId());
                break;
            case Bits.PLAYER | Bits.ENEMY_SIDE:
                taggedEnemy = (TaggedUserData<Enemy>) resolveUserData(fixtureA, fixtureB, Bits.ENEMY_SIDE);
                if (taggedEnemy.getUserData() instanceof Flower) {
                    Flower flower = (Flower) taggedEnemy.getUserData();
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

        Player player;
        Enemy enemy;
        PretzelBullet pretzelBullet;
        Platform platform;
        switch (collisionDef) {
            case Bits.PLAYER | Bits.PLATFORM:
                player = (Player) resolveUserData(fixtureA, fixtureB, Bits.PLAYER);
                platform = (Platform) resolveUserData(fixtureA, fixtureB, Bits.PLATFORM);

                if (player.hasLastJumpThroughPlatformId()) {
                    if (platform.getId().equals(player.getLastJumpThroughPlatformId())) {
                        contact.setEnabled(false);
                    }
                } else if (player.getVelocityRelativeToGround().y > 0.5) {
                    player.setLastJumpThroughPlatformId(platform.getId());
                    contact.setEnabled(false);
                }
                break;

            case Bits.ENEMY | Bits.FIREBALL:
                // done in pre-solve to don't have an impulse from the pretzelBullet to the other object
                pretzelBullet = (PretzelBullet) resolveUserData(fixtureA, fixtureB, Bits.FIREBALL);
                enemy = (Enemy) resolveUserData(fixtureA, fixtureB, Bits.ENEMY);
                pretzelBullet.resetLater();
                enemy.kill(true);
                contact.setEnabled(false);
                break;
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
