package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoom.task.xml.intf.type.UnitType;

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
    public Future<Object> write(TaskInstance data) {
        return startChildUnit(data, null, unit -> unit instanceof Executable);
    }

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        try {
            /*data.cache(this, preUnitResult);*/
            this.cols = getChild(Col.class).stream().toArray(Col[]::new);
            data.generateData(this);
            return operation(data, preUnitResult);

        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    protected Future<Object> operation(TaskInstance data, Object preUnitResult) {
        if (getRead() != null) {
            return read(data);
        } else if (getWrite() != null) {
            return write(data);
        } else {
            return Future.failedFuture("未知的操作");
        }
    }

    BufferedReader getReader(TaskInstance data) throws FileNotFoundException {
        String mode = getRead();
        return new BufferedReader(
                new InputStreamReader(
                        Objects.equals(mode, "@stream") ? data.cache(this) : new FileInputStream(UnitType.textFormat(data, mode)),
                        attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset())
        );
    }

    java.io.Writer getWriter(TaskInstance data) throws IOException {
        String path = UnitType.textFormat(data, getWrite());
        switch (path) {
            case "@stream":
                return new PrintWriter((OutputStream) data.cache(this));
            default:
                return new FileWriter(path, getCharset(data), "true".equals(attributes.get("append")));
        }
    }

    Charset getCharset(TaskInstance data) {
        return Charset.forName(UnitType.textFormat(data, attributes.getOrDefault("charset", "utf-8")));
    }

    protected Future<Object> listeningBreak(Future<Object> listFuture, Closeable reader, AtomicLong index) {
        return listFuture.compose(Future::succeededFuture, throwable -> Break.onBreak(throwable, index.get()))
                .onComplete(objectAsyncResult -> {
                    try (reader) {
//                        reader.close();
                    } catch (IOException e) {
                        log(e.getMessage());
                    }
                });
    }

    @Tag(value = "write-line", parentTagTypes = {Executable.class}, desc = "写入单行数据")
    public static class WriteLine extends Unit implements Writer {
        StreamUnit stream;

        @Override
        protected Future<Object> start(TaskInstance data, Object preUnitResult) {
            if (stream == null) {
                synchronized (this) {
                    if (stream == null)
                        if (attributes.containsKey("link"))
                            stream = (StreamUnit) getParents(aClass -> StreamUnit.class.isAssignableFrom(aClass)).stream().filter(t -> Objects.equals(t.getId(), attributes.get("link"))).findFirst().get();
                        else
                            stream = getParent(aClass -> StreamUnit.class.isAssignableFrom(aClass));
                }
            }
            return stream.write(data, this);
        }
    }
}
