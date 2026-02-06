package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.connect.sync.SFtpConnectManager;
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
@Deprecated
public class FtpConnectPool implements ConnectPool<FTPClient> {
    Map<IToken, Map<String, FTPClient>> client = new HashMap<>();
    ConfigDao configDao;

    @Autowired(required = false)
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public Future<FTPClient> getConnect(String name, IToken token) {
        try {
            return Future.succeededFuture(ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(client, token, () -> new HashMap<>()), name,
                    () -> {
                        SFtpConnectManager.FtpInfo ftpLinkInfo = configDao.getFtp(name);
                        FTPClient client = new FTPClient();
                        client.connect(ftpLinkInfo.getHost(),
                                ftpLinkInfo.getPort());
                        client.user(ftpLinkInfo.getUser());
                        client.pass(ftpLinkInfo.getPw());
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

    public static class FtpLinkInfo extends SFtpConnectManager.FtpInfo {

    }
}
