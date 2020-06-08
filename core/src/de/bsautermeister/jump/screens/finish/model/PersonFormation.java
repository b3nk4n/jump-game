package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.utils.Array;

public final class PersonFormation {
    private PersonFormation() {}

    public static Array<Array<Person>> createBlockCheersFormation() {
        Array<Array<Person>> result = new Array<>();
        for (int i = 0; i < 9; ++i) {
            result.add(createBlockCheersRow());
        }
        return result;
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
}
