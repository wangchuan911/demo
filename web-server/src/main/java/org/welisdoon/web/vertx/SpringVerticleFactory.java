package org.welisdoon.web.vertx;

import io.vertx.core.Deployable;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.concurrent.Callable;

/**
 * A {@link VerticleFactory} backed by Spring's {@link ApplicationContext}. It allows to implement verticles as Spring
 * beans and thus benefit from dependency injection, ...etc.
 *
 * @author Thomas Segismont
 */
//@Component("verticleFactory")
@Deprecated
public class SpringVerticleFactory implements VerticleFactory {

    @Override
    public String prefix() {
        // Just an arbitrary string which must uniquely identify the verticle factory
        return "myapp";
    }

    @Override
    public void createVerticle2(String verticleName, ClassLoader classLoader, Promise<Callable<? extends Deployable>> promise) {
        String clazz = VerticleFactory.removePrefix(verticleName);
        try {
            Verticle verticle;
            verticle = (Verticle) ApplicationContextProvider.getBean(Class.forName(clazz));
            verticle.hashCode();
            promise.complete(() -> verticle);
        } catch (Throwable e) {
            promise.fail(e);
        }
    }
}