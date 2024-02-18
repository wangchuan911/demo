package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.net.ftp.FTPClient;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.FtpConnectPool;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.StreamUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "ftp", parentTagTypes = Executable.class, desc = "ftp读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "get", desc = "获取流", require = true, options = {"get", "put"})
@Attr(name = "put", desc = "写入文件", require = true, options = {"get", "put"})
@Attr(name = "local", desc = "本地文件位置", require = true, options = {"get", "put"})

public class Ftp extends StreamUnit<Stream.Writer> implements Executable, Copyable {
//    Map<TaskRequest, Cache> ftpClientMap = new HashMap<>();

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        ApplicationContextProvider.getBean(FtpConnectPool.class).getConnect(getId(), data).onSuccess(client -> {
            data.cache(this, client);
            try {
                String file = getAttrFormatValue("local", data), remote = getAttrFormatValue("get", data);
                log(String.format("%s=====>%s", remote, file));
                StreamUtils.write(client.retrieveFileStream(remote), new FileOutputStream(file));
                toNext.complete(file);
            } catch (Throwable throwable) {
                toNext.fail(throwable);
            }
        }).onFailure(toNext::fail);
        return toNext.future();
    }

    public Future<Object> write(TaskRequest data, StreamUnit.WriteLine unit) {
        return Future.failedFuture("不支持的操作");
    }

    @Override
    public Future<Object> write(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        ApplicationContextProvider.getBean(FtpConnectPool.class).getConnect(getId(), data).onSuccess(client -> {
            data.cache(this, client);
            try {
                String file = getAttrFormatValue("local", data), remote = getAttrFormatValue("put", data);
                log(String.format("%s=====>%s", file, remote));
                StreamUtils.write(new FileInputStream(file), client.storeFileStream(remote));
                toNext.complete(file);
            } catch (Throwable throwable) {
                toNext.fail(throwable);
            }
        }).onFailure(toNext::fail);
        return toNext.future();
    }

    @Override
    public Future<Object> write(TaskRequest request, Writer writer) {
        return Future.failedFuture("无效的操作");
    }

    @Override
    public Copyable copy() {
        Ftp ftp = copyableUnit(this);
//        ftp.ftpClientMap = this.ftpClientMap;
        return ftp;
    }

    @Override
    protected String getRead() {
        return attributes.get("get");
    }

    @Override
    protected String getWrite() {
        return attributes.get("put");
    }
   /* @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        *//*log("ftp");
        if (true) {
            super.start(data, preUnitResult, toNext);
            return;
        }*//*
        try {
            if (attributes.containsKey("get")) {
                this.read(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else if (attributes.containsKey("put")) {
                this.writer(data).onSuccess(toNext::complete).onFailure(toNext::fail);
            } else {
                toNext.fail("未知的操作");
                return;
            }
        } catch (Throwable e) {
            toNext.fail(e);
        }
    }*/

    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println("ftp");
        if (true) {
            data.next(null);
            return;
        }
        Cache cache = ObjectUtils.getMapValueOrNewSafe(ftpClientMap, data, () -> new Cache(new FTPClient()));
        FTPClient client = cache.client;
        if (!client.isConnected()) {
            client = new FTPClient();
            client.connect(attributes.get("host"),
                    attributes.containsKey("port") ? Integer.valueOf(attributes.get("post")) : 22);
        }
        Iterate iterate = getChild(Iterate.class).stream().findFirst().orElse(null);
        if (iterate == null) {
            Closeable closeable;
            if (attributes.containsKey("get")) {
                closeable = client.retrieveFileStream(attributes.get("get"));
                cache.closeables.add(closeable);
                Future<?> future = Future.succeededFuture(closeable);
                super.execute(data, future);
            } else if (attributes.containsKey("put")) {
                closeable = client.storeFileStream(attributes.get("put"));
                cache.closeables.add(closeable);
                data.next(closeable);
            } else
                throw new RuntimeException("错误的操作");
        }
    }*/
    protected Future<Void> disconnectFtp(TaskRequest data) {
        Optional<FTPClient> optional = Optional.ofNullable(data.cache(this));
        if (optional.isPresent()) {
            try {
                optional.get().disconnect();
            } catch (IOException e) {
                return Future.failedFuture(e);
            }
        }
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> destroy(TaskRequest data) {
        return disconnectFtp(data).transform(objectAsyncResult -> super.destroy(data));
    }
}
