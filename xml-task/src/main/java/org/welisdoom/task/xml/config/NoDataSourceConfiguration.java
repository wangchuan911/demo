package org.welisdoom.task.xml.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StreamUtils;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.connect.FtpConnectPool;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.GuessCharset;
import org.welisdoon.common.ObjectUtils;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname DataSourceConfiguration
 * @Description TODO
 * @Author Septem
 * @Date 16:32
 */
@Configuration
@ConfigurationProperties("xml-task")
@ConditionalOnMissingBean(value = DataSourceConfiguration.class)
public class NoDataSourceConfiguration {


    Map<String, Object> connections;

    Map<Long, String> templates;

    Map<Long, String> templatesCache = new HashMap<>();

    public void setConnections(Map<String, Object> connections) {
        this.connections = connections;
    }

    public void setTemplates(Map<Long, String> templates) {
        this.templates = templates;
    }

    @Bean
    public ConfigDao getConfigDao() {
        return new ConfigDao() {
            @Override
            public FtpConnectPool.FtpLinkInfo getFtp(String name) {
                return JSON.toJavaObject((JSON) JSONObject.toJSON(connections.get(name)), FtpConnectPool.FtpLinkInfo.class);
            }

            @Override
            public DataBaseConnectPool.DatabaseLinkInfo getDatabase(String name) {
                return JSON.toJavaObject((JSON) JSONObject.toJSON(connections.get(name)), DataBaseConnectPool.DatabaseLinkInfo.class);
            }

            @Override
            public String getTaskXML(Long id) {
                try {
                    return ObjectUtils.getMapValueOrNewSafe(templatesCache, id, () -> {
                        String path = templates.get(id);
                        return StreamUtils.copyToString(new FileInputStream(path), Charset.forName(GuessCharset.guessCharset(path)));
                    });
                } catch (Throwable e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
    }
}
