package com.hubidaauto.servmarket.module.scheduler.verticle;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.Verticle;
import org.welisdoon.web.vertx.verticle.AbstractCustomVerticle;

/**
 * @Classname SchedulerVerticle
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/8 15:10
 */
@Component("schedulerVerticle")
@Verticle
public class SchedulerVerticle extends AbstractCustomVerticle {
}
