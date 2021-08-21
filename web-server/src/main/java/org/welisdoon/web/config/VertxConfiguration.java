package org.welisdoon.web.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.welisdoon.web.WebserverApplication;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.common.web.AsyncProxyUtils;
import org.welisdoon.web.common.web.intf.ICommonAsynService;
import org.welisdoon.web.vertx.SpringVerticleFactory;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.annotation.VertxRegister;
import org.welisdoon.web.vertx.verticle.AbstractCustomVerticle;
import org.welisdoon.web.vertx.verticle.WorkerVerticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

@Configuration
@AutoConfigureAfter({ClusterConfiguration.class})
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

    @Autowired
    ClusterConfiguration clusterConfiguration;

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
            final Collection<URL> CollectUrl = new LinkedList<>();
            ApplicationContextProvider.getApplicationContext()
                    .getBeansWithAnnotation(ComponentScan.class).entrySet().stream().map(Map.Entry::getValue).flatMap(o ->
                    Arrays.stream(ApplicationContextProvider
                            .getRealClass(o.getClass())
                            .getAnnotation(ComponentScan.class)
                            .basePackageClasses())
            ).forEach(aClass -> {
                CollectUrl.addAll(ClasspathHelper.forPackage(aClass.getPackageName()));
            });
            Arrays.stream(extraScanPath).forEach(path -> {
                CollectUrl.addAll(ClasspathHelper.forPackage(path));
            });

            CollectUrl.addAll(ClasspathHelper.forPackage(WebserverApplication.class.getPackageName()));
            logger.warn(CollectUrl.toString());
            configurationBuilder.setUrls(CollectUrl);
            configurationBuilder.setInputsFilter(s -> s.toLowerCase().endsWith(".class") || s.toLowerCase().endsWith(".java"));
        }
        return new Reflections(configurationBuilder);
    }

    /**
     * Verticles deploy after  springboot ready
     */
    @EventListener
    public void deployVerticles(ApplicationReadyEvent event) {
        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(workerPoolSize)
                .setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        this.optionManager(vertxOptions);


        if (vertxOptions.getClusterManager() != null) {
            Vertx.clusteredVertx(
                    vertxOptions,
                    result -> {
                        if (result.succeeded()) {
                            Vertx vertx = result.result();
                            this.deployVerticles().accept(vertx);
                            logger.info("service is running with cluster by hazelcast.");
                            vertx.sharedData().getAsyncMap("aa", asyncMapAsyncResult -> {
                                asyncMapAsyncResult.result().put("aa", "aa", voidAsyncResult -> {
                                    System.out.println(voidAsyncResult.result());
                                });
                            });
                        } else {
                            logger.error("cluster running with error: "
                                    + result.cause().getMessage());
                        }
                    });
        } else {
            Vertx vertx = Vertx.vertx(vertxOptions);
            this.deployVerticles().accept(vertx);
            logger.info("service is running with single instance hazelcast.");
        }
    }

    //option初始化
    void optionManager(VertxOptions vertxOptions) {
        //集群配置
        clusterConfiguration.clusterManager(vertxOptions);
        this.dnsManger(vertxOptions);

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

    private Consumer<Vertx> deployVerticles() {
        Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);
        AbstractCustomVerticle.initVertxInSpring(new AbstractCustomVerticle.Options());
        Consumer<Vertx> runner = vertx -> {
            // The verticle factory is registered manually because it is created by the Spring container
            vertx.registerVerticleFactory(verticleFactory);

            reflections.getTypesAnnotatedWith(Verticle.class).stream().forEach(clz -> {
                Verticle verticle = clz.getAnnotation(Verticle.class);
                DeploymentOptions options = new DeploymentOptions();
                String verticleName = verticleFactory.naming(clz.getName());
                if (verticle.worker()) {
                    options.setWorker(true)
                            // As worker verticles are never executed concurrently by Vert.x by more than one thread,
                            // deploy multiple instances to avoid serializing requests.
                            .setInstances(workerInstancesMax);
                }
                vertx.deployVerticle(verticleName, options, ar -> {
                    if (ar.failed()) {
                        logger.error("Failed to deploy book verticle", ar.cause());
                    } else {
                        logger.info("deploy success!{}", verticleName);
                    }
                });
            });

            /*//多线程计数
            CountDownLatch deployLatch = new CountDownLatch(1);
            //多线程时使用该类可以提供原子性操作
            AtomicBoolean failed = new AtomicBoolean(false);
            {
                String verticleName = verticleFactory.prefix() + ":" + StandaredVerticle.class.getName();
                vertx.deployVerticle(verticleName, ar -> {
                    if (ar.failed()) {
                        logger.error("Failed to deploy book verticle", ar.cause());
                        //如果failed的值是false则置为true
                        failed.compareAndSet(false, true);
                    }
                    //发布成功计数减1
                    deployLatch.countDown();
                });
            }
            {
                DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
                        .setWorker(true)
                        // As worker verticles are never executed concurrently by Vert.x by more than one thread,
                        // deploy multiple instances to avoid serializing requests.
                        .setInstances(workerInstancesMax);
                String workerVerticleName = verticleFactory.prefix() + ":" + WorkerVerticle.class.getName();
                vertx.deployVerticle(workerVerticleName, workerDeploymentOptions, ar -> {
                    if (ar.failed()) {
                        logger.error("Failed to deploy verticle", ar.cause());
                        //如果failed的值是false则置为true
                        failed.compareAndSet(false, true);
                    }
                    //发布成功计数减1
                    deployLatch.countDown();
                });
            }*/
        };
        return runner;
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncService() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            logger.info(String.format("create AsyncService:%s", AsyncProxyUtils.createServiceBinder(vertx1, null, ICommonAsynService.class)));
        };
        return vertxConsumer;
    }
}
