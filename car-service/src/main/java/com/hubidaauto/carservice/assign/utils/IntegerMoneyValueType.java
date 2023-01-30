package com.hubidaauto.carservice.assign.utils;

import org.springframework.stereotype.Component;
import org.welisdoon.web.common.annotation.EntitySpecialType;

import java.math.BigDecimal;

/**
 * @Classname IntegerMoneyValueType
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2020/11/19 19:56
 */
@Component
public class IntegerMoneyValueType implements EntitySpecialType {
    @Override
    public Object getValue(Object value) {
        if (value instanceof Integer || value instanceof Long) {
            BigDecimal bigDecimal = new BigDecimal(value.toString());
            value = bigDecimal.divide(new BigDecimal("100"));
        }
        return String.format("%s元", value.toString());
    }
}
