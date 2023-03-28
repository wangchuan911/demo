package org.welisdoon.model.query.entity.input;

import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.query.annotations.Model;
import org.welisdoon.model.query.consts.QueryObjectType;
import org.welisdoon.model.query.entity.AbstractInputEntity;

/**
 * @Classname InputEntity
 * @Description TODO
 * @Author Septem
 * @Date 16:29
 */
public class InputEntity extends AbstractInputEntity {
    ColumnEntity[] column;
}
