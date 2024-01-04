package org.welisdoom.task.xml.connect;

import com.jcraft.jsch.*;
import io.vertx.core.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoon.common.data.Event;
import org.welisdoon.common.data.EventObject;

import java.util.*;
import java.util.function.Consumer;

/**
 * @Classname FtpConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 14:52
 */
@Component
public class SFtpConnectPool implements ConnectPool<SFtpConnectPool.SFtpClient> {
    ConfigDao configDao;
    ;

    public static class SFtpClient implements EventObject<SFtpClient.SFtpEvent, SFtpClient> {
        Map<SFtpEvent, List<Consumer<SFtpClient>>> eventMap;

        @Override
        public Map<SFtpEvent, List<Consumer<SFtpClient>>> getEventListMap() {
            return eventMap = Optional.ofNullable(eventMap).orElseGet(() -> new HashMap<>());
        }

        public enum SFtpEvent implements Event {
            Connect,
            Disconnect;
        }

        ChannelSftp channelSftp;

        public SFtpClient() {
        }

        public void connect(Session session) throws JSchException {
            Channel channel = session.openChannel("sftp");
            channel.connect();
            this.channelSftp = (ChannelSftp) channel;
            triggerEvent(SFtpEvent.Connect);
        }

        public void put(String src, String dst,
                        SftpProgressMonitor monitor) throws SftpException {
            channelSftp.put(src, dst, monitor);
        }

        public void get(String src, String dst,
                        SftpProgressMonitor monitor) throws SftpException {
            channelSftp.get(src, dst, monitor, ChannelSftp.OVERWRITE);
        }

        public void disconnect() {
            if (channelSftp != null)
                try {
                    channelSftp.disconnect();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    triggerEvent(SFtpEvent.Disconnect);
                }
            channelSftp = null;
            getEventListMap().clear();
        }
    }

    static class SFtpLinkInfo {
        final static Map<SFtpLinkInfo, SFtpLinkInfo> SESSIONS = new HashMap<>();
        final static JSch jSch = new JSch();

        Session session;
        final int port;
        final String host;
        final String user;
        List<SFtpClient> connectingClients = new LinkedList<>();

        protected Session getSession() {
            return this.session != null ? this.session : SESSIONS.get(this).session;
        }

        public SFtpLinkInfo(FtpConnectPool.FtpLinkInfo ftpLinkInfo) {
            this(ftpLinkInfo.host, ftpLinkInfo.port, ftpLinkInfo.user);
        }

        public SFtpLinkInfo(String host, int port, String user) {
            this.host = host;
            this.port = port;
            this.user = user;
        }

        synchronized SFtpClient getClient(IToken iToken, String pw) throws Throwable {
            if (!SESSIONS.containsKey(this)) {
                this.session = jSch.getSession(user, host, port);
                this.session.setPassword(pw);
                this.session.setConfig("StrictHostKeyChecking", "no");
                this.session.connect();
                SESSIONS.put(this, this);
            }

            SFtpClient sFtpClient = new SFtpClient();
            sFtpClient.addEvent(SFtpClient.SFtpEvent.Connect, connectingClients::add);
            sFtpClient.addEvent(SFtpClient.SFtpEvent.Disconnect, connectingClients::remove);
            sFtpClient.connect(getSession());
            return sFtpClient;
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

        synchronized void disconnect(final IToken token) {
            if (token == null) return;
            if (connectingClients.size() > 0) return;
            disconnect();
        }

        synchronized void disconnect() {
            if (getSession() != null) {
                try {
                    getSession().disconnect();
                } finally {
                    SESSIONS.remove(this);
                }
            }
        }

    }

    @Autowired
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public Future<SFtpClient> getConnect(String name, IToken token) {
        try {
            FtpConnectPool.FtpLinkInfo ftpLinkInfo = configDao.getFtp(name);
            return Future.succeededFuture(new SFtpLinkInfo(ftpLinkInfo).getClient(token, ftpLinkInfo.pw));
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public Future<Void> close(IToken token) {
        for (SFtpLinkInfo entry : new LinkedList<>(SFtpLinkInfo.SESSIONS.keySet())) {
            try {
                entry.disconnect(token);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return Future.succeededFuture();
    }


    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (SFtpLinkInfo entry : new LinkedList<>(SFtpLinkInfo.SESSIONS.keySet())) {
                try {
                    entry.disconnect();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    SFtpLinkInfo.SESSIONS.remove(entry);
                }
            }
        }));
    }
}
