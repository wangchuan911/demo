package org.welisdoon.common;

import java.io.*;

public interface StreamUtils {

    static void write(InputStream in, OutputStream out) throws IOException {
        try (in; out) {
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
        }
    }

    static void write(Reader in, Writer out) throws IOException {
        try (in; out) {
            char[] b = new char[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
                out.flush();
            }
            out.flush();
        }
    }
}
