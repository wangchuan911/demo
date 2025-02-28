package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.IMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaList;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.define.MetaPrototype;

import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

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
    public static class Field extends Attribute {
        MetaList<RowMapper> mappers;
        MetaList<MetaLink> columnLinks;

        public MetaList<MetaLink> getColumnLinks() {
            return ObjectUtils.synchronizedGet(this, field -> field.columnLinks, field -> {
                MetaLink parent = getParent();
                this.columnLinks = new MetaList<>();
                if (parent != null) {
                    this.columnLinks.add(parent);
                    while (CollectionUtils.isNotEmpty(parent.getChildren())) {
                        parent = parent.getChildren().get(0);
                        this.columnLinks.add(parent);
                    }
                }
                return this.columnLinks;
            });
        }

        public void setColumnLinks(MetaList<MetaLink> columnLinks) {
            ListIterator<MetaLink> current = getColumnLinks().listIterator();
            ListIterator<MetaLink> newLink = columnLinks.listIterator();
            MetaLink eNode, cNode;
            while (current.hasNext()) {
                eNode = current.next();
                if (newLink.hasNext()) {
                    cNode = newLink.next();
                    if (!eNode.compareValues(cNode)) {
                        while (current.hasNext()) {
                            current.next().remove();
                        }
                        while (newLink.hasNext()) {
                            current.add(newLink.next());
                        }
                        MetaLink node, pre = null;
                        for (int i = 0; i < this.columnLinks.size(); i++) {
                            node = this.columnLinks.get(i);
                            if (i != 0) {
                                pre.getChildren().clear();
                                pre.getChildren().add(node);
                            } else {
                                setParent(node);
                            }
                            pre = node;
                        }
                        return;
                    }
                }
            }
            throw new IllegalStateException("错误的数据");
        }


        @Override
        public DataObject getObject() {
            return (DataObject) super.getObject();
        }

        public MetaList<RowMapper> getMappers() {
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
                RowType type;
                Long typeId;

                public void setTypeId(Long typeId) {
                    this.typeId = typeId;
                    type = Arrays.stream(RowType.values()).filter(rowType -> rowType.getId() == typeId).findFirst().orElseThrow(() -> new IllegalStateException("未知属性"));
                }

                public RowType getType() {
                    return type;
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

            public static class AttrRow extends Row {
                Long objectId;
                Long attrId;

                public Long getObjectId() {
                    return objectId;
                }

                public Long getAttrId() {
                    return attrId;
                }

                public Long getTypeId() {
                    return typeId;
                }

                public void setObjectId(Long objectId) {
                    this.objectId = objectId;
                }

                public void setAttrId(Long attrId) {
                    this.attrId = attrId;
                }
            }

            public static class KeyRow extends AttrRow {

            }

            public static class MapperRow extends Row {

            }

            public enum RowType implements IMetaType {
                Key(2100, "主要关联项", KeyRow.class), Attr(2101, "属性关联", AttrRow.class), Condition(2102, "条件关联", MapperRow.class);
                long id;
                String name;
                Class<? extends Row> rowType;

                RowType(long id, String name, Class<? extends Row> rowType) {
                    this.id = id;
                    this.name = name;
                    this.rowType = rowType;
                }

                @Override
                public long getId() {
                    return id;
                }

                @Override
                public String getDesc() {
                    return name;
                }
            }
        }
    }
}
