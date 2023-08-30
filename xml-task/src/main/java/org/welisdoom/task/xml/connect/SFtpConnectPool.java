package org.welisdoom.task.xml.connect;

import com.jcraft.jsch.*;
import io.vertx.core.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
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
    ConfigDao configDao;
    static Map<String, SFtpLinkInfo> SESSIONS = new HashMap<>();

    static class SFtpLinkInfo {
        final int port;
        final String host;
        final String user;
        private Session session;
        final static JSch jSch = new JSch();
        final Set<IToken> iTokens = new HashSet<>();

        public SFtpLinkInfo(FtpConnectPool.FtpLinkInfo ftpLinkInfo) {
            this(ftpLinkInfo.host, ftpLinkInfo.port, ftpLinkInfo.user);
        }

        public SFtpLinkInfo(String host, int port, String user) {
            this.host = host;
            this.port = port;
            this.user = user;
        }

        synchronized ChannelSftp getClient(IToken iToken, String pw) throws JSchException {
            iTokens.add(iToken);
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

        synchronized void disconnect(IToken token) {
            if (token == null) return;
            iTokens.remove(token);
            if (iTokens.size() == 0) {
                disconnect();
            }
        }

        synchronized void disconnect() {
            if (session != null) {
                try {
                    session.disconnect();
                    session = null;
                } finally {
                    String key = generalKey(user, host, port);
                    SESSIONS.remove(key);
                }
            }
        }

        static String generalKey(String user, String host, int port) {
            return String.format("%s@%s:%s", user, host, port);
        }
    }

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public Future<ChannelSftp> getConnect(String name, IToken token) {
        try {
            FtpConnectPool.FtpLinkInfo ftpLinkInfo = configDao.getFtp(name);
            String key = SFtpLinkInfo.generalKey(ftpLinkInfo.user, ftpLinkInfo.host, ftpLinkInfo.port);
            SFtpLinkInfo sftpLinkInfo = ObjectUtils.getMapValueOrNewSafe(SESSIONS, key, () -> new SFtpLinkInfo(ftpLinkInfo));
            return Future.succeededFuture(sftpLinkInfo.getClient(token, ftpLinkInfo.pw));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public Future<Void> close(IToken token) {
        for (Map.Entry<String, SFtpLinkInfo> stringFTPClientEntry : new LinkedList<>(SESSIONS.entrySet())) {
            try {
                stringFTPClientEntry.getValue().disconnect(token);
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
            for (SFtpLinkInfo session : SESSIONS.values()) {
                try {
                    session.disconnect();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
