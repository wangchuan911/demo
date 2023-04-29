package org.welisdoom.task.xml.dao;

import org.springframework.stereotype.Repository;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;

import java.util.Map;

/**
 * @Classname ConfigDao
 * @Description TODO
 * @Author Septem
 * @Date 17:49
 */
@Repository
public interface ConfigDao {
    Map<String, Object> getFtp(String name);

    DataBaseConnectPool.DatabaseLinkInfo getDatabase(String name);

    String getTaskXML(Long id);
}
