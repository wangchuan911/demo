package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Classname StreamUnit
 * @Description TODO
 * @Author Septem
 * @Date 17:56
 */
public abstract class StreamUnit<T extends Stream.Writer> extends Unit implements Stream<T>, Copyable, Executable {
    protected Col[] cols;
//    Map<TaskRequest, Object> map = new HashMap<>();

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }

    protected String getRead() {
        return attributes.get("read");
    }

    protected String getWrite() {
        return attributes.get("write");
    }

    @Override
    public Future<Object> write(TaskRequest data) {
        return startChildUnit(data, null, unit -> unit instanceof Executable);
    }

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            /*data.cache(this, preUnitResult);*/
            this.cols = getChild(Col.class).stream().toArray(Col[]::new);
            data.generateData(this);
            if (getRead() != null) {
                read(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else if (getWrite() != null) {
                write(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else {
                toNext.fail("未知的操作");
            }
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }

    BufferedReader getReader(TaskRequest data) throws FileNotFoundException {
        String mode = getRead();
        return new BufferedReader(
                new InputStreamReader(
                        Objects.equals(mode, "@stream") ? data.cache(this) : new FileInputStream(textFormat(data, mode)),
                        attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset())
        );
    }

    java.io.Writer getWriter(TaskRequest data) throws IOException {
        String path = textFormat(data, getWrite());
        switch (path) {
            case "@stream":
                return new PrintWriter((OutputStream) data.cache(this));
            default:
                return new FileWriter(path, getCharset(data), "true".equals(attributes.get("append")));
        }
    }

    Charset getCharset(TaskRequest data) {
        return Charset.forName(textFormat(data, attributes.getOrDefault("charset", "utf-")));
    }

    protected Future<Object> listeningBreak(Future<Object> listFuture, Closeable reader, AtomicLong index) {
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

    @Tag(value = "write-line", parentTagTypes = {Executable.class}, desc = "写入单行数据")
    public static class WriteLine extends Unit implements Writer {
        StreamUnit stream;

        @Override
        protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
            if (stream == null) {
                synchronized (this) {
                    if (stream == null)
                        if (attributes.containsKey("link"))
                            stream = (StreamUnit) getParents(aClass -> StreamUnit.class.isAssignableFrom(aClass)).stream().filter(t -> Objects.equals(t.getId(), attributes.get("link"))).findFirst().get();
                        else
                            stream = getParent(aClass -> StreamUnit.class.isAssignableFrom(aClass));
                }
            }
            ((Future<Object>) stream.write(data, this)).onSuccess(toNext::complete).onFailure(toNext::fail);
        }
    }
}
