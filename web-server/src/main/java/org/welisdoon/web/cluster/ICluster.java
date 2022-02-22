package org.welisdoon.web.cluster;

import io.vertx.core.spi.cluster.ClusterManager;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname ICluster
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/22 16:40
 */
public interface ICluster extends Condition {
    ClusterManager create();

    String name();

    @Override
    default boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment environment = conditionContext.getEnvironment();
        return Arrays.stream(new String[]{environment.getProperty("vertx.clusterMode"), environment.getProperty("spring.clusterMode")})
                .filter(s -> Objects.equals(this.name(), s)).findFirst().isPresent();
    }
}
