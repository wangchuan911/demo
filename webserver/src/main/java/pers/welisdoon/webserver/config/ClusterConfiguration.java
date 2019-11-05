package pers.welisdoon.webserver.config;

import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import pers.welisdoon.webserver.common.ApplicationContextProvider;
import pers.welisdoon.webserver.config.condition.HazelcastClusterCondition;
import pers.welisdoon.webserver.config.condition.ZookeeperClusterCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import pers.welisdoon.webserver.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ClusterConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConfiguration.class);

    final public static Map<String, Handler<Void>> handlersAdd = new ConcurrentHashMap<>(4);
    final static Map<String, Handler<Object>> handlersRemove = new ConcurrentHashMap<>(4);
    final static Map<String, Handler<Object>> handlersChange = new ConcurrentHashMap<>(4);

    final public static String CLUSTER_MODE_HAZELCAST = "hazelcast";
    final public static String CLUSTER_MODE_ZOOKEEPER = "zookeeper";

    @Autowired
    ApplicationContextProvider applicationContextProvider;

    /**
     * cluster power [true/false]
     */
    @Value("${vertx.clusterMode}")
    String CLUSTERED_MODE;

    boolean CLUSTERED_IS_ENABLE = false;

    @Bean
    @Conditional(HazelcastClusterCondition.class)
    public Config hazelcastClusterConfig() {
        CLUSTERED_IS_ENABLE = true;
        return ConfigUtil.loadConfig();
    }

    @Bean
    @Conditional(ZookeeperClusterCondition.class)
    public Config zookeeperClusterConfig() {
        CLUSTERED_IS_ENABLE = true;
        return null;
    }

    @PostConstruct
    public void ClusterConfigurationDone() {
        if (!CLUSTERED_IS_ENABLE) return;
        this.listenMembersState();
    }

    private void listenMembersState() {
        HazelcastInstance hazelcastInstance = applicationContextProvider.getBean(HazelcastInstance.class);
        if (hazelcastInstance != null) {
            hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
                @Override
                public void memberAdded(MembershipEvent membershipEvent) {
                    handlersAdd.forEach((s, handler) -> {
                        handler.handle(null);
                    });

                }

                @Override
                public void memberRemoved(MembershipEvent membershipEvent) {
                    handlersRemove.forEach((s, handler) -> {
                        handler.handle(null);
                    });
                }

                @Override
                public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
                    handlersChange.forEach((s, handler) -> {
                        handler.handle(null);
                    });
                }
            });
        }
    }

    public static Handler memberAddedListener(String keyName, Handler handler) {
        if (handler == null) {
            return handlersAdd.remove(keyName);
        }
        return handlersAdd.put(keyName, handler);
    }

    public static Handler memberRemovedListener(String keyName, Handler handler) {
        if (handler == null) {
            return handlersRemove.remove(keyName);
        }
        return handlersRemove.put(keyName, handler);
    }

    public static Handler memberAttributeChangedListener(String keyName, Handler handler) {
        if (handler == null) {
            return handlersChange.remove(keyName);
        }
        return handlersChange.put(keyName, handler);
    }

    public void clusterManager(VertxOptions options) {
        ClusterManager mgr = null;
        if (CLUSTERED_IS_ENABLE && !StringUtils.isEmpty(CLUSTERED_MODE)) {
            switch (CLUSTERED_MODE) {
                case CLUSTER_MODE_HAZELCAST:
                    HazelcastInstance hazelcastInstance = applicationContextProvider.getBean(HazelcastInstance.class);
                    mgr = hazelcastInstance == null ? new HazelcastClusterManager(ConfigUtil.loadConfig()) : new HazelcastClusterManager(hazelcastInstance);
                    break;
                case CLUSTER_MODE_ZOOKEEPER:
                    mgr = null;
            }
        }
        if (mgr != null) {
            options.setClusterManager(mgr);
        }
    }
}

