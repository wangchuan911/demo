package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaProtoList;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.define.MirrorList;

import java.util.*;

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
    public List<Field> getFields() {
        return (List) getAttributes();
    }

    @Override
    public MetaLink getConstruct() {
        if (getConstructId() == null) {
            MetaLink link = new MetaLink().setObjectId(this.getId()).setParentId(0L).setTypeId(LinkMetaType.ObjToDataBase.getId());
            setConstruct(link);
            return link;
        }
        return super.getConstruct();
    }

    @JsonIgnore
    @JSONField(deserialize = false, serialize = false)
    public List<MetaLink> getConstructorLinks() {
        List<MetaLink> list = new LinkedList<>();
        Optional.ofNullable(this.getParentId()).ifPresent(aLong -> {
            MetaLink metaLink = new MetaLink();
            metaLink.setObjectId(aLong);
            metaLink.setId(-1 * this.getId());
            metaLink.setTypeId(LinkMetaType.ObjConstructor.getId());
            metaLink.setInstanceId(1L);
            list.add(metaLink);
        });
        getConstruct().getChildren().stream().filter(metaLink -> metaLink.getType().getParent() == LinkMetaType.SqlToJoin).forEach(list::add);

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
    public static class Field extends Attribute {
        //        MetaList<RowMapper> mappers;
        MetaLink foreignKey;

        @JsonIgnore
        @JSONField(deserialize = false, serialize = false)
        public MetaLink getForeignKey() {
            return ObjectUtils.synchronizedGet(this, field -> field.foreignKey, field ->
                    field.foreignKey = getParent().getChildren().stream().filter(metaLink -> metaLink.getType() == LinkMetaType.ForeignKey).findFirst().orElseGet(() -> {
                        return new MetaLink().<MetaLink>setTypeId(LinkMetaType.ForeignKey.getId());
                    })
            );
        }

        public void setColumn(MetaLink column) {
            ListIterator<MetaLink> iterator = getParent().getChildren().listIterator();
            while (iterator.hasNext()) {
                MetaLink metaLink = iterator.next();
                if (metaLink.getType() == LinkMetaType.SqlToSelect) {
                    if (changeColumn(metaLink, column)) {
                        iterator.set(column);
                    }
                    return;
                }
            }
            getParent().getChildren().add(column);
        }

        protected boolean changeColumn(MetaLink self, MetaLink create) {
            if (!self.compareValues(create)) {
                return true;
            }
            MetaProtoList<MetaLink> selfLinks = self.getChildren(), createLinks = create.getChildren();
            Assert.isTrue(CollectionUtils.isEmpty(self.getChildren()) && CollectionUtils.isNotEmpty(createLinks), "错误的数据");
            Assert.isTrue(CollectionUtils.isNotEmpty(self.getChildren()) && CollectionUtils.isEmpty(createLinks), "错误的数据");
            if (CollectionUtils.isNotEmpty(self.getChildren()) && CollectionUtils.isNotEmpty(createLinks) && changeColumn(selfLinks.getFirst(), createLinks.getFirst())) {
                self.getChildren().clear();
                self.getChildren().add(create);
            }
            return false;
        }

        @JsonIgnore
        @JSONField(deserialize = false, serialize = false)
        @Override
        public DataObject getObject() {
            return (DataObject) super.getObject();
        }


        /*public MetaList<RowMapper> getMappers() {
            return mappers;
        }

        public Field setMappers(MetaList<RowMapper> mappers) {
            this.mappers = mappers;
            return this;
        }

        public Field setMappers(MetaLink link) {
            return this;
        }

        public static class RowMapper {
            List<Row> rows;

            public static class Row {
                Long id;
                Long typeId;

                public void setTypeId(Long typeId) {
                    this.typeId = typeId;
                }

                public Long getId() {
                    return id;
                }

                public void setId(Long id) {
                    this.id = id;
                }
            }

            public List<Row> getRows() {
                return rows;
            }
        }*/
    }
}
