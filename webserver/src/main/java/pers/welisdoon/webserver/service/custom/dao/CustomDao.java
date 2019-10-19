package pers.welisdoon.webserver.service.custom.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CustomDao {
    public List<Map<String, Object>> list(Map params);

    public int saveImage(Map params);
}
