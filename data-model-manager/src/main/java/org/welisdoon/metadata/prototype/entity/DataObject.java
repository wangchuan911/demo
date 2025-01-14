package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.metadata.prototype.condition.MetaLinkCondition;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;
import org.welisdoon.metadata.prototype.dao.MetaObjectDao;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Classname DataObject
 * @Description TODO
 * @Author Septem
 * @Date 14:49
 */
public class DataObject extends MetaObject {
    /*MetaLink[] constructorLinks;*/

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public Field[] getFields() {
        return Arrays.stream(getAttributes()).filter(attribute -> attribute instanceof Field).toArray(Field[]::new);
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<MetaLink> getConstructorLinks() {
        List<MetaLink> list = new LinkedList<>();
        Optional.ofNullable(getConstruct()).ifPresent(construct -> {
            Optional.ofNullable(this.getParentId()).ifPresent(aLong -> {
                MetaLink metaLink = new MetaLink();
                metaLink.setObjectId(aLong);
                metaLink.setId(-1 * this.getId());
                metaLink.setTypeId(LinkMetaType.ObjConstructor.getId());
                metaLink.setInstanceId(1L);
                list.add(metaLink);
            });
            construct.getChildren().stream().filter(metaLink -> metaLink.getType().getParent() == LinkMetaType.SqlToJoin).forEach(list::add);
        });
        return list;
        /*return Optional.ofNullable(constructorLinks).orElseGet(() -> {
            MetaLinkDao metaLinkDao = MetaUtils.getInstance().getMetaLinkDao();
            List<MetaLink> list = new LinkedList<>();
            Optional.ofNullable(this.getParentId()).ifPresent(aLong -> {
                MetaLink metaLink = new MetaLink();
                metaLink.setObjectId(aLong);
                metaLink.setId(-1 * this.getId());
                metaLink.setTypeId(LinkMetaType.ObjConstructor.getId());
                metaLink.setInstanceId(1L);
                list.add(metaLink);
            });

            MetaLinkCondition condition = new MetaLinkCondition();
            condition.setData(new MetaLink());
            condition.getData().setObjectId(this.getId());
            condition.getData().setTypeId(LinkMetaType.ObjConstructor.getId());
            metaLinkDao.list(condition).stream().flatMap(metaLink -> {
                return LinkMetaType.getChildTypeId(LinkMetaType.ObjConstructor.getId()).stream().flatMap(aLong -> {
                    return LinkMetaType.getChildTypeId(aLong).stream();
                }).flatMap(aLong -> {
                    MetaLinkCondition condition1 = new MetaLinkCondition();
                    condition1.setData(new MetaLink());
                    condition1.setParentId(metaLink.getId());
                    condition1.getData().setTypeId(aLong);
                    return metaLinkDao.list(condition1).stream();
                });
            }).forEach(list::add);
            constructorLinks = list.toArray(new MetaLink[0]);
            return constructorLinks;
        });*/
    }

    @AttributeMetaType.MetaType(AttributeMetaType.Field)
    public static class Field extends Attribute<DataObject> {
        DataBaseTable.Column[] columns;

        public DataBaseTable.Column[] getColumns() {
            return columns;
        }

        public void setColumns(DataBaseTable.Column[] columns) {
            this.columns = columns;
        }
    }
}
