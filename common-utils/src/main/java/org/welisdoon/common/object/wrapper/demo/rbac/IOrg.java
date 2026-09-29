package org.welisdoon.common.object.wrapper.demo.rbac;

import org.welisdoon.common.object.wrapper.IDataAccessObject;

/**
 * @Classname RBACObj
 * @Description TODO
 * @Author Septem
 * @Date 17:09
 */
@IDataAccessObject.DataFuture(name = "RBAC-ORG", filter = {@IDataAccessObject.FutureExpress(property = "typeId", values = "int:102")})
@IDataAccessObject.Table(table = "ORG_OBJECT t2", datasource = "db1",
        columns = {
                @IDataAccessObject.Column(column = "t2.org_id", property = "t5_1column1", linkColumn = "t1.ID", type = IDataAccessObject.ColumnType.PrimaryKey),
                @IDataAccessObject.Column(column = "t2.parent_org_id", property = "parentId")}
)
public interface IOrg extends IRBAC {
    default String getOrgId() {
        Object o = getValue("parentId");
        return o == null ? null : o.toString();
    }
}
