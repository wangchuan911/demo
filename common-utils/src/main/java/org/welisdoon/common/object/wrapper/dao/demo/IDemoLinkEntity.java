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
        feature = @Database.Feature(alias = "link", express = {}),
        parts = {
                @Database.Part(
                        name = "res.cm_link t1",
                        attrs = {
                                @Database.Attr(name = "id", alias = "id", desc = "字段1"),
                                @Database.Attr(name = "code", alias = "code", desc = "字段2")
                        }),
                @Database.Part(
                        name = "res.pe_link t2",
                        attrs = {
                                @Database.Attr(name = "link_id", alias = "linkId", desc = "字段1", relAttr = "id"),
                                @Database.Attr(name = "parent_seg", alias = "parentSeg", desc = "字段1")
                        },
                        required = false)
        })
@Database.Relation(
        target = IDemoLinkEntity.Link2Parent.class,
        rel = @Database.AttrToAttr(self = "id", target = "currentId"),
        alias = "link2Parent", one2More = true)
@Database.Relation(
        target = IDemoLinkEntity.Link2Child.class,
        rel = @Database.AttrToAttr(self = "id", target = "currentId"),
        alias = "link2Children", one2More = true)

public interface IDemoLinkEntity extends IEntity {

    default IDemoServiceEntity getValueA() {
        throw new IllegalCallerException();
    }

    @Database.Main(
            feature = @Database.Feature(alias = "rel3", express = {}),
            id = "id",
            parts = {
                    @Database.Part(
                            name = "res.cr_link_link t1",
                            attrs = {
                                    @Database.Attr(name = "id", alias = "id", desc = "字段1"),
                                    @Database.Attr(name = "upper_link_id", alias = "linkId", desc = "字段1"),
                                    @Database.Attr(name = "lower_link_id", alias = "currentId", desc = "字段2"),
                                    @Database.Attr(name = "seq", alias = "seq", desc = "字段3"),
                            })
            }
    )
    @Database.Relation(alias = "link", target = IDemoLinkEntity.class, rel = @Database.AttrToAttr(self = "linkId", target = "id"))
    interface Link2Parent extends IEntity<BigDecimal> {

    }

    @Database.Main(
            feature = @Database.Feature(alias = "rel3", express = {}),
            id = "id",
            parts = {
                    @Database.Part(
                            name = "res.cr_link_link t1",
                            attrs = {
                                    @Database.Attr(name = "id", alias = "id", desc = "字段1"),
                                    @Database.Attr(name = "upper_link_id", alias = "currentId", desc = "字段1"),
                                    @Database.Attr(name = "lower_link_id", alias = "linkId", desc = "字段2"),
                                    @Database.Attr(name = "seq", alias = "seq", desc = "字段3"),
                            })
            }
    )
    @Database.Relation(alias = "link", target = IDemoLinkEntity.class, rel = @Database.AttrToAttr(self = "linkId", target = "id"))
    interface Link2Child extends IEntity<BigDecimal> {

    }
}
