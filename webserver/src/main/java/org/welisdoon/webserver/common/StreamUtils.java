package org.welisdoon.webserver.common;

import java.io.*;

public interface StreamUtils {
    static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void writeStream(InputStream in, OutputStream out) throws IOException {
        byte[] b = new byte[1024];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
    }

    static void writeStream(Reader in, Writer out) throws IOException {
        char[] b = new char[4096];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
            out.flush();
        }
        out.flush();
    }
}
