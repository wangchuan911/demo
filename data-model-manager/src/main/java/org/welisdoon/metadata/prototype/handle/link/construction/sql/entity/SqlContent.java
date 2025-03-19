package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.define.MetaObject;
import org.welisdoon.metadata.prototype.entity.DataObject;

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
        String from = ((SqlJoiner) getChildren().get(0)).formatFirst();
        StringBuilder join = new StringBuilder();
        if (getChildren().size() > 1) {
            ListIterator<SqlJoiner> iterator = (ListIterator) getChildren().listIterator(1);
            SqlJoiner sqlJoiner;
            while (iterator.hasNext()) {
                sqlJoiner = iterator.next();
                join.append(sqlJoiner.format());
            }
        }
        return from.replace(SqlJoiner.OTHER, join.toString());
    }
}
