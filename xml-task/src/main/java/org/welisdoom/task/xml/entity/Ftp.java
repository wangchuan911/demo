package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.apache.commons.net.ftp.FTPClient;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.welisdoon.common.ObjectUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "ftp", parentTagTypes = Executable.class)
public class Ftp extends Unit implements Stream {
    Map<TaskRequest, FTPClient> ftpClientMap = new HashMap<>();

    @Override

    protected Future<Object> execute(TaskRequest data) {
        try {
            if (true)
                return Future.succeededFuture();
            FTPClient client = ObjectUtils.getMapValueOrNewSafe(ftpClientMap, data, () -> new FTPClient());
            if (!client.isConnected()) {
                client = new FTPClient();
                ftpClientMap.put(data, client);
                client.connect(attributes.get("host"),
                        attributes.containsKey("port") ? Integer.valueOf(attributes.get("post")) : 22);
            }
            Iterate iterate = getChild(Iterate.class).stream().findFirst().orElse(null);
            if (iterate == null) {
                Closeable closeable;
                if (attributes.containsKey("get")) {
                    closeable = client.retrieveFileStream(attributes.get("get"));
                    try (closeable) {
                        data.setBus(this, String.format("@stream", getId()), closeable);
                        children.stream().filter(unit -> unit instanceof Stream).findFirst().orElse(new Unit()).execute(data);
                    }
                } else if (attributes.containsKey("put")) {
                    closeable = client.storeFileStream(attributes.get("put"));
                    try (closeable) {
                        return execute(data, Stream.class);
                    }
                } else
                    throw new RuntimeException("错误的操作");
            }
            return Future.succeededFuture();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void destroy(TaskRequest request) {
        try {
            FTPClient client = ftpClientMap.remove(request);
            if (client != null) client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
