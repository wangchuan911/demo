package org.welisdoon.common.object.wrapper;

/**
 * @Classname DemoTypeAEntity
 * @Description TODO
 * @Author Septem
 * @Date 10:58
 */
@IDataAccessObject.DataFuture(name = "TypeB", filter = {})
@IDataAccessObject.Table(table = "user5.table5 t5_1", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_1.column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t5_1.column2"),
})
@IDataAccessObject.Table(table = "user2.table2 t5_2", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Strong, columns = {
        @IDataAccessObject.Column(column = "t5_2.column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t5_2.column2"),
})
@IDataAccessObject.Table(table = "user6.table6 t5_3", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_3.column1", linkColumn = "t1.column1"),
        @IDataAccessObject.Column(column = "t5_3.column2", type = IDataAccessObject.ColumnType.PrimaryKeu),
})
@IDataAccessObject.Table(table = "user7.table7 t5_4_1", group = "t5_4", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_4_1.column1", linkColumn = "t5_2.column1"),
        @IDataAccessObject.Column(column = "t5_4_1.column2", type = IDataAccessObject.ColumnType.PrimaryKeu),
})
@IDataAccessObject.Table(table = "user7.table7 t5_4_2", group = "t5_4", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_4_2.column1", linkColumn = "t5_4_1.column1"),
        @IDataAccessObject.Column(column = "t5_4_2.column2"),
})

public interface IDemoTypeBEntity  extends IDemoTypeAEntity {
}
