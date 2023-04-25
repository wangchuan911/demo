package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Stream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname StreamUnit
 * @Description TODO
 * @Author Septem
 * @Date 17:56
 */
public abstract class StreamUnit extends Unit implements Stream, Copyable {
    protected Col[] cols;

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        try {
            this.cols = getChild(Col.class).stream().toArray(Col[]::new);
            data.generateData(this);
            if (attributes.containsKey("read")) {
                read(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else if (attributes.containsKey("writer")) {
                writer(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else {
                toNext.fail("未知的操作");
            }
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }

    BufferedReader getReader(TaskRequest data) throws FileNotFoundException {
        String mode = attributes.get("read");
        return new BufferedReader(
                new InputStreamReader(
                        Objects.equals(mode, "@stream") ? (InputStream) data.lastUnitResult : new FileInputStream(textFormat(data, mode)),
                        attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset())
        );
    }

    Writer getWriter(TaskRequest data) throws IOException {
        String path = attributes.get("writer");
        switch (path) {
            case "@stream":
                return new PrintWriter((OutputStream) data.lastUnitResult);
            default:
                return new FileWriter(path);
        }
    }

    protected Future<Object> listeningBreak(Future<Object> listFuture, Closeable reader, AtomicInteger index) {
        Promise<Object> promise = Promise.promise();
        listFuture
                .onSuccess(promise::complete)
                .onFailure(throwable -> {
                    Break.onBreak(throwable, promise, index.get());
                })
                .onComplete(objectAsyncResult -> {
                    try (reader) {
//                        reader.close();
                    } catch (IOException e) {
                        log(e.getMessage());
                    }
                });
        return promise.future();
    }
}
