package org.welisdoon.web.cluster;

import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.web.common.ApplicationContextProvider;


/**
 * @Classname HazelcastCluster
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/22 16:41
 */
@Configuration
@Conditional(HazelcastCluster.class)
public class HazelcastCluster implements ICluster {
    HazelcastInstance hazelcastInstance;

    @Autowired(required = false)
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public ClusterManager create() {
        return hazelcastInstance == null ? new HazelcastClusterManager(ConfigUtil.loadConfig()) : new HazelcastClusterManager(hazelcastInstance);
    }

    @Override
    public String name() {
        return "hazelcast";
    }

    /*@PostConstruct
    public void ClusterConfigurationDone() {
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
            });
        }
    }

    final public static Map<String, Handler<Void>> handlersAdd = new ConcurrentHashMap<>(4);
    final static Map<String, Handler<Object>> handlersRemove = new ConcurrentHashMap<>(4);
    final static Map<String, Handler<Object>> handlersChange = new ConcurrentHashMap<>(4);

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
    }*/
}
