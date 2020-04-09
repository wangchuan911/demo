package org.welisdoon.webserver.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.welisdoon.webserver.WebserverApplication;
import org.welisdoon.webserver.common.ApplicationContextProvider;
import org.welisdoon.webserver.common.web.intf.ICommonAsynService;
import org.welisdoon.webserver.vertx.SpringVerticleFactory;
import org.welisdoon.webserver.vertx.annotation.VertxRegister;
import org.welisdoon.webserver.vertx.verticle.AbstractCustomVerticle;
import org.welisdoon.webserver.vertx.verticle.StandaredVerticle;
import org.welisdoon.webserver.vertx.verticle.WorkerVerticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Configuration
@AutoConfigureAfter({WeChatServiceConfiguration.class, ClusterConfiguration.class})
@org.welisdoon.webserver.vertx.annotation.VertxConfiguration
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

    @Value("${vertx.scanPath}")
    private String[] scanPath;

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
            Arrays.stream(scanPath).forEach(path -> {
                CollectUrl.addAll(ClasspathHelper.forPackage(path));
            });
            if (CollectionUtils.isEmpty(CollectUrl)) {
                CollectUrl.addAll(ClasspathHelper.forPackage(WebserverApplication.class.getPackageName()));
            }
            configurationBuilder.setUrls(CollectUrl);
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
        AbstractCustomVerticle.scanRegister(ApplicationContextProvider.getBean(Reflections.class));
        Consumer<Vertx> runner = vertx -> {
            // The verticle factory is registered manually because it is created by the Spring container
            vertx.registerVerticleFactory(verticleFactory);
            //多线程计数
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
            }
       /*
        String restApiVerticleName = verticleFactory.prefix() + ":" + RestApi.class.getName();
        vertx.deployVerticle(restApiVerticleName, ar -> {
            if (ar.failed()) {
                Logger.error("Failed to deploy book verticle", ar.cause());
                //如果failed的值是false则置为true
                failed.compareAndSet(false, true);
            }
            //发布成功计数减1
            deployLatch.countDown();
        });

        DeploymentOptions workerDeploymentOptions = new DeploymentOptions()
                .setWorker(true)
                // As worker verticles are never executed concurrently by Vert.x by more than one thread,
                // deploy multiple instances to avoid serializing requests.
                .setInstances(springWorkerInstances);
        String workerVerticleName = verticleFactory.prefix() + ":" + SpringWorker.class.getName();
        vertx.deployVerticle(workerVerticleName, workerDeploymentOptions, ar -> {
            if (ar.failed()) {
                logger.error("Failed to deploy verticle", ar.cause());
                //如果failed的值是false则置为true
                failed.compareAndSet(false, true);
            }
            //发布成功计数减1
            deployLatch.countDown();
        });
        */
            /*try {
                //如果10秒还未发布成功则超时，抛出异常
                if (!deployLatch.await(10, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout waiting for verticle deployments");
                } else if (failed.get()) {
                    throw new RuntimeException("Failure while deploying verticles");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/
        };
        return runner;
    }

    @VertxRegister(WorkerVerticle.class)
    public Consumer<Vertx> createAsyncService() {
        Consumer<Vertx> vertxConsumer = vertx1 -> {
            logger.info(String.format("create AsyncService:%s", ICommonAsynService.create(vertx1)));
        };
        return vertxConsumer;
    }
}
