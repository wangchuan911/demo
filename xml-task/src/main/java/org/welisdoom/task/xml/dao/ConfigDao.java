package org.welisdoom.task.xml.dao;

import org.springframework.stereotype.Repository;
import org.welisdoom.task.xml.connect.FtpConnectPool;
import org.welisdoom.task.xml.connect.sync.DatasouceConnectManager;
import org.welisdoom.task.xml.connect.sync.SFtpConnectManager;

/**
 * @Classname ConfigDao
 * @Description TODO
 * @Author Septem
 * @Date 17:49
 */
@Repository
public interface ConfigDao {
    SFtpConnectManager.FtpInfo getFtp(String name);

    DatasouceConnectManager.DatasourceInfo getDatabase(String name);

    String getTaskXML(Long id);
}
