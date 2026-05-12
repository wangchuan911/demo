package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Stream;

import java.io.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Classname File
 * @Description TODO
 * @Author Septem
 * @Date 15:49
 */

@Tag(value = "file", parentTagTypes = Executable.class, desc = "文件读取")
@Attr(name = "read", desc = "读文件", require = true, options = {"write", "read"})
@Attr(name = "write", desc = "写文件", require = true, options = {"write", "read"})
public class File extends StreamUnit<Stream.Writer> implements Executable, Copyable, Iterable<String> {
    @Override
    public Future<Object> read(TaskSession request) {
        String fileName = getAttrFormatValue("read", request);
        java.io.File file = StringUtils.isEmpty(fileName) ? null : new java.io.File(fileName);
        String style = Optional.ofNullable(attributes.get("style")).orElseGet(() -> "byte");
        if (file == null || !file.exists())
            return "exists".equals(style) ? Future.succeededFuture(false) : Future.failedFuture(new FileNotFoundException("file[" + fileName) + "]");
        Future<Object> future;
        try {
            switch (style) {
                case "exists":
                    future = Future.succeededFuture(true);
                    break;
                case "text":
                    future = Future.succeededFuture(StreamUtils.copyToString(new FileInputStream(file), getCharset(request)));
                    break;
                case "readline":
                    BufferedReader inputStream = new BufferedReader(new FileReader(file));
                    String text;
                    AtomicLong index = new AtomicLong(0);
                    future = Future.succeededFuture();
                    while (Objects.nonNull(text = inputStream.readLine())) {
                        future = this.bigFutureLoop(Iterable.Item.of(index.incrementAndGet(), text), 100, future, request);
                    }
                    future = listeningBreak(future.compose(o -> loopEnd(request)), inputStream, index);
                    break;
                case "byte":
                default:
                    future = Future.succeededFuture(StreamUtils.copyToByteArray(new FileInputStream(file)));
                    break;
            }
            return future;
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<Object> write(TaskSession request, Writer writer) {
        return null;
    }

    @Override
    public void readSync(TaskSession request) throws Throwable {
        String fileName = getAttrFormatValue("read", request);
        java.io.File file = StringUtils.isEmpty(fileName) ? null : new java.io.File(fileName);
        String style = Optional.ofNullable(attributes.get("style")).orElseGet(() -> "byte");
        if (file == null || !file.exists()) {
            if ("exists".equals(style)) request.setResult(this, false);
            else throw new FileNotFoundException("file[" + fileName + "]");
        }
        switch (style) {
            case "exists":
                request.setResult(this, true);
                break;
            case "text":
                request.setResult(this, StreamUtils.copyToString(new FileInputStream(file), getCharset(request)));
                break;
            case "readline":
                BufferedReader inputStream = new BufferedReader(new FileReader(file));
//                String text;
                AtomicLong index = new AtomicLong(0);
                /*while (Objects.nonNull(text = inputStream.readLine())) {
                    try {
                        this.iteratorSync(request, Item.of(index.incrementAndGet(), text));
                    } catch (Break.SkipOneLoopThrowable e) {
                        Break.onContinue(e);
                    } catch (Break.BreakLoopThrowable e) {
                        Break.onBreak(e);
                        log(e.getMessage());
                        break;
                    }
                }*/
                StreamUnit.loop(() -> inputStream.readLine(), text -> {
                    this.iteratorSync(request, Item.of(index.incrementAndGet(), text));
                });
                await(request);
                break;
            case "byte":
            default:
                request.setResult(this, StreamUtils.copyToByteArray(new FileInputStream(file)));
                break;
        }
    }

    @Override
    public void writeSync(TaskSession request, Writer writer) throws Throwable {

    }
}
