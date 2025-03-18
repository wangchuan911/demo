package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @Classname SqlContent
 * @Description TODO
 * @Author Septem
 * @Date 15:57
 */
public class SqlContent extends MetaLink {

    public SqlContent(MetaObject metaObject) {
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

    public String format() {
        StringBuilder sql = new StringBuilder();
        if (getChildren().size() > 1) {
            ListIterator<SqlJoiner> iterator = (ListIterator) getChildren().listIterator(1);
            SqlJoiner sqlJoiner;
            while (iterator.hasNext()) {
                sqlJoiner = iterator.next();
                sql.append(sqlJoiner.format());
            }
        }
        String first = ((SqlJoiner) getChildren().get(0)).format();
        int offset1 = first.indexOf(" join ") + 6;
        int offset2 = first.indexOf(" on ");
        sql.insert(0, first.substring(offset1, offset2));
        sql.insert(0, " from ");
        sql.append(first.substring(offset2 + 4));
        return sql.toString();
    }
}
