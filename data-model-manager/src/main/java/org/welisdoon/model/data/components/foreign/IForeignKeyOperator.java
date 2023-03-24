package org.welisdoon.model.data.components.foreign;

import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Classname IForeignKey
 * @Description TODO
 * @Author Septem
 * @Date 16:06
 */
public interface IForeignKeyOperator<T extends IForeignTarget> {

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ForeignKey {
        long typeId();

        Class<? extends IForeignKeyOperator> operator();
    }

    IForeignTarget getTarget(ForeignEntity entity);

    TableEntity getTable(T t);
}
