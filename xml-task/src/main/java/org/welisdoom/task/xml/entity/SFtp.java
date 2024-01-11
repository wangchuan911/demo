package org.welisdoom.task.xml.entity;

import com.jcraft.jsch.SftpProgressMonitor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.connect.SFtpConnectPool;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.Optional;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "sftp", parentTagTypes = Executable.class, desc = "sftp读写")
@Attr(name = "id", desc = "唯一标识")
@Attr(name = "get", desc = "获取流", require = true, options = {"get", "put"})
@Attr(name = "put", desc = "写入文件", require = true, options = {"get", "put"})
@Attr(name = "local", desc = "本地文件位置", require = true, options = {"get", "put"})

public class SFtp extends Ftp implements Executable, Copyable {
//    Map<TaskRequest, Cache> ftpClientMap = new HashMap<>();

    @Override
    public Future<Object> read(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        SFtpConnectPool sFtpConnectPool = ApplicationContextProvider.getBean(SFtpConnectPool.class);
        sFtpConnectPool.getConnect(getId(), data).onSuccess(session -> {
            try {
                SFtpConnectPool.SFtpClient client = session.getClient(data);
                try {
                    data.cache(this, client);
                    String file = getAttrFormatValue("local", data), remote = getAttrFormatValue("get", data);
                    log(String.format("%s=====>%s", remote, file));
                    client.get(remote, file, new MySftpProgressMonitor(this));
                    toNext.complete(file);
                } finally {
                    client.disconnect();
                }
            } catch (Throwable throwable) {
                toNext.fail(throwable);
            }
        }).onFailure(toNext::fail);
        return toNext.future();
    }


    @Override
    public Future<Object> write(TaskRequest data) {
        Promise<Object> toNext = Promise.promise();
        SFtpConnectPool sFtpConnectPool = ApplicationContextProvider.getBean(SFtpConnectPool.class);
        sFtpConnectPool.getConnect(getId(), data).onSuccess(session -> {
            try {
                SFtpConnectPool.SFtpClient client = session.getClient(data);
                try {
                    data.cache(this, client);
                    String file = getAttrFormatValue("local", data), remote = getAttrFormatValue("put", data);
                    client.put(file, remote, new MySftpProgressMonitor(this));
                    toNext.complete(file);
                } finally {
                    client.disconnect();
                }
            } catch (Throwable throwable) {
                toNext.fail(throwable);
            }
        }).onFailure(toNext::fail);
        return toNext.future();
    }

    @Override
    public Copyable copy() {
        SFtp ftp = copyableUnit(this);
//        ftp.ftpClientMap = this.ftpClientMap;
        return ftp;
    }

    /*@Override
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

   /* @Override
    protected Future<Void> destroy(TaskRequest data) {
        return ApplicationContextProvider.getBean(SFtpConnectPool.class).close(data).transform(v -> super.destroy(data));
    }*/

    @Override
    protected Future<Void> disconnectFtp(TaskRequest data) {
        Optional<SFtpConnectPool.SFtpSession> optional = Optional.ofNullable(data.cache(this));
        if (optional.isPresent()) {
            try {
                optional.get().disconnect();
            } catch (Throwable e) {
                return Future.failedFuture(e);
            }
        }
        return Future.succeededFuture();
    }

    static class MySftpProgressMonitor implements SftpProgressMonitor {
        long max, current;
        SFtp sFtp;

        MySftpProgressMonitor(SFtp sFtp) {
            this.sFtp = sFtp;
        }

        @Override
        public void init(int i, String s, String s1, long l) {
            max = l;
            sFtp.log(String.format("begin %s : from [%s] to [%s]", SftpProgressMonitor.PUT == i ? "upload" : "download", s, s1));
        }

        @Override
        public boolean count(long l) {
            current += l;
            sFtp.logNoTag(String.format("\rtransfer:%6.3f %s ======> [%019d/%019d]", (100f * current / max), "%", current, max));
            return l > 0;
        }

        @Override
        public void end() {
            System.out.println();
            sFtp.log(String.format("success!!!"));
            sFtp = null;
        }
    }
}
