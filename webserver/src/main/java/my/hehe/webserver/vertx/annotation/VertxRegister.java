package my.hehe.webserver.vertx.annotation;

import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VertxRegister {
    @AliasFor("VerticleClass")
    Class<? extends Verticle> value() default Verticle.class;

    @AliasFor("value")
    Class<? extends Verticle> verticleClass() default Verticle.class;

    ;


}
