package org.welisdoom.task.xml.entity;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname csv
 * @Description TODO
 * @Author Septem
 * @Date 12:05
 */
@Tag(value = "col", parentTagTypes = Executable.class)
public class Csv extends Unit implements Stream {
    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        String read = attributes.get("read");
        if (StringUtils.isNotEmpty(read)) {
            CSVReader csvReader;
            try {
                csvReader = new CSVReaderBuilder(
                        new BufferedReader(
                                new InputStreamReader(
                                        Objects.equals(read, "@stream") ? (InputStream) data.lastUnitResult : new FileInputStream(read))
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
                        headers = getChild(Col.class).stream().map(col -> col.getId()).toArray(String[]::new);
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
                                startChildUnit(data, Map.ofEntries(entries), org.welisdoom.task.xml.entity.Iterator.class)
                        );
                    }
                    listFuture.onSuccess(toNext::complete).onFailure(toNext::fail);
                }
            } catch (Throwable e) {
                toNext.fail(e);
            }
        }
    }
}
