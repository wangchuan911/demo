package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.net.ftp.FTPClient;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.ObjectUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "ftp", parentTagTypes = Executable.class)
public class Ftp extends Unit implements Stream, Executable, Copyable {
    Map<TaskRequest, Cache> ftpClientMap = new HashMap<>();

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        Cache cache = null;
        try {
            cache = getCache(data);
            FTPClient client = getClient(cache);
            Closeable closeable = client.retrieveFileStream(attributes.get("get"));
            cache.closeables.add(closeable);
            toNext.complete(closeable);
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
        return toNext.future();
    }

    @Override
    public Future<Object> writer(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        Cache cache = null;
        try {
            cache = getCache(data);
            FTPClient client = getClient(cache);
            Closeable closeable = client.storeFileStream(attributes.get("put"));
            cache.closeables.add(closeable);
            toNext.complete(closeable);
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
        return toNext.future();
    }

    @Override
    public  Copyable copy() {
        Ftp ftp = new Ftp();
        ftp.setId(this.id);
        ftp.attributes = this.attributes;
        ftp.ftpClientMap = this.ftpClientMap;
        return ftp;
    }

    static class Cache {
        Cache(FTPClient client) {
            this.client = client;
        }

        FTPClient client;
        Set<Closeable> closeables = new HashSet<>();

        void destroy() {
            try {
                if (client != null) client.disconnect();
                for (Closeable closeable : this.closeables) {
                    try (closeable) {

                    }
                }
                this.closeables.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Cache getCache(TaskRequest data) throws Throwable {
        return ObjectUtils.getMapValueOrNewSafe(ftpClientMap, data, () -> new Cache(new FTPClient()));
    }

    FTPClient getClient(Cache cache) throws Throwable {
        FTPClient client = cache.client;
        if (!client.isConnected()) {
            client = new FTPClient();
            client.connect(attributes.get("host"),
                    attributes.containsKey("port") ? Integer.valueOf(attributes.get("post")) : 22);
        }
        return client;
    }

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        log("ftp");
        if (true) {
            super.start(data, toNext);
            return;
        }
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
    }

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

    @Override
    public void destroy(TaskRequest request) {
        ftpClientMap.remove(request).destroy();
    }
}
