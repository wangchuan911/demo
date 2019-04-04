package my.hehe.webserver.config;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import my.hehe.webserver.vertx.SpringVerticleFactory;
import my.hehe.webserver.vertx.verticle.StandaredVerticle;
import my.hehe.webserver.vertx.verticle.WorkerVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Configuration
@AutoConfigureAfter({WeChatServiceConfiguration.class, ClusterConfiguration.class})
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

    @Autowired
    ClusterConfiguration clusterConfiguration;

    /**
     * Verticles deploy after  springboot ready
     */
    @EventListener
    public void deployVerticles(ApplicationReadyEvent event) {
        VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(workerPoolSize)
                .setMaxEventLoopExecuteTime(Long.MAX_VALUE);
        this.optionManager(vertxOptions);


        if (vertxOptions.isClustered()) {
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
        vertxOptions.setAddressResolverOptions(
                new AddressResolverOptions()
                        .addServer("134.192.232.18")
                        .addServer("134.192.232.17")
                        .setMaxQueries(10));
    }

    private Consumer<Vertx> deployVerticles() {
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


}
