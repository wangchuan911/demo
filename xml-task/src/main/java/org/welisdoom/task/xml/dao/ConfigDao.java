package org.welisdoom.task.xml.dao;

import org.springframework.stereotype.Repository;

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

    Map<String, Object> getDatabase(String name);


}
