package org.welisdoon.web.vertx;

import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * A {@link VerticleFactory} backed by Spring's {@link ApplicationContext}. It allows to implement verticles as Spring
 * beans and thus benefit from dependency injection, ...etc.
 *
 * @author Thomas Segismont
 */
@Component("verticleFactory")
public class SpringVerticleFactory implements VerticleFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /*@Override
    public boolean blockingCreate() {
        // Usually verticle instantiation is fast but since our verticles are Spring Beans,
        // they might depend on other beans/resources which are slow to build/lookup.
        return true;
    }
*/
    @Override
    public String prefix() {
        // Just an arbitrary string which must uniquely identify the verticle factory
        return "myapp";
    }

    public String naming(String name) {
        return String.format("%s:%s", prefix(), name);
    }

    @Override
    public void createVerticle(String verticleName, ClassLoader classLoader, Promise<Callable<Verticle>> var3) {
        // Our convention in this example is to give the class name as verticle name
        String clazz = VerticleFactory.removePrefix(verticleName);
        var3.complete(() -> (Verticle) applicationContext.getBean(Class.forName(clazz)));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}