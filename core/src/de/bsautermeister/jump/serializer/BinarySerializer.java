package de.bsautermeister.jump.serializer;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class BinarySerializer {

    /**
     * The version to check for compatibility, in case the expected data has changed.
     */
    private final static byte VERSION = 0x01;

    /**
     * The header written to the binary file.
     */
    private final static byte[] HEADER = {0x53, 0x4e, 0x45, 0x47, 0x47, VERSION};

    public static boolean write(BinarySerializable serializable, OutputStream output) {
        DataOutputStream out = new DataOutputStream(output);
        try {
            out.write(HEADER);
            serializable.write(out);
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            tryClose(out);
        }
    }

    public static boolean read(BinarySerializable serializable, InputStream input) {
        DataInputStream in = new DataInputStream(input);
        try {
            if (checkHeader(in)); {
                serializable.read(in);
                return true;
            }
        } catch (IOException ex) {
            return false;
        } finally {
            tryClose(in);
        }
    }

    private static boolean checkHeader(DataInputStream in) throws IOException {
        byte[] savedBuffer = new byte[HEADER.length];
        in.readFully(savedBuffer);
        return Arrays.equals(savedBuffer, HEADER);
    }


    private static void tryClose(Closeable out) {
        try {
            out.close();
        } catch (IOException ex) {
        }
    }
}

