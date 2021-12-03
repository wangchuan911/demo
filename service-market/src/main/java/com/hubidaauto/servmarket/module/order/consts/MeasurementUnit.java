package com.hubidaauto.servmarket.module.order.consts;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Classname CostTimeUnit
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/23 20:32
 */
public enum MeasurementUnit {
    TIMES(11110L, "次"), HOURS(11111L, "小时"), UKNOW(-1L, "未知");

    private Long id;
    private String desc;

    MeasurementUnit(Long id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static MeasurementUnit getInstance(Long id) {
        Optional<MeasurementUnit> optional = Arrays.stream(values()).findFirst();
        return optional.orElseGet(() -> UKNOW);
    }
}
