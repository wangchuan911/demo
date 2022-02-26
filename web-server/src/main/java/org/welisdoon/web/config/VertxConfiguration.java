package org.welisdoon.web.config;

import com.alibaba.fastjson.parser.ParserConfig;
import io.vertx.core.*;
import io.vertx.core.dns.AddressResolverOptions;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
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
public class VertxConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(VertxConfiguration.class);


    @Autowired
    @Qualifier("verticleFactory")
    private SpringVerticleFactory verticleFactory;

    /**
     * The Vert.x worker pool size, configured in the {@code application.properties} file.
     * <p>
     * Make sure this is greater than {@link #workerInstancesMax}.
     */
    @Value("${vertx.worker.pool.size}")
    int workerPoolSize;

    /**
     * The number of {@link WorkerVerticle} instances to deploy, configured in the {@code application.properties} file.
     */
    @Value("${vertx.workerVerticle.instances}")
    int workerInstancesMax;

    @Value("${vertx.extraScanPath}")
    private String[] extraScanPath;

    @Value("${vertx.dns.enable}")
    boolean dnsEnable;
    @Value("${vertx.dns.ip}")
    String[] dnsIp;
    @Value("${vertx.dns.maxQueries}")
    Integer dnsMaxQueries;


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
    public void deployVerticles(ApplicationReadyEvent event) {
        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(workerPoolSize)
                .setMaxEventLoopExecuteTime(Long.MAX_VALUE);

        this.dnsManger(vertxOptions);
        ICluster[] clusters = ApplicationContextProvider.getApplicationContext().getBeansOfType(ICluster.class).entrySet().stream().map(Map.Entry::getValue).toArray(ICluster[]::new);
        switch (clusters.length) {
            case 0:
                this.deployVerticles(Vertx.vertx(vertxOptions));
                logger.info("service is running with single instance.");
                break;
            case 1:
                vertxOptions.setClusterManager(clusters[0].create());
                Vertx.clusteredVertx(
                        vertxOptions,
                        result -> {
                            if (result.succeeded()) {
                                this.deployVerticles(result.result());
                                logger.info("service is running with cluster by {}.", clusters[0].name());
                            } else {
                                logger.error("cluster running with error: "
                                        + result.cause().getMessage());
                            }
                        });
                break;
            default:
                throw new RuntimeException("too many clusters!");

        }
    }


    private void dnsManger(VertxOptions vertxOptions) {
        AddressResolverOptions options = new AddressResolverOptions();
        if (dnsEnable) {
            if (dnsIp != null) {
                for (int i = 0; i < dnsIp.length; i++) {
                    options.addServer(dnsIp[i]);
                }
            }
            if (dnsMaxQueries > 0) {
                options.setMaxQueries(10);
            }
        }
        vertxOptions.setAddressResolverOptions(options);
    }

    @Autowired(required = false)
    AbstractMyVerticle.Options options;

    protected void deployVerticles(Vertx vertx) {
        Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);

        AbstractMyVerticle.prepare(vertx, options != null ? options :
                new AbstractMyVerticle.Options()
                        .setRegister(reflections.getSubTypesOf(AbstractMyVerticle.Register.class).toArray(Class[]::new))
                        .setFactory(verticleFactory)
                        .setVerticle(reflections
                                .getTypesAnnotatedWith(Verticle.class)
                                .stream()
                                .filter(aClass -> ApplicationContextProvider.getApplicationContext()
                                        .containsBean(aClass.getAnnotation(Component.class).value())).toArray(Class[]::new)
                        )
                        .setWorkerInstancesMax(workerInstancesMax)
        );
    }

}
