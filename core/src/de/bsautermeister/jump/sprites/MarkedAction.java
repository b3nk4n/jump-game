package de.bsautermeister.jump.sprites;

public class MarkedAction {
    private boolean marked;
    private boolean done;

    public void mark() {
        marked = true;
    }

    public void done() {
        done = true;
    }

    public boolean needsAction() {
        return marked && !done;
    }

    public boolean isMarked() {
        return marked;
    }

    public boolean isDone() {
        return done;
    }
}
