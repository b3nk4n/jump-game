package de.bsautermeister.jump.sprites;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.serializer.BinarySerializable;

public class MarkedAction implements BinarySerializable {
    private boolean marked;
    private boolean done;

    public void reset() {
        marked = false;
        done = false;
    }

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

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeBoolean(marked);
        out.writeBoolean(done);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        marked = in.readBoolean();
        done = in.readBoolean();
    }
}
