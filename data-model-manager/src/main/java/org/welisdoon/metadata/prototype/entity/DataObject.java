package org.welisdoon.metadata.prototype.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.metadata.prototype.consts.AttributeMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaProtoList;
import org.welisdoon.metadata.prototype.define.MetaObject;

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
        //        MetaList<RowMapper> mappers;
        MetaLink foreignKey;
        MetaProtoList<MetaLink> columnLinks;

        public MetaProtoList<MetaLink> getColumnLinks() {
            return ObjectUtils.synchronizedGet(this, field -> field.columnLinks, field -> {
                MetaLink parent = getParent();
                this.columnLinks = new MetaProtoList<>();
                if (parent != null) {
                    this.columnLinks.add(parent);
                    MetaLink node;
                    while ((node = parent.getChildren().stream().filter(metaLink -> metaLink.getType() == LinkMetaType.SqlToSelect).findFirst().orElse(null)) != null) {
                        this.columnLinks.add(node);
                        parent = node;
                    }
                }
                return this.columnLinks;
            });
        }

        public MetaLink getForeignKey() {
            return ObjectUtils.synchronizedGet(this, field -> field.foreignKey, field ->
                    field.foreignKey = getParent().getChildren().stream().filter(metaLink -> metaLink.getType() == LinkMetaType.ForeignKey).findFirst().orElse(null)
            );
        }

        public void setColumnLinks(MetaProtoList<MetaLink> columnLinks) {
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

        public MetaProtoList getMappers() {

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
