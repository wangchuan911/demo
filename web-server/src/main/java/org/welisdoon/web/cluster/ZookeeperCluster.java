package org.welisdoon.web.cluster;

import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Configuration
@Conditional(ZookeeperCluster.class)
public class ZookeeperCluster implements ICluster {

    @Override
    public ClusterManager create() {
        return null;
    }

    @Override
    public String name() {
        return "zookeeper";
    }
}
