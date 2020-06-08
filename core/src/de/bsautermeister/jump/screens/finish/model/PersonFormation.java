package de.bsautermeister.jump.screens.finish.model;

import com.badlogic.gdx.utils.Array;

public class PersonFormation {

    private Array<Array<Person>> rows;

    public PersonFormation(Array<Array<Person>> rows) {
        this.rows = rows;
    }

    public void update(float delta) {
        for (Array<Person> row : rows) {
            for (Person person : row) {
                person.update(delta);
            }
        }
    }

    public Array<Person> getRow(int index) {
        return rows.get(index);
    }
}
