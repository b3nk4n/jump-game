package de.bsautermeister.jump.physics;

public class TaggedUserData<T> {
    private T userData;
    private String tag;
    public TaggedUserData(T userData, String tag) {
        this.userData = userData;
        this.tag = tag;
    }

    public T getUserData() {
        return userData;
    }

    public String getTag() {
        return tag;
    }
}
