package org.welisdoom.task.xml.connect.sync;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoom.task.xml.connect.ConnectPool;
import org.welisdoom.task.xml.connect.FtpConnectPool;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.intf.ISession;
import org.welisdoon.common.data.Event;
import org.welisdoon.common.data.EventObject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @Classname FtpConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 14:52
 */
@Component
public class SFtpConnectManager implements IConnectManager<SFtpConnectManager.SFtpSession> {
    ConfigDao configDao;
    volatile Map<String, SFtpSession> sessions = new HashMap<>();

    @Override
    public SFtpSession getConnect(String name, ISession session) throws SQLException {
        SFtpSession sFtpSession = sessions.get(name);
        if (sFtpSession != null)
            return sFtpSession;
        FtpConnectPool.FtpLinkInfo ftpLinkInfo = configDao.getFtp(name);
        sessions.put(name, new SFtpSession(ftpLinkInfo));
        return sessions.get(name);
    }

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
            try {
                if (channelSftp != null)
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

    public static class FtpInfo {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FtpInfo that = (FtpInfo) o;
            return port == that.port && Objects.equals(host, that.host) && Objects.equals(user, that.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(port, host, user);
        }
    }

    public static class SFtpSession {
        final static Set<SFtpSession> SESSIONS = new HashSet<>();
        final static JSch jSch = new JSch();

        Session session;
        FtpInfo ftpInfo;
        List<SFtpClient> connectingClients = new LinkedList<>();

        protected Session getSession() {
            return Optional.ofNullable(this.session).orElseGet(() -> SESSIONS.stream().filter(sFtpLinkInfo -> Objects.equals(sFtpLinkInfo, this)).findFirst().get().session);
        }

        public SFtpSession(FtpInfo ftpLinkInfo) {
            this.ftpInfo = ftpLinkInfo;
//            this(ftpLinkInfo.host, ftpLinkInfo.port, ftpLinkInfo.user, ftpLinkInfo.pw.getBytes(StandardCharsets.UTF_8));
        }


        public synchronized SFtpClient getClient(ISession iToken) throws Throwable {
            if (!SESSIONS.contains(this)) {
                this.session = jSch.getSession(ftpInfo.user, ftpInfo.host, ftpInfo.port);
                this.session.setPassword(new String(ftpInfo.pw.getBytes(StandardCharsets.UTF_8)));
                this.session.setConfig("StrictHostKeyChecking", "no");
                this.session.connect();
                SESSIONS.add(this);
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
            SFtpSession that = (SFtpSession) o;
            return Objects.equals(that.ftpInfo, this.ftpInfo);
        }

        @Override
        public int hashCode() {
            return ftpInfo.hashCode();
        }

        synchronized void disconnect(final ISession token) {
            if (token == null) return;
            if (connectingClients.size() > 0) return;
            disconnect();
        }

        public synchronized void disconnect() {
            try {
                if (getSession() != null)
                    getSession().disconnect();
            } finally {
                SESSIONS.remove(this);
            }
        }

    }

    @Autowired(required = false)
    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

}
