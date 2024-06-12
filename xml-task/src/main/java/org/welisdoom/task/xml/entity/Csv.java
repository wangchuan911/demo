package org.welisdoom.task.xml.entity;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import io.vertx.core.Future;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.UnitType;

import java.io.*;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * @Classname csv
 * @Description TODO
 * @Author Septem
 * @Date 12:05
 */
@Tag(value = "csv", parentTagTypes = Executable.class, desc = "csv文件读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "read", desc = "读文件", require = true, options = {"write", "read", "split"})
@Attr(name = "write", desc = "写文件", require = true, options = {"write", "read", "split"})
@Attr(name = "split", desc = "文件拆分", require = true, options = {"write", "read", "split"})
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
    protected Future<Object> operation(TaskInstance data, Object preUnitResult) {
        if (attributes.containsKey("split")) {
            return split(data);
        }
        return super.operation(data, preUnitResult);
    }

    public Future<Object> split(TaskInstance data) {
        String targetPath = getAttrFormatValue("split", data);
        String filePath = getAttrFormatValue("read", data);
        File file = new File(filePath);
        int maxLine = Integer.parseInt(attributes.get("max-lines"));
        if (!file.exists()) {
            return Future.failedFuture("file not found");
        }
        CSVReader source = null;
        CSVWriter target = null;
        try {
            Charset charset = attributes.containsKey("charset") ? Charset.forName(getAttrFormatValue("charset", data)) : Charset.defaultCharset();
            source = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(new FileInputStream(filePath), charset))).build();

            Iterator<String[]> iterator = source.iterator();
            String[] headers;
            AtomicLong index = new AtomicLong(0);
            AtomicLong rowNum = new AtomicLong(0);
            AtomicLong offset = new AtomicLong(maxLine);
            if ("false".equals(attributes.get("header"))) {
                if (iterator.hasNext()) {
                    headers = iterator.next();
                    rowNum.incrementAndGet();
                } else {
                    throw new RuntimeException("文件获取文件头失败");
                }
            } else {
                headers = Arrays.stream(cols).map(col -> col.getCode()).toArray(String[]::new);
            }
            Supplier<CSVWriter> supplier = () -> {
                String targetFileName = String.format("%s%s%s_%d%s", targetPath, File.separator, file.getName().substring(0, file.getName().lastIndexOf(".")), index.getAndIncrement(), file.getName().substring(file.getName().lastIndexOf(".")));
                log("开始写入文件{}==>{} row:{}--{}", filePath, targetFileName, rowNum.get(), rowNum.get() + maxLine);
                try {
                    return new CSVWriter(new FileWriter(targetFileName, charset));
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            };
            target = supplier.get();
            target.writeNext(headers);
            offset.decrementAndGet();
            while (iterator.hasNext()) {
                if (offset.getAndDecrement() <= 0) {
                    if (target != null) {
                        target.close();
                    }
                    target = supplier.get();
                    target.writeNext(headers);
                    offset.set(maxLine);
                    offset.decrementAndGet();
                }
                target.writeNext(iterator.next());
                rowNum.getAndIncrement();
            }
            return Future.succeededFuture(targetPath);
        } catch (Throwable e) {
            return Future.failedFuture(e);
        } finally {
            IOUtils.closeQuietly(source, target);
        }
    }

    @Override
    public Future<Object> read(TaskInstance data) {
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
    Charset getCharset(TaskInstance data) {
        return Charset.forName("gbk");
    }

    protected Closeable initWriter(TaskInstance request) throws Throwable {
        CSVWriter writer = new CSVWriter(getWriter(request));
        if ("true".equals(attributes.get("header"))) {
            writer.writeNext(Arrays.stream(this.cols).map(Col::getName).toArray(String[]::new));
            writer.flush();
        }
        return writer;
    }


    @Override
    public Future<Object> write(TaskInstance data, StreamUnit.WriteLine unit) {
        try {
            CSVWriter csvWriter = data.cache(this);
            String[] value = unit.getChild(Col.class).stream().map(col -> UnitType.textFormat(data, col.getValue())).toArray(String[]::new);
            log(Arrays.toString(value));
            csvWriter.writeNext(value);
            csvWriter.flush();
            return Future.succeededFuture();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }


    @Override
    public Future<Void> destroy(TaskInstance taskInstance) {
        CSVWriter csvWriter = taskInstance.clearCache(this);
        try (csvWriter) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.destroy(taskInstance);
    }

    @Override
    public Copyable copy() {
        Csv csv = (Csv) super.copy();
//        csv.map = this.map;
        return csv;
    }
}
