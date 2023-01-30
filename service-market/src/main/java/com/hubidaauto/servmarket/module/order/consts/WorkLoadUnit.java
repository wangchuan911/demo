package com.hubidaauto.servmarket.module.order.consts;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Classname CostTimeUnit
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/23 20:32
 */
public enum WorkLoadUnit {
    TIMES(11110L, "次"), HOURS(11111L, "小时"), SQUARE(11121L, "平方"), UKNOW(-1L, "未知");

    private Long id;
    private String desc;

    WorkLoadUnit(Long id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static WorkLoadUnit getInstance(Long id) {
        Optional<WorkLoadUnit> optional = Arrays.stream(values()).findFirst();
        return optional.orElseGet(() -> UKNOW);
    }

    public String getDesc() {
        return desc;
    }
}
