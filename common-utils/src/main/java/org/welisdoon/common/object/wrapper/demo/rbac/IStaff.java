package org.welisdoon.common.object.wrapper.demo.rbac;

import org.welisdoon.common.object.wrapper.IDataAccessObject;

/**
 * @Classname RBACObj
 * @Description TODO
 * @Author Septem
 * @Date 17:09
 */
@IDataAccessObject.DataFuture(name = "RBAC-STAFF", filter = @IDataAccessObject.FutureExpress(property = "typeId", values = "int:101"))
@IDataAccessObject.Table(table = "STAFF t2", datasource = "db1",
        columns = {@IDataAccessObject.Column(column = "t2.STAFF_ID", property = "", linkColumn = "t1.id", type = IDataAccessObject.ColumnType.PrimaryKey),
                @IDataAccessObject.Column(column = "t2.ORG_ID", property = "orgId")}
)
public interface IStaff extends IRBAC {

    default String getOrgId() {
        Object o = getValue("orgId");
        return o == null ? null : o.toString();
    }
}
