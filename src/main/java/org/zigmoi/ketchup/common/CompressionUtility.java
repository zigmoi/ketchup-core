package org.zigmoi.ketchup.common;

import org.xerial.snappy.Snappy;

import java.io.IOException;

public class CompressionUtility {

    public static byte[] snappyCompress(byte[] bytes) throws IOException {
        return Snappy.compress(bytes);
    }

    public static byte[] snappyDecompress(byte[] bytes) throws IOException {
        return Snappy.uncompress(bytes);
    }
}
