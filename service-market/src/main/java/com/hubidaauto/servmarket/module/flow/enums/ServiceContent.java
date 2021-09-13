package com.hubidaauto.servmarket.module.flow.enums;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @Classname ServiceKind
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/13 20:43
 */
public final class ServiceContent {
    /*INNER_CAR_WASH(-100L),OUT_CAR_WASH(-101L),

    HOME_CLEAN(-110L),HOME_EQP_CLEAN(-111L);*/
    final private static Set<ServiceContent> UNIQUE = new HashSet<>();
    final Long id;
    final String code, desc;

    public ServiceContent(long ID, String CODE, String DESC) {
        this.id = ID;
        this.code = CODE;
        this.desc = DESC;
        if (UNIQUE.contains(this)) {
            throw new RuntimeException("repeat ServiceContent!");
        }
        UNIQUE.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceContent that = (ServiceContent) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    public final static ServiceContent getInstance(Long id) {
        Optional<ServiceContent> optional = UNIQUE.stream().filter(serviceContent -> serviceContent.id.equals(id)).findFirst();
        return optional.isPresent() ? optional.get() : null;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public final static ServiceContent
            INNER_CAR_WASH = new ServiceContent(-100L, "INNER_CAR_WASH", "车内清洗"),
            OUT_CAR_WASH = new ServiceContent(-101L, "OUT_CAR_WASH", "车外清洗"),
            HOME_CLEAN = new ServiceContent(-110L, "HOME_CLEAN", "房屋清洁"),
            HOME_EQP_CLEAN = new ServiceContent(-111L, "HOME_EQP_CLEAN", "电器清洁");
}
