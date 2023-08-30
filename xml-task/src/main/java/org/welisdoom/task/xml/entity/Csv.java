package org.welisdoom.task.xml.entity;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Classname csv
 * @Description TODO
 * @Author Septem
 * @Date 12:05
 */
@Tag(value = "csv", parentTagTypes = Executable.class, desc = "csv文件读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "read", desc = "读文件", require = true, options = {"write", "read"})
@Attr(name = "write", desc = "写文件", require = true, options = {"write", "read"})
@Attr(name = "header", desc = "文件是否有标题头")

public class Csv extends Sheet implements Iterable<Map<String, Object>> {
//    Map<TaskRequest, CSVWriter> map = new HashMap<>();
    /*@Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        data.generateData(this);
        if (attributes.containsKey("read")) {
            read(data).onSuccess(toNext::complete).onFailure(toNext::fail);
        } else if (attributes.containsKey("writer")) {
            writer(data).onSuccess(toNext::complete).onFailure(toNext::fail);
        } else {
            toNext.fail("未知的操作");
        }

    }*/


    @Override
    public Future<Object> read(TaskRequest data) {
        CSVReader csvReader;
        try {
            csvReader = new CSVReaderBuilder(
                    /*new BufferedReader(
                            new InputStreamReader(
                                    Objects.equals(mode, "@stream") ? (InputStream) data.lastUnitResult : new FileInputStream(textFormat(data, mode)),
                                    attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset())
                    )*/
                    this.getReader(data)
            ).build();

            Iterator<String[]> iterator = csvReader.iterator();
            String[] headers;
            if ("false".equals(attributes.get("header"))) {
                if (iterator.hasNext()) {
                    headers = iterator.next();
                } else {
                    throw new RuntimeException("文件获取文件头失败");
                }
            } else {
                headers = Arrays.stream(cols).map(col -> col.getCode()).toArray(String[]::new);
            }
            List<Map.Entry> entries = new LinkedList<>();
            String[] values;
            Future<Object> listFuture = Future.succeededFuture();
            AtomicLong index = new AtomicLong(0);
            while (iterator.hasNext()) {
                values = iterator.next();
                entries.clear();
                for (int i = 0, len = Math.min(headers.length, values.length); i < len; i++) {
                    if (StringUtils.isEmpty(values[i]))
                        values[i] = "";
                    entries.add(Map.entry(headers[i], values[i]));
                }
                listFuture = this.bigFutureLoop(Item.of(index.incrementAndGet(), Map.ofEntries(entries.toArray(Map.Entry[]::new))), 100, listFuture, data);
            }
            /*return listFuture.onComplete(objectAsyncResult -> {
                try (csvReader) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/
            return listeningBreak(listFuture.compose(o -> loopEnd(data)), csvReader, index);
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    Charset getCharset(TaskRequest data) {
        return Charset.forName("gbk");
    }

    protected Closeable initWriter(TaskRequest request) throws Throwable {
        CSVWriter writer = new CSVWriter(getWriter(request));
        if ("true".equals(attributes.get("header"))) {
            writer.writeNext(Arrays.stream(this.cols).map(Col::getName).toArray(String[]::new));
            writer.flush();
        }
        return writer;
    }


    @Override
    public Future<Object> write(TaskRequest data, StreamUnit.WriteLine unit) {
        Promise<Object> promise = Promise.promise();
        try {
            CSVWriter csvWriter = data.cache(this);
            String[] value = unit.getChild(Col.class).stream().map(col -> textFormat(data, col.getValue())).toArray(String[]::new);
            log(Arrays.toString(value));
            csvWriter.writeNext(value);
            csvWriter.flush();
            promise.complete();
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
    }


    @Override
    public Future<Void> destroy(TaskRequest taskRequest) {
        CSVWriter csvWriter = taskRequest.clearCache(this);
        try (csvWriter) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.destroy(taskRequest);
    }

    @Override
    public Copyable copy() {
        Csv csv = (Csv) super.copy();
//        csv.map = this.map;
        return csv;
    }
}
