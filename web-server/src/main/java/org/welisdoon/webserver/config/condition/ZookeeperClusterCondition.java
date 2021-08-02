package org.welisdoon.webserver.config.condition;

import org.welisdoon.webserver.config.ClusterConfiguration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ZookeeperClusterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String clusteredMode = null;
        Environment environment = conditionContext.getEnvironment();
        clusteredMode =
                (clusteredMode = environment.getProperty("vertx.clusterMode")) == null
                        ? ((clusteredMode = environment.getProperty("spring.clusterMode")) == null
                        ? null : clusteredMode)
                        : clusteredMode;

        return ClusterConfiguration.CLUSTER_MODE_ZOOKEEPER.equals(clusteredMode);
    }
}
