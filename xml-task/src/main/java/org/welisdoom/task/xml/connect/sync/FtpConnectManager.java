package org.welisdoom.task.xml.connect.sync;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.connect.ConnectPool;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.intf.ISession;
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
public class FtpConnectManager implements IConnectManager<FTPClient> {
    Map<ISession, Map<String, FTPClient>> client = new HashMap<>();
    ConfigDao configDao;

    @Autowired(required = false)
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public FTPClient getConnect(String name, ISession session) throws Throwable {
        return ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(client, session, () -> new HashMap<>()), name,
                () -> {
                    SFtpConnectManager.FtpInfo ftpLinkInfo = configDao.getFtp(name);
                    FTPClient client = new FTPClient();
                    client.connect(ftpLinkInfo.host,
                            ftpLinkInfo.port);
                    client.user(ftpLinkInfo.user);
                    client.pass(ftpLinkInfo.pw);
                    client.pasv();
                    client.mode(FTP.BINARY_FILE_TYPE);
                    return client;
                });
    }

    public void close(String name, ISession token) throws IOException {
        client.get(token).remove(name).disconnect();
    }

    public void close(ISession token) {
        for (Map.Entry<String, FTPClient> stringFTPClientEntry : client.remove(token).entrySet()) {
            try {
                stringFTPClientEntry.getValue().disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static class FtpLinkInfo extends SFtpConnectManager.FtpInfo {

    }
}
