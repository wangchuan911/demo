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
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.xml.sax.Attributes;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @Classname Text
 * @Description TODO
 * @Author Septem
 * @Date 15:30
 */
@Tag(value = "sheet", parentTagTypes = Executable.class, desc = "表单文件")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "charset", desc = "编码", require = true)
@Attr(name = "delimiter", desc = "分隔符", require = true)
@Attr(name = "quote", desc = "字符引号", require = true)
@Attr(name = "line-break", desc = "换行", require = true)


public class Sheet extends StreamUnit<StreamUnit.WriteLine> implements Iterable<Map<String, Object>> {
//    final protected Map<TaskRequest, Writer> writer = new HashMap<>();

    String delimiter, quote, linebreak;

    protected String[] readLine(BufferedReader reader, StringBuilder builder) throws IOException {
        String line;
        int index;
        /*String linebreak = MapUtils.getString(Sheet.this.attributes, "line-break");
        String delimiter = MapUtils.getString(Sheet.this.attributes, "delimiter");*/
        String quote = MapUtils.getString(Sheet.this.attributes, "quote");
        while ((line = reader.readLine()) != null) {
            if ((index = (line += "\n").indexOf(linebreak)) >= 0) {
                builder.append(line, 0, index);
                String[] strings = builder.toString().split(delimiter);
                builder.setLength(0);
                builder.append(line, index + linebreak.length(), line.length());
                return StringUtils.isEmpty(quote) ? strings : Arrays.stream(strings).map(s -> s.substring(quote.length(), s.length() - quote.length())).toArray(value -> strings);
            } else {
                builder.append(line);
            }
        }
        return null;
    }

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        this.delimiter = this.attributes.getOrDefault("delimiter", ",");
        this.quote = MapUtils.getString(this.attributes, "quote", "");
        this.linebreak = MapUtils.getString(Sheet.this.attributes, "line-break", "\n");
        return this;
    }

    @Override
    public Future<Object> read(TaskRequest data) {

        StringBuilder builder = new StringBuilder("");
        try {
            BufferedReader reader = this.getReader(data);

            String[] line;
            String[] headers;
            if ("false".equals(attributes.get("header"))) {
                if ((headers = readLine(reader, builder)) == null) {
                    throw new RuntimeException("文件获取文件头失败");
                }
            } else {
                headers = Arrays.stream(cols).map(col -> col.getCode()).toArray(String[]::new);
            }
            String[] values;
            Future<Object> listFuture = Future.succeededFuture();
            AtomicLong index = new AtomicLong(0);
            List<Map.Entry> entries = new LinkedList<>();
            while ((line = readLine(reader, builder)) != null) {
                values = line;
                entries.clear();
                for (int i = 0; i < Math.min(values.length, headers.length); i++) {
                    if (StringUtils.isEmpty(values[i]))
                        values[i] = "";
                    entries.add(Map.entry(headers[i], values[i]));
                }
                    /*listFuture = listFuture.compose(o ->
//                            startChildUnit(data, Map.ofEntries(entries), Iterable.class)
                                    this.iterator(data, Item.of(index.incrementAndGet(), Map.ofEntries(entries)))
                    );*/
                listFuture = this.futureLoop(Map.ofEntries(entries.toArray(Map.Entry[]::new)), index, listFuture, data);
                if (index.get() % 100 == 0) {
                    Thread.sleep(0);
                }

            }
            return listeningBreak(listFuture, reader, index);


        } catch (Throwable e) {
            log(builder.toString());
            return Future.failedFuture(e);
        }
    }

    protected Closeable initWriter(TaskRequest request) throws Throwable {
        return getWriter(request);
    }

    @Override
    public Future<Object> write(TaskRequest request) {
        try {
            request.cache(this, () -> initWriter(request));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
        return super.write(request).onComplete(event -> {
            try {
                Closeable closeable = request.clearCache(this);
                closeable.close();
                /*File file = new File(textFormat(request, getWrite()));
                log("文件名：" + file.getAbsolutePath());
                log("文件存在：" + file.exists());
                log("文件大小：" + file.length());*/
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Future<Object> write(TaskRequest data, StreamUnit.WriteLine unit) {
        Promise<Object> promise = Promise.promise();

        try {
            java.io.Writer writer = data.cache(this, () -> getWriter(data));
            writer.append(unit.getChild(Col.class).stream().map(col -> {
                try {
                    return TypeUtils.castToString(Ognl.getValue(col.getValue(), data.getOgnlContext(), data.getBus(), String.class));
                } catch (OgnlException e) {
                    log(String.format("col[%s] parse fail %s", col.getCode(), e.getMessage()));
                    return "";
                }
            }).map(s -> StringUtils.isEmpty(quote) ? s : String.format("%s%s%s", quote, s, quote)).collect(Collectors.joining(delimiter)))
                    .append("\n");
            promise.complete();
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        java.io.Writer writer = taskRequest.clearCache(this);
        super.destroy(taskRequest);
        try (writer) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
