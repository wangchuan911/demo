package org.welisdoom.task.xml.entity;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.commons.lang3.StringUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.ObjectUtils;

import java.io.*;
import java.util.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Classname csv
 * @Description TODO
 * @Author Septem
 * @Date 12:05
 */
@Tag(value = "csv", parentTagTypes = Executable.class, desc = "csv文件读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "read", desc = "读文件", require = true, options = {"writer", "read"})
@Attr(name = "writer", desc = "写文件", require = true, options = {"writer", "read"})

public class Csv extends Unit implements Executable, Stream, Copyable {
    Map<TaskRequest, CSVWriter> map = new HashMap<>();

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        if (attributes.containsKey("read")) {
            read(data).onSuccess(toNext::complete).onFailure(toNext::fail);
        } else if (attributes.containsKey("writer")) {
            writer(data).onSuccess(toNext::complete).onFailure(toNext::fail);
        } else {
            toNext.fail("未知的操作");
        }

    }

    public static Future<String> guessCharset(File file) {
        Promise<String> promise = Promise.promise();
        Future<String> future = promise.future();
        Map<String, AtomicInteger> map = new HashMap<>();
        try {
            nsDetector detector = new nsDetector();
            detector.Init(charset -> {
                try {
                    ObjectUtils.getMapValueOrNewSafe(map, charset, () -> new AtomicInteger(0)).incrementAndGet();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            try (input) {
                byte[] buffer = new byte[4096];
                int hasRead;
                while ((hasRead = input.read(buffer)) != -1) {
                    if (detector.isAscii(buffer, hasRead)) {
                        detector.Report("ASCII");
                    } else {
                        detector.DoIt(buffer, hasRead, false);
                    }
                }
            } finally {
                detector.DataEnd();
            }

            promise.complete(map.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().get())).collect(Collectors.toList()).get(0).getKey());
        } catch (Throwable e) {
            promise.fail(e);
        }
        return future;
    }

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> promise = Promise.promise();
        String mode = attributes.get("read");
        CSVReader csvReader;
        try {
            csvReader = new CSVReaderBuilder(
                    new BufferedReader(
                            new InputStreamReader(
                                    Objects.equals(mode, "@stream") ? (InputStream) data.lastUnitResult : new FileInputStream(mode))
                    )
            ).build();
            try (csvReader) {
                Iterator<String[]> iterator = csvReader.iterator();
                String[] headers;
                if ("false".equals(attributes.get("header"))) {
                    if (iterator.hasNext()) {
                        headers = iterator.next();
                    } else {
                        throw new RuntimeException("文件获取文件头失败");
                    }
                } else {
                    headers = getChild(Col.class).stream().map(col -> col.getCode()).toArray(String[]::new);
                }
                Map.Entry[] entries = new Map.Entry[headers.length];
                String[] values;
                Future<Object> listFuture = Future.succeededFuture();
                while (iterator.hasNext()) {
                    values = iterator.next();
                    for (int i = 0; i < values.length; i++) {
                        if (StringUtils.isEmpty(values[i]))
                            values[i] = "";
                        entries[i] = Map.entry(headers[i], values[i]);
                    }
                    listFuture = listFuture.compose(o ->
                            startChildUnit(data, Map.ofEntries(entries), Iterable.class)
                    );
                }
                listFuture.onSuccess(promise::complete).onFailure(promise::fail);
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
            CSVWriter csvWriter = getCSVWriter(data);
            Col[] headers = getChild(Col.class).stream().toArray(Col[]::new);
            csvWriter.writeNext(Arrays.stream(headers).map(col -> {
                try {
                    return Ognl.getValue(col.getValue(), data.getOgnlContext(), data.getBus(), String.class);
                } catch (OgnlException e) {
                    log(String.format("col[%s] parse fail %s", col.getCode(), e.getMessage()));
                    return "";
                }
            }).toArray(String[]::new));
            promise.complete();
        } catch (Throwable e) {
            promise.fail(e);
        }
        return promise.future();
    }

    CSVWriter getCSVWriter(TaskRequest data) throws Throwable {
        return ObjectUtils.getMapValueOrNewSafe(map, data, () -> {
            String path = attributes.get("writer");
            return new CSVWriter(new FileWriter(path));
        });
    }

    @Override
    public void destroy(TaskRequest taskRequest) {
        super.destroy(taskRequest);
        try {
            map.get(taskRequest).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Copyable copy() {
        Csv csv = new Csv();
        csv.setId(this.id);
        csv.attributes = this.attributes;
        csv.map = this.map;
        return csv;
    }
}
