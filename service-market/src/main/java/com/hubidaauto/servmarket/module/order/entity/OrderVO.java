package com.hubidaauto.servmarket.module.order.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @Classname OrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
@DataObject
public class OrderVO {
    public OrderVO() {
    }

    public OrderVO(JsonObject jsonObject) {
        OrderVO condition = jsonObject.mapTo(this.getClass());
        Arrays.stream(this.getClass().getMethods()).filter(method -> {
            return method.getName().indexOf("set") == 0 && method.getParameterCount() == 1;
        }).forEach(method -> {
            char[] chars = method.getName().substring(3).toCharArray();
            String name = new String(chars), head;
            if (method.getParameterTypes()[0] == boolean.class || method.getParameterTypes()[0] == Boolean.class) {
                head = "is";
            } else {
                head = "get";
            }
            try {
                method.invoke(this, getClass().getMethod(String.format("%s%s", head, name)).invoke(condition));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
