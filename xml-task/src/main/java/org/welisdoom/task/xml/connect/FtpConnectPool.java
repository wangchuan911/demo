package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoon.common.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname FtpConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 14:52
 */
@Component
public class FtpConnectPool implements ConnectPool {
    Map<IToken, Map<String, FTPClient>> client = new HashMap<>();
    ConfigDao configDao;

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public Future<FTPClient> getConnect(String name, IToken token) {
        try {
            return Future.succeededFuture(ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(client, token, () -> new HashMap<>()), name,
                    () -> {
                        FtpLinkInfo ftpLinkInfo = configDao.getFtp(name);
                        FTPClient client = new FTPClient();
                        client = new FTPClient();
                        client.connect(ftpLinkInfo.host,
                                ftpLinkInfo.port);
                        client.user(ftpLinkInfo.user);
                        client.pass(ftpLinkInfo.pw);
                        client.pasv();
                        client.mode(FTP.BINARY_FILE_TYPE);
                        return client;
                    }));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public Future<Void> close(String name, IToken token) {
        try {
            client.get(token).remove(name).disconnect();
            return Future.succeededFuture();
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    public Future<Void> close(IToken token) {
        for (Map.Entry<String, FTPClient> stringFTPClientEntry : client.remove(token).entrySet()) {
            try {
                stringFTPClientEntry.getValue().disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return Future.succeededFuture();
    }

    public static class FtpLinkInfo {
        String name;
        int port;
        String host;
        String user;
        String pw;
        String model;

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPw() {
            return pw;
        }

        public void setPw(String pw) {
            this.pw = pw;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
