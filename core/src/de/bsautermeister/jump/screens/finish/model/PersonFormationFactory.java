package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public final class PersonFormationFactory {
    private final static int BRO_COL_IDX = 12;
    private final static int GIRLFRIEND_COL_IDX = 13;

    private PersonFormationFactory() {}

    public static PersonFormation createRandomFormation() {
        switch (MathUtils.random(3)) {
            case 0:
                return createBlockCheersFormation();
            case 1:
                return createLaolaCheersFormation();
            case 2:
                return createDiagonalLaolaCheersFormation();
            default:
                return createSwingRowFormation();
        }
    }

    public static PersonFormation createBlockCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 8; ++i) {
            result.add(createBlockCheersRow());
        }
        Array<Person> frontRow = result.get(result.size - 1);
        frontRow.get(BRO_COL_IDX).setCharacterIdx(0);
        frontRow.get(BRO_COL_IDX).setSwingAngle(3f);
        frontRow.get(BRO_COL_IDX).setDelay(0f);
        frontRow.get(GIRLFRIEND_COL_IDX).setCharacterIdx(1);
        frontRow.get(GIRLFRIEND_COL_IDX).setSwingAngle(3f);
        frontRow.get(GIRLFRIEND_COL_IDX).setDelay(0f);
        return new PersonFormation(result);
    }

    public static PersonFormation createSwingRowFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 8; ++i) {
            result.add(createSwingRow(i * 0.2f, 10f));
        }
        Array<Person> frontRow = result.get(result.size - 1);
        frontRow.get(BRO_COL_IDX).setCharacterIdx(0);
        frontRow.get(BRO_COL_IDX).setSpriteAnimation(createCheersAnimation());
        frontRow.get(GIRLFRIEND_COL_IDX).setCharacterIdx(1);
        frontRow.get(GIRLFRIEND_COL_IDX).setSpriteAnimation(createCheersAnimation());
        return new PersonFormation(result);
    }

    private static Array<Person> createBlockCheersRow() {
        return Array.with(
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                new Person(0f, createCheersAnimation()),
                Person.empty(),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                new Person(1f, createCheersAnimation()),
                Person.empty(),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation()),
                new Person(2f, createCheersAnimation())
        );
    }

    public static PersonFormation createLaolaCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 8; ++i) {
            result.add(createLaolaCheersRow(0.1f));
        }
        Array<Person> frontRow = result.get(result.size - 1);
        frontRow.get(BRO_COL_IDX).setCharacterIdx(0);
        frontRow.get(BRO_COL_IDX).setSwingAngle(3f);
        frontRow.get(GIRLFRIEND_COL_IDX).setCharacterIdx(1);
        frontRow.get(GIRLFRIEND_COL_IDX).setSwingAngle(3f);
        return new PersonFormation(result);
    }

    private static Array<Person> createLaolaCheersRow(float delay) {
        Array<Person> result = new Array<>(26);
        for (int i = 0; i < 26; ++i) {
            if ((i + 1) % 9 == 0) {
                result.add(Person.empty());
                continue;
            }
            result.add(new Person(i * delay, createCheersAnimation()));
        }
        return result;
    }

    public static PersonFormation createDiagonalLaolaCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 8; ++i) {
            result.add(createDiagonalLaolaCheersRow(0.125f * i, 0.1f));
        }
        Array<Person> frontRow = result.get(result.size - 1);
        frontRow.get(BRO_COL_IDX).setCharacterIdx(0);
        frontRow.get(BRO_COL_IDX).setSwingAngle(3f);
        frontRow.get(GIRLFRIEND_COL_IDX).setCharacterIdx(1);
        frontRow.get(GIRLFRIEND_COL_IDX).setSwingAngle(3f);
        return new PersonFormation(result);
    }

    private static Array<Person> createDiagonalLaolaCheersRow(float delayOffset, float delay) {
        Array<Person> result = new Array<>(26);
        for (int i = 0; i < 26; ++i) {
            if ((i + 1) % 9 == 0) {
                result.add(Person.empty());
                continue;
            }
            result.add(new Person(delayOffset + i * delay, createCheersAnimation()));
        }
        return result;
    }

    private static Animatable createCheersAnimation() {
        return new PathAnimation(0f, true, Array.with(
                new PathAnimation.Item(1.0f, 1f),
                new PathAnimation.Item(2.0f, 1f),
                new PathAnimation.Item(1.0f, 0f),
                new PathAnimation.Item(2.0f, 0f),
                new PathAnimation.Item(1.0f, 1f),
                new PathAnimation.Item(2.0f, 1f),
                new PathAnimation.Item(1.0f, 0f),
                new PathAnimation.Item(2.0f, 0f)
        ));
    }

    private static Array<Person> createSwingRow(float delta, float swingAngle) {
        FixedAnimation fixedAnimation = new FixedAnimation(1f);
        return Array.with(
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                Person.empty(),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                Person.empty(),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle),
                new Person(delta, fixedAnimation, swingAngle)
        );
    }
}
