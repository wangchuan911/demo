package org.welisdoon.web.vertx.verticle;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

public abstract class AbstractWebVerticle extends AbstractMyVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebVerticle.class);

    int port;
    boolean sslEnable;
    String sslKeyStore;
    String sslPassword;
    String sslKeyType;
    String sslKeyPath;

    Router router;

    @Override
    public void start(Promise promise) {
        this.router = Router.router(this.vertx);
        logger.info("create router");

        this.start();
        //开启https
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        if (!new File(sslKeyStore).exists()) {
            logger.warn(String.format("sslKeyStore:%s is not exists!", sslKeyStore));
            this.sslEnable = false;
        } else if (sslEnable) {
            httpServerOptions.setSsl(true);
            switch (this.sslKeyType.toLowerCase()) {
                case "pem":
                    httpServerOptions.setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(sslKeyStore).setKeyPath(sslKeyPath));
                    break;
                case "jks":
                    httpServerOptions.setKeyCertOptions(new JksOptions().setPath(sslKeyStore).setPassword(sslPassword));
                    break;
                case "pfx":
                    httpServerOptions.setPfxKeyCertOptions(new PfxOptions().setPath(sslKeyStore).setPassword(sslPassword));
                    break;
                default:
                    SelfSignedCertificate certificate = SelfSignedCertificate.create();
                    httpServerOptions.setKeyCertOptions(certificate.keyCertOptions())
                            .setTrustOptions(certificate.trustOptions());
            }
        }
        this.vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .listen(port, httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        promise.complete();
                        logger.info("HTTP server started on {}://localhost:{}", sslEnable ? "https" : "http", port);
                    } else {
                        promise.fail(httpServerAsyncResult.cause());
                    }
                });

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getSslKeyStore() {
        return sslKeyStore;
    }

    public void setSslKeyStore(String sslKeyStore) {
        this.sslKeyStore = sslKeyStore;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }

    public String getSslKeyType() {
        return sslKeyType;
    }

    public void setSslKeyType(String sslKeyType) {
        this.sslKeyType = sslKeyType;
    }

    public String getSslKeyPath() {
        return sslKeyPath;
    }

    public void setSslKeyPath(String sslKeyPath) {
        this.sslKeyPath = sslKeyPath;
    }


    final public static class VertxRouterEntry extends Entry implements Register {
        Set<Method> routeMethod;
        Set<Class<?>> configBeans;
        private static String REGEX_PATH = "\\{([\\w\\-]+)\\.path\\.(\\w+)\\}(.*)";

        @Override
        public synchronized void scan(Class<?> aClass, Map<String, Entry> map) {
            /*ReflectionUtils.getMethods(aClass, ReflectionUtils.withAnnotation(VertxRouter.class))
                    .stream()*/
            Arrays.stream(this.getMethod(aClass, ReflectionUtils.withAnnotation(VertxRouter.class)))
                    .filter(method -> Arrays.stream(method.getParameterTypes())
                            .filter(bClass -> bClass == RoutingContextChain.class).findFirst().isPresent())
                    .forEach(method -> {
                        String key = this.key(aClass);
                        VertxRouterEntry routerEntry;
                        if (map.containsKey(key)) {
                            routerEntry = (VertxRouterEntry) map.get(key);
                            routerEntry.routeMethod.add(method);
                        } else {
                            routerEntry = new VertxRouterEntry();
                            routerEntry.ServiceClass = aClass;
                            routerEntry.routeMethod = new HashSet<>();
                            routerEntry.routeMethod.add(method);
                            map.put(key, routerEntry);
                        }
                    });
        }


        @Override
        synchronized void inject(Vertx vertx, AbstractMyVerticle verticle) {
            final Object serviceBean = ApplicationContextProvider.getBean(this.ServiceClass);
            if (verticle instanceof AbstractWebVerticle) {
                this.routeMethod.stream().filter(Objects::nonNull).sorted((o1, o2) ->
                        o1.getAnnotation(VertxRouter.class).order() > o2.getAnnotation(VertxRouter.class).order() ? 1 : -1
                ).forEach(routeMethod -> {
                    VertxRouter vertxRouter = routeMethod.getAnnotation(VertxRouter.class);
                    Route route = ((AbstractWebVerticle) verticle).router.route();
                    RoutingContextChain chain = new RoutingContextChain(route);

                    if (vertxRouter.method() != null && vertxRouter.method().length > 0) {

                        Arrays.stream(vertxRouter.method()).filter(httpMethodString -> {
                                    Optional<HttpMethod> optional = HttpMethod
                                            .values()
                                            .stream()
                                            .filter(httpMethod ->
                                                    httpMethod.name().equals(httpMethodString))
                                            .findFirst();
                                    boolean flag = optional.isPresent();
                                    if (flag) {
                                        route.method(optional.get());
                                    }
                                    return !flag;
                                }
                        ).forEach(s -> {
                            if (StringUtils.isEmpty(s)) return;
                            route.method(new HttpMethod(s));
                        });

                    }
                    VertxRoutePath routePath = routeMethod.getDeclaringClass().getAnnotation(VertxRoutePath.class);
                    String suffix = getRegexPath(vertxRouter.path()),
                            prefix = (routePath != null && !StringUtils.isEmpty(routePath.value()))
                                    ? getRegexPath(routePath.value())
                                    : "",
                            pathString = prefix + suffix;

                    /*String pathString = vertxRouter.path(), part0, part1, part2;
                    final String replaceFormat = "%s$3";
                    if (pathString.matches(REGEX_PATH)) {
                        part0 = pathString.replaceFirst(REGEX_PATH, "$1");
                        Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);
                        if (configBeans == null || configBeans.size() == 0)
                            configBeans = reflections.getTypesAnnotatedWith(ConfigurationProperties.class);
                        Class<AbstractWechatConfiguration> configClass =
                                (Class<AbstractWechatConfiguration>) configBeans.stream().filter(aClass -> {
                                    ConfigurationProperties properties = aClass.getAnnotation(ConfigurationProperties.class);
                                    return AbstractWechatConfiguration.class.isAssignableFrom(aClass)
                                            && (part0.equals(properties.value()) || part0.equals(properties.prefix()));
                                }).findFirst().get();
                        AbstractWechatConfiguration.Path path = AbstractWechatConfiguration.getConfig(configClass).getPath();
                        part1 = pathString.replaceFirst(REGEX_PATH, "$2");
                        part2 = path.path(part1);
                        pathString = (StringUtils.isEmpty(part2)
                                ? vertxRouter.path()
                                : pathString.replaceFirst(REGEX_PATH, String.format(replaceFormat, part2)));
                    }*/
                    switch (vertxRouter.mode()) {
                        case Path:
                            route.path(pathString);
                            break;
                        case PathRegex:
                            route.pathRegex(pathString = prefix.replaceAll("\\/", "\\\\/") + suffix);
                            break;
                        case VirtualHost:
                            route.virtualHost(pathString);
                            break;
                        default:
                            throw new RuntimeException(String.format("no support mode[%s]", vertxRouter.mode().toString()));
                    }
                    try {
                        Object[] paramaters = Arrays.stream(routeMethod.getParameterTypes()).map(aClass -> {
                            if (aClass == Vertx.class) {
                                return vertx;
                            } else if (aClass == RoutingContextChain.class) {
                                return chain;
                            }
                            return null;
                        }).toArray();
                        routeMethod.setAccessible(true);
                        if (paramaters.length > 0)
                            routeMethod.invoke(serviceBean, paramaters);
                        else {
                            route.handler(routingContext -> {
                                try {
                                    routeMethod.invoke(serviceBean, routingContext);
                                } catch (Throwable e) {
                                    logger.error(e.getMessage(), e);
                                    routingContext.response().setStatusCode(500).end(e.getMessage());
                                }
                            });
                        }


                        LoggerFactory.getLogger(routeMethod.getDeclaringClass()).info(String.format("%s[%s]", route.methods() == null ? "ALL" : route.methods(), pathString));
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        logger.error(e.getMessage(), e);
                        return;
                    }

                });
            }
        }

        String getRegexPath(final String routePath) {
            String pathString = routePath, part0, part1, part2;
            final String replaceFormat = "%s$3";
            if (pathString.matches(REGEX_PATH)) {
                part0 = pathString.replaceFirst(REGEX_PATH, "$1");
                part1 = pathString.replaceFirst(REGEX_PATH, "$2");
                /*Reflections reflections = ApplicationContextProvider.getBean(Reflections.class);
                if (configBeans == null || configBeans.size() == 0)
                    configBeans = reflections.getTypesAnnotatedWith(ConfigurationProperties.class);
                Class<AbstractWechatConfiguration> configClass =
                        (Class<AbstractWechatConfiguration>) configBeans.stream().filter(aClass -> {
                            ConfigurationProperties properties = aClass.getAnnotation(ConfigurationProperties.class);
                            return AbstractWechatConfiguration.class.isAssignableFrom(aClass)
                                    && (part0.equals(properties.value()) || part0.equals(properties.prefix()));
                        }).findFirst().get();

                AbstractWechatConfiguration.Path path = AbstractWechatConfiguration.getConfig(configClass).getPath();
                part2 = path.path(part1);*/

                Environment environment = ApplicationContextProvider.getBean(Environment.class);
                part2 = environment.getProperty(String.format("%s.path.%s", part0, part1));
                return (StringUtils.isEmpty(part2)
                        ? routePath
                        : pathString.replaceFirst(REGEX_PATH, String.format(replaceFormat, part2)));

            }
            return routePath;
        }
    }
}


