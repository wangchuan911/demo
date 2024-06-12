package org.welisdoom.task.xml.entity;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.NoStackTraceThrowable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.UnitType;

import java.io.File;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Classname csv
 * @Description TODO
 * @Author Septem
 * @Date 12:05
 */
@Tag(value = "excel", parentTagTypes = Executable.class, desc = "csv文件读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "read", desc = "读文件", require = true, options = {"write", "read", "split"})
@Attr(name = "write", desc = "写文件", require = true, options = {"write", "read", "split"})
@Attr(name = "split", desc = "文件拆分", require = true, options = {"write", "read", "split"})
@Attr(name = "header", desc = "文件是否有标题头")

public class Excel extends Sheet implements Iterable<Map<String, Object>> {

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
        try {

            AtomicLong rowNum = new AtomicLong(0);
            Promise promise = Promise.promise();
            List<String> headers = new LinkedList<>();
            boolean hasHead = !"false".equals(attributes.get("header"));
            ExcelReaderSheetBuilder readerSheetBuilder = EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
                int max = 0;
                List<List<Object>> list = new LinkedList<>();
                AtomicLong index = new AtomicLong(0);

                @Override
                public void invoke(Map<Integer, String> data, AnalysisContext context) {
                    List<Object> row = new LinkedList<>();
                    for (int i = 0; i <= max; i++) {
                        row.add(data.getOrDefault(i, ""));
                    }
                    rowNum.incrementAndGet();
                    list.add(row);
                    if (list.size() >= maxLine - 1) {
                        String targetFileName = String.format("%s%s%s_%d%s", targetPath, File.separator, file.getName().substring(0, file.getName().lastIndexOf(".")), index.getAndIncrement(), attributes.getOrDefault("target-type", file.getName().substring(file.getName().lastIndexOf("."))));
                        log("开始写入文件{}==>{} row:{}--{}", filePath, targetFileName, rowNum.get(), rowNum.get() + maxLine);
                        EasyExcel.write(targetFileName).sheet().head(headers.stream().map(ImmutableList::of).collect(Collectors.toList())).doWrite(list);
                        list.forEach(List::clear);
                        list.clear();
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    promise.complete(filePath);
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) throws Exception {
                    promise.fail(exception);
                }

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    if (headMap.isEmpty()) {
                        return;
                    }
                    max = Math.max(max, headMap.keySet().stream().max(Integer::compare).orElse(0));
                    headers.clear();
                    for (int i = 0; i <= max; i++) {
                        headers.add(headMap.getOrDefault(i, ""));
                    }
                }
            }).sheet();
            if (hasHead) {
                readerSheetBuilder.headRowNumber(1);
                rowNum.getAndIncrement();
            } else {
                readerSheetBuilder.headRowNumber(0);
                headers.clear();
                headers.addAll(Arrays.stream(cols).map(col -> col.getCode()).collect(Collectors.toList()));
            }
            readerSheetBuilder.doRead();
            return promise.future();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        } finally {
        }

    }

    @Override
    public Future<Object> read(TaskInstance data) {
        String filePath = getAttrFormatValue("read", data);
        try {

            AtomicLong rowNum = new AtomicLong(0);
            Promise<Object> promise = Promise.promise();
            Future<Object> future = promise.future();
            List<String> headers = new LinkedList<>();
            boolean hasHead = !"false".equals(attributes.get("header"));
            ExcelReaderSheetBuilder readerSheetBuilder = EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
                int max = 0;
                AtomicLong index = new AtomicLong(0);

                @Override
                public void invoke(Map<Integer, String> data1, AnalysisContext context) {
                    if (future.isComplete()) {
                        return;
                    }
                    Map.Entry[] row = new Map.Entry[max];
                    for (int i = 0; i < max; i++) {
                        row[i] = Map.entry(headers.get(i), data1.get(i));
                    }
                    rowNum.incrementAndGet();
                    Future<Object> future = Excel.this.iterator(data, Item.of(index.incrementAndGet(), Map.ofEntries(row)));
                    future.onComplete(event -> {
                        if (event.failed()) {
                            promise.fail(event.cause());
                        }
                    });
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    if (!future.isComplete())
                        loopEnd(data).onComplete(promise);
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) throws Exception {
                    if (!future.isComplete())
                        promise.fail(exception);

                }

                @Override
                public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                    if (headMap.isEmpty()) {
                        return;
                    }
                    max = Math.max(max, headMap.keySet().stream().max(Integer::compare).orElse(0));
                    headers.clear();
                    for (int i = 0; i <= max; i++) {
                        headers.add(headMap.getOrDefault(i, ""));
                    }
                }
            }).sheet();
            if (hasHead) {
                readerSheetBuilder.headRowNumber(1);
                rowNum.getAndIncrement();
            } else {
                readerSheetBuilder.headRowNumber(0);
                headers.clear();
                headers.addAll(Arrays.stream(cols).map(col -> col.getCode()).collect(Collectors.toList()));
            }
            readerSheetBuilder.doRead();
            return future;
        } catch (Throwable e) {
            return Future.failedFuture(e);
        } finally {
        }
    }


    @Override
    Charset getCharset(TaskInstance data) {
        return Charset.forName("gbk");
    }

    protected Closeable initWriter(TaskInstance request) throws Throwable {
        throw new IllegalStateException("不支持");
    }


    @Override
    public Future<Object> write(TaskInstance data, WriteLine unit) {
        throw new IllegalStateException("不支持");
    }


    @Override
    public Future<Void> destroy(TaskInstance taskInstance) {
        return super.destroy(taskInstance);
    }

    @Override
    public Copyable copy() {
        Excel csv = (Excel) super.copy();
        return csv;
    }
}
