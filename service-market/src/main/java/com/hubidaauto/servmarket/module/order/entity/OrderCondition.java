package com.hubidaauto.servmarket.module.order.entity;

import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * @Classname OrderVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/13 12:14
 */
public class OrderCondition extends AbstractBaseCondition<Long, OrderVO> {
    int type;

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }


}
