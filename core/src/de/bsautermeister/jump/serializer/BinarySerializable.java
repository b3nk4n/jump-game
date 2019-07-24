package de.bsautermeister.jump.serializer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface BinarySerializable {
    void write(final DataOutputStream out) throws IOException;

    void read(final DataInputStream in) throws IOException;
}
