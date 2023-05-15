package org.welisdoom.task.xml.connect;

import com.jcraft.jsch.*;
import io.vertx.core.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.TaskRequest;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoon.common.ObjectUtils;

import java.util.*;

/**
 * @Classname FtpConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 14:52
 */
@Component
public class SFtpConnectPool implements ConnectPool<ChannelSftp> {
    Map<IToken, Map<String, ChannelSftp>> client = new HashMap<>();
    ConfigDao configDao;
    static Set<SFtpLinkInfo> SESSIONS = new HashSet<>();

    public static class SFtpLinkInfo {
        int port;
        String host;
        String user;
        Session session;
        static JSch jSch = new JSch();

        public SFtpLinkInfo(String host, int port, String user) {
            this.host = host;
            this.port = port;
            this.user = user;
        }

        ChannelSftp getClient(String pw) throws JSchException {
            if (session == null) {
                session = jSch.getSession(user, host, port);
                session.setPassword(pw);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
            }
            Channel channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SFtpLinkInfo that = (SFtpLinkInfo) o;
            return port == that.port && Objects.equals(host, that.host) && Objects.equals(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(port, host, user);
        }
    }

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public Future<ChannelSftp> getConnect(String name, IToken token) {
        try {
            return Future.succeededFuture(ObjectUtils.getMapValueOrNewSafe(ObjectUtils.getMapValueOrNewSafe(client, token, HashMap::new), name,
                    () -> {
                        FtpConnectPool.FtpLinkInfo ftpLinkInfo = configDao.getFtp(name);
                        SFtpLinkInfo sftpLinkInfo = new SFtpLinkInfo(ftpLinkInfo.host, ftpLinkInfo.port, ftpLinkInfo.user);
                        if (SESSIONS.contains(sftpLinkInfo)) {
                            for (SFtpLinkInfo session : SESSIONS) {
                                if (session.equals(sftpLinkInfo)) {
                                    sftpLinkInfo = session;
                                    break;
                                }
                            }
                        } else {
                            SESSIONS.add(sftpLinkInfo);
                        }
                        return sftpLinkInfo.getClient(ftpLinkInfo.pw);
                    }));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public Future<Void> close(String name, IToken token) {
        try {
            client.get(token).remove(name).disconnect();
            return Future.succeededFuture();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    public Future<Void> close(IToken token) {
        for (Map.Entry<String, ChannelSftp> stringFTPClientEntry : client.remove(token).entrySet()) {
            try {
                stringFTPClientEntry.getValue().disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return Future.succeededFuture();
    }

    static HookThread taskHookThread = new HookThread();

    static {
        Runtime.getRuntime().addShutdownHook(taskHookThread);
    }

    static class HookThread extends Thread {

        @Override
        public void run() {
            for (SFtpLinkInfo session : SESSIONS) {
                try {
                    session.session.disconnect();
                } catch (Throwable e) {
                }
            }
        }
    }
}
