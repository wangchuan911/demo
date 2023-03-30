package org.welisdoom.task.xml.entity;

import org.apache.commons.net.ftp.FTPClient;
import org.w3c.dom.Element;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "ftp", parentTagTypes = Executable.class)
public class Ftp extends Unit implements Stream {
    FTPClient client;

    @Override
    protected void execute(Map<String, Object> data) {
        try {
            if (client == null || !client.isConnected()) {
                client = new FTPClient();
                client.connect(attributes.get("host"),
                        attributes.containsKey("port") ? Integer.valueOf(attributes.get("post")) : 22);
            }
            Iterate iterate = getChild(Iterate.class).stream().findFirst().orElse(null);
            if (iterate == null) {
                Closeable closeable;
                if (attributes.containsKey("get")) {
                    closeable = client.retrieveFileStream(attributes.get("get"));
                    try (closeable) {
                        data.put(String.format("parent.stream", getName()), closeable);
                        children.stream().filter(unit -> unit instanceof Stream).findFirst().orElse(new Unit()).execute(data);
                    }
                } else if (attributes.containsKey("put")) {
                    closeable = client.storeFileStream(attributes.get("put"));
                    try (closeable) {
                        children.stream().filter(unit -> unit instanceof Stream).forEach(unit -> {
                            unit.execute(data);
                        });
                    }
                } else
                    throw new RuntimeException("错误的操作");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
