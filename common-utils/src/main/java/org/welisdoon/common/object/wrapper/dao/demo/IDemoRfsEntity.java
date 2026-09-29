package org.welisdoon.common.object.wrapper.dao.demo;

import org.welisdoon.common.object.wrapper.dao.annotation.Database;
import org.welisdoon.common.object.wrapper.dao.base.IEntity;

import java.math.BigDecimal;

/**
 * @Classname IDemoRfsEntity
 * @Description TODO
 * @Author Septem
 * @Date 11:06
 */
@Database.Extra(
        feature = @Database.Feature(alias = "rfs", express = "true=true"),
        parts = {})
@Database.Relation(target = IDemoRfsEntity.Rfs2Entity.class, rel = @Database.AttrToAttr(self = "id", target = "serviceId"), alias = "res", one2More = true)
public interface IDemoRfsEntity extends IDemoServiceEntity {

    @Database.Main(
            id = "id",
            feature = @Database.Feature(alias = "res", express = "true=true"),
            parts = {
                    @Database.Part(
                            name = "res.rr_service_entity t1",
                            attrs = {
                                    @Database.Attr(name = "id", alias = "id", desc = "字段1"),
                                    @Database.Attr(name = "service_id", alias = "serviceId", desc = "字段1"),
                                    @Database.Attr(name = "entity_id", alias = "entityId", desc = "字段1"),
                                    @Database.Attr(name = "comp_id", alias = "compId", desc = "字段2")
                            })
            })
    @Database.Relation2(
            alias = "entity",
            selfAttr = "entityId",
            rel = {
                    @Database.AttrToAttr2(targetAttr = "id", targetType = IDemoLinkEntity.class),
                    @Database.AttrToAttr2(targetAttr = "id", targetType = IDemoDeviceEntity.class)
            }
    )
    interface Rfs2Entity extends IEntity<BigDecimal> {

    }
}
