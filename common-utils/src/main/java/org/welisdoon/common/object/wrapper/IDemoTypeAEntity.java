package org.welisdoon.common.object.wrapper;

/**
 * @Classname DemoTypeAEntity
 * @Description TODO
 * @Author Septem
 * @Date 10:58
 */
@IDataAccessObject.DataFuture(name = "TypeBA", filter = {})
@IDataAccessObject.Table(table = "user1.table1 t1", datasource = "db1", columns = {
        @IDataAccessObject.Column(column = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t1.column2"),
        @IDataAccessObject.Column(column = "t1.column3"),
        @IDataAccessObject.Column(column = "t1.column4"),
        @IDataAccessObject.Column(column = "t1.column5"),
        @IDataAccessObject.Column(column = "t1.column6")
})
@IDataAccessObject.Table(table = "user2.table2 t2", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t2.column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t2.column2"),
        @IDataAccessObject.Column(column = "t2.column3"),
        @IDataAccessObject.Column(column = "t2.column4"),
        @IDataAccessObject.Column(column = "t2.column5"),
        @IDataAccessObject.Column(column = "t2.column6")
})
@IDataAccessObject.Table(table = "user3.table3 t3", datasource = "db1", rel = IDataAccessObject.TableRel.Strong, columns = {
        @IDataAccessObject.Column(column = "t3.column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t3.column2"),
        @IDataAccessObject.Column(column = "t3.column3"),
        @IDataAccessObject.Column(column = "t3.column4"),
        @IDataAccessObject.Column(column = "t3.column5"),
        @IDataAccessObject.Column(column = "t3.column6")
})
@IDataAccessObject.Table(table = "user4.table4 t4", datasource = "db1", rel = IDataAccessObject.TableRel.Multi, columns = {
        @IDataAccessObject.Column(column = "t4.column1", linkColumn = "t3.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t4.column2"),
        @IDataAccessObject.Column(column = "t4.column3"),
        @IDataAccessObject.Column(column = "t4.column4"),
        @IDataAccessObject.Column(column = "t4.column5"),
        @IDataAccessObject.Column(column = "t4.column6")
})

public interface IDemoTypeAEntity extends IDataAccessObject {
}
