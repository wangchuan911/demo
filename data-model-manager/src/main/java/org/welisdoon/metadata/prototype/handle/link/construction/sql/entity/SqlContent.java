package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.text.MessageFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 15:57
 */
public class SqlContent extends Sql {
    MetaObject metaObject;

    public SqlContent(MetaObject metaObject) {
        this.metaObject = metaObject;
        this.build();
    }


    @Override
    protected void build() {
        readonly();
        if (metaObject instanceof DataObject) {
            for (MetaLink constructorLink : ((DataObject) metaObject).getConstructorLinks()) {
                if (constructorLink instanceof Sql) {
                    constructorLink.setParent(this);
                    ((SqlJoiner) constructorLink).build();
                    getChildren().add(constructorLink);
                }
            }
        }
    }

    public String format(Format format) {
        String from = ((SqlJoiner) getChildren().get(0)).formatFirst(format);
        StringBuilder join = new StringBuilder();
        if (getChildren().size() > 1) {
            ListIterator<SqlJoiner> iterator = (ListIterator) getChildren().listIterator(1);
            SqlJoiner sqlJoiner;
            while (iterator.hasNext()) {
                sqlJoiner = iterator.next();
                join.append(sqlJoiner.format(format));
            }
        }
        String column = findInputs().stream().map(field -> {
            Sql last = SqlItem.format(field);
            return MessageFormat.format("{0}.{1}", last.getPrefix(), last.getAttribute().getCode());
        }).collect(Collectors.joining(","));
        return MessageFormat.format("select {0} {1}", column, from.replace(SqlJoiner.OTHER, join.toString()));
    }

    protected List<DataObject.Field> findInputs() {
        return ((DataObject) this.metaObject).getFields().stream().filter(field -> field.getParent().getChildren().stream().anyMatch(child -> child.getType() == LinkMetaType.SqlToSelect)).collect(Collectors.toList());
    }
}
