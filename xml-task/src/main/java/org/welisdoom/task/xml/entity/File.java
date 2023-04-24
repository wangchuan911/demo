package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.util.TypeUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.ObjectUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Classname Text
 * @Description TODO
 * @Author Septem
 * @Date 15:30
 */
@Tag(value = "file", parentTagTypes = Executable.class, desc = "读取")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "charset", desc = "编码", require = true)

public class File extends Unit implements Stream, Copyable, Iterable<Map<String, Object>> {
    final protected Map<File, Col[]> cols = new HashMap<>();

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        try {
            ObjectUtils.getMapValueOrNewSafe(cols, this, () -> getChild(Col.class).stream().toArray(Col[]::new));
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

    Reader getReader(TaskRequest data) throws FileNotFoundException {
        String mode = attributes.get("read");
        return new BufferedReader(
                new InputStreamReader(
                        Objects.equals(mode, "@stream") ? (InputStream) data.lastUnitResult : new FileInputStream(textFormat(data, mode)),
                        attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset())
        );
    }

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> promise = Promise.promise();

        try {
            Reader reader = this.getReader(data);
            try (reader) {

            }
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
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

    @Override
    public Future<Object> writer(TaskRequest data) {
        Promise<Object> promise = Promise.promise();

        try {
            Writer writer = getWriter(data);
            Col[] headers = cols.get(this);
            String delimiter = attributes.get("delimiter"), quote = MapUtils.getString(attributes, "quote", "");

            Arrays.stream(headers).map(col -> {
                try {
                    try {
                        writer.append(quote);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return TypeUtils.castToString(Ognl.getValue(col.getValue(), data.getOgnlContext(), data.getBus(), String.class));
                } catch (OgnlException e) {
                    log(String.format("col[%s] parse fail %s", col.getCode(), e.getMessage()));
                    return "";
                } finally {
                    try {
                        writer.append(quote);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }).collect(Collectors.joining(delimiter));
            writer.append("\n");
            promise.complete();
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
    }

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }
}
