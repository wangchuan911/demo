package org.welisdoon.common.object.wrapper.demo.rbac;

import org.welisdoon.common.object.wrapper.IDataAccessObject;

/**
 * @Classname RBACObj
 * @Description TODO
 * @Author Septem
 * @Date 17:09
 */
@IDataAccessObject.DataFuture(name = "RBAC-Base", filter = {})
@IDataAccessObject.Table(table = "RBAC_OBJECT t1", datasource = "db1",
        columns = {@IDataAccessObject.Column(column = "t1.ID", property = "id", type = IDataAccessObject.ColumnType.PrimaryKey),
                @IDataAccessObject.Column(column = "t1.NAME", property = "name"),
                @IDataAccessObject.Column(column = "t1.CODE", property = "code"),
                @IDataAccessObject.Column(column = "t1.TYPE_ID", property = "typeId")}
)
public interface IRBAC extends IDataAccessObject {

    default String getId() {
        Object o = getValue("id");
        return o == null ? null : o.toString();
    }

    default String getName() {
        Object o = getValue("name");
        return o == null ? null : o.toString();
    }

    default String getCode() {
        Object o = getValue("code");
        return o == null ? null : o.toString();
    }

    default String getTypeId() {
        Object o = getValue("typeId");
        return o == null ? null : o.toString();
    }
}
