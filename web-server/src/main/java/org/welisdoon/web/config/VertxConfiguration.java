package org.welisdoon.web.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import io.vertx.core.*;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.spi.VerticleFactory;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.cluster.ICluster;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.SpringVerticleFactory;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.verticle.AbstractMyVerticle;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@org.welisdoon.web.vertx.annotation.VertxConfiguration
@ConfigurationProperties(prefix = "vertx.options")
public class VertxConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(VertxConfiguration.class);
    @Autowired
    Reflections reflections;

    VertxOptions vertxOptions;
    Map<Class<? extends AbstractMyVerticle>, DeploymentOptions> deployOptions;
    Set<Class<? extends AbstractMyVerticle.Register>> register;
    VerticleFactory factory;

    public void setRegister(Set<Class<? extends AbstractMyVerticle.Register>> register) {
        this.register = register;
    }

    public VerticleFactory getFactory() {
        return factory;
    }

    public Set<Class<? extends AbstractMyVerticle.Register>> getRegister() {
        if (this.register == null)
            this.register = reflections.getSubTypesOf(AbstractMyVerticle.Register.class);
        return register;
    }

    @Autowired
    public void setFactory(VerticleFactory factory) {
        this.factory = factory;
    }

    public void setVertxOptions(VertxOptions vertxOptions) {
        this.vertxOptions = vertxOptions;
    }

    public void setDeployOptions(Map<String, Object> deployOptions) {
        this.deployOptions = deployOptions
                .entrySet()
                .stream()
                .collect(Collectors.toMap(stringObjectEntry -> {
                    try {
                        return (Class<? extends AbstractMyVerticle>) Class.forName(stringObjectEntry.getKey());
                    } catch (Throwable e) {
                        return null;
                    }
                }, o -> TypeUtils.castToJavaBean(o.getValue(), DeploymentOptions.class)));
    }

    public VertxOptions getVertxOptions() {
        return vertxOptions;
    }

    public Map<Class<? extends AbstractMyVerticle>, DeploymentOptions> getDeployOptions() {
        if (this.deployOptions == null)
            this.deployOptions = reflections
                    .getTypesAnnotatedWith(Verticle.class)
                    .stream()
                    .collect(Collectors.toMap(
                            aClass -> (Class<? extends AbstractMyVerticle>) aClass,
                            aClass ->
                                    new DeploymentOptions()
                                            .setMaxWorkerExecuteTime(vertxOptions.getMaxWorkerExecuteTime())
                                            .setWorkerPoolSize(vertxOptions.getWorkerPoolSize())
                                            .setWorker(aClass.getAnnotation(Verticle.class).worker())
                    ));
        return deployOptions;
    }

    /**
     * The number of {@link WorkerVerticle} instances to deploy, configured in the {@code application.properties} file.
     */

//    @Value("${vertx.extraScanPath}")
    private String[] extraScanPath = new String[]{};

    public void setExtraScanPath(String[] extraScanPath) {
        this.extraScanPath = extraScanPath;
    }

    @Bean
    Reflections getReflections() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        {
            final Collection<URL> CollectUrl = new HashSet<>();
            ApplicationContextProvider.getApplicationContext()
                    .getBeansWithAnnotation(ComponentScan.class).entrySet()
                    .stream()
                    .map(entry ->
                            ApplicationContextProvider
                                    .getRealClass(entry.getValue().getClass())
                    )
                    .flatMap(clz ->
                            Arrays.stream((clz
                                    .getAnnotation(ComponentScan.class) != null) ? clz
                                    .getAnnotation(ComponentScan.class)
                                    .basePackageClasses() : new Class[]{clz})
                    )
                    .forEach(aClass -> {
                        addPathToReflections(CollectUrl, aClass);
                    });
            Arrays.stream(extraScanPath).forEach(path -> {
                CollectUrl.addAll(ClasspathHelper.forPackage(path));
            });
            addPathToReflections(CollectUrl, WebserverApplication.class);
            logger.warn(CollectUrl.toString());
            configurationBuilder.setUrls(CollectUrl);
            configurationBuilder.setInputsFilter(s -> s.toLowerCase().endsWith(".class") || s.toLowerCase().endsWith(".java"));
        }
        return new Reflections(configurationBuilder);
    }

    protected void addPathToReflections(Collection<URL> CollectUrl, Class clz) {
        CollectUrl.addAll(ClasspathHelper.forPackage(clz.getPackageName()));
        ParserConfig.getGlobalInstance().addAccept(String.format("%s.", clz.getPackageName()));
    }

    /**
     * Verticles deploy after  springboot ready
     */
    @EventListener
    void deployVerticles(ApplicationReadyEvent event) {
        Promise<Vertx> promise = Promise.promise();
        promise.future().onSuccess(this::deployVerticles);

        ICluster[] clusters = ApplicationContextProvider.getApplicationContext().getBeansOfType(ICluster.class).entrySet().stream().map(Map.Entry::getValue).toArray(ICluster[]::new);
        switch (clusters.length) {
            case 0:
                promise.complete(Vertx.vertx(this.vertxOptions));
                logger.info("service is running with single instance.");
                break;
            case 1:
                Vertx.clusteredVertx(
                        this.vertxOptions.setClusterManager(clusters[0].create()),
                        result -> {
                            if (result.succeeded()) {
                                promise.complete(result.result());
                                logger.info("service is running with cluster by {}.", clusters[0].name());
                            } else {
                                promise.fail(result.cause());
                                logger.error("cluster running with error: "
                                        + result.cause().getMessage());
                            }
                        });
                break;
            default:
                promise.fail(new NoStackTraceThrowable("too many clusters!"));
                throw new RuntimeException("too many clusters!");

        }
    }

    protected void deployVerticles(Vertx vertx) {
        AbstractMyVerticle.prepare(vertx, this);
    }

}
