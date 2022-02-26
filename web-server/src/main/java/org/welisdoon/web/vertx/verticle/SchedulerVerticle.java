package org.welisdoon.web.vertx.verticle;

import io.vertx.core.Future;
import io.vertx.core.TimeoutStream;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.client.WebClient;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.annotation.VertxCron;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

/**
 * @Classname SchedulerVerticle
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/8 15:10
 */
@Component("schedulerVerticle")
@ConfigurationProperties(prefix = "vertx.scheduler")
@Verticle
public class SchedulerVerticle extends AbstractMyVerticle {
    protected static final Logger logger = LoggerFactory.getLogger(SchedulerVerticle.class);

    static Map<Method, Long> tasks = new HashMap<>();


    public static class CronRegister extends Entry implements Register {
        Set<Method> methods;
        static Map<Class<?>, Object> params = new HashMap<>();

        @Override
        void inject(Vertx vertx, AbstractMyVerticle verticle) {
            if (verticle instanceof SchedulerVerticle)
                methods.stream().forEach(method -> {
                    VertxCron cron = method.getDeclaredAnnotation(VertxCron.class);
                    CronSequenceGenerator generator = new CronSequenceGenerator(cron.expression());
                    if (cron.immediate())
                        this.invoke(verticle, method);
                    timer(vertx, generator, method, verticle);
                });
        }

        protected void timer(final Vertx vertx, final CronSequenceGenerator generator, final Method method, final AbstractMyVerticle verticle) {
            vertx.timerStream(generator.next(new Date()).getTime() - System.currentTimeMillis())
                    .handler(event -> {
                        synchronized (methods) {
                            tasks.put(method, event);
                        }
                        this.invoke(verticle, method).onComplete(event1 -> {
                            timer(vertx, generator, method, verticle);
                        });
                    });
        }

        protected Future invoke(AbstractMyVerticle verticle, Method method) {
            Object[] params = Arrays.stream(method.getParameterTypes()).map(aClass -> {
                try {
                    if (aClass == Vertx.class) {
                        return verticle.getVertx();
                    } else if (WebClient.class == aClass) {
                        return ObjectUtils.getMapValueOrNewSafe(CronRegister.params, aClass, () -> WebClient.create(verticle.getVertx()));
                    } else if (EventBus.class == aClass) {
                        return ObjectUtils.getMapValueOrNewSafe(CronRegister.params, aClass, () -> verticle.getVertx().eventBus());
                    } else return null;
                } catch (Throwable e) {
                    logger.error(e.getMessage());
                    return null;
                }
            }).toArray();
            return verticle.getVertx().executeBlocking(promise -> {
                try {
                    Object o = method.invoke(ApplicationContextProvider.getBean(this.ServiceClass), params);
                    if (o instanceof Future) {
                        ((Future<?>) o).onComplete(promise::complete);
                    } else {
                        promise.complete();
                    }

                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    promise.fail(e);
                }
            });
        }

        @Override
        public void scan(Class<?> aClass, Map<String, Entry> map) {
            Arrays.stream(this.getMethod(aClass, ReflectionUtils.withAnnotation(VertxCron.class)))
                    .forEach(method -> {
                        CronRegister register;
                        String key = this.key();
                        if ((register = (CronRegister) map.get(key)) == null) {
                            register = new CronRegister();
                            register.methods = new HashSet<>();
                            map.put(this.key(), register);
                            register.ServiceClass = ApplicationContextProvider.getRealClass(aClass);
                        }
                        register.methods.add(method);
                    });
        }

    }


}
