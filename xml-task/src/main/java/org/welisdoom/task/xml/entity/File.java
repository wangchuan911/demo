package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.util.TypeUtils;
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
import org.xml.sax.Attributes;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
@Attr(name = "delimiter", desc = "分隔符", require = true)
@Attr(name = "quote", desc = "字符引号", require = true)
@Attr(name = "line-break", desc = "换行", require = true)


public class File extends StreamUnit implements Iterable<Map<String, Object>> {
    String delimiter, quote, linebreak;
    final protected Map<TaskRequest, Writer> writer = new HashMap<>();



    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        this.delimiter = MapUtils.getString(this.attributes, "delimiter", ",");
        this.quote = MapUtils.getString(this.attributes, "quote", ",");
        this.linebreak = MapUtils.getString(this.attributes, "linebreak", ",");
        return this;
    }



    protected String[] readLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {

        }
        return null;
    }

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> promise = Promise.promise();

        try {
            BufferedReader reader = this.getReader(data);

            String[] line;
            try (reader) {
                String[] headers;
                if ("false".equals(attributes.get("header"))) {
                    if ((headers = readLine(reader)) == null) {
                        throw new RuntimeException("文件获取文件头失败");
                    }
                } else {
                    headers = Arrays.stream(cols).map(col -> col.getCode()).toArray(String[]::new);
                }
                Map.Entry[] entries = new Map.Entry[headers.length];
                String[] values;
                Future<Object> listFuture = Future.succeededFuture();
                AtomicInteger index = new AtomicInteger(0);
                while ((line = readLine(reader)) != null) {
                    values = line;
                    for (int i = 0; i < values.length; i++) {
                        if (StringUtils.isEmpty(values[i]))
                            values[i] = "";
                        if (i >= headers.length) break;
                        entries[i] = Map.entry(headers[i], values[i]);
                    }
                    /*listFuture = listFuture.compose(o ->
//                            startChildUnit(data, Map.ofEntries(entries), Iterable.class)
                                    this.iterator(data, Item.of(index.incrementAndGet(), Map.ofEntries(entries)))
                    );*/
                    listFuture = bigFutureLoop(countReset(index, 500, 0), 500, listFuture,
                            o -> this.iterator(data, Item.of(index.incrementAndGet(), Map.ofEntries(entries))));
                }
            }
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
    }



    @Override
    public Future<Object> writer(TaskRequest data) {
        Promise<Object> promise = Promise.promise();

        try {
            Writer writer = getWriter(data);
            Col[] headers = this.cols;
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

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        try {
            if (this.writer.containsKey(taskRequest))
                this.writer.remove(taskRequest).close();
        } catch (IOException e) {
            log(e.getMessage());
        }
    }
}
