package org.welisdoon.common.object.wrapper.dao.demo;

import org.welisdoon.common.object.wrapper.dao.annotation.Database;
import org.welisdoon.common.object.wrapper.dao.base.IEntity;

import java.math.BigDecimal;

/**
 * @Classname DemoTypeAEntity
 * @Description TODO
 * @Author Septem
 * @Date 10:58
 */
@Database.Main(
        id = "id",
        feature = @Database.Feature(alias = "service", express = {}),
        parts = {
                @Database.Part(
                        name = "res.cm_device t1",
                        attrs = {
                                @Database.Attr(name = "id", alias = "id", desc = "字段1"),
                                @Database.Attr(name = "code", alias = "code", desc = "字段2")
                        }),
        })

public interface IDemoDeviceEntity extends IEntity<BigDecimal> {


}
