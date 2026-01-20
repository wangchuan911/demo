package org.welisdoon.common.object.wrapper.demo;

import org.welisdoon.common.object.wrapper.IDataAccessObject;

/**
 * @Classname DemoTypeAEntity
 * @Description TODO
 * @Author Septem
 * @Date 10:58
 */
@IDataAccessObject.DataFuture(name = "TypeB", filter = {})
@IDataAccessObject.Table(table = "user5.table5 t5_1", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_1.column1", property = "t5_1column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t5_1.column2", property = "t5_1column2"),
})
@IDataAccessObject.Table(table = "user2.table6 t5_2", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Strong, columns = {
        @IDataAccessObject.Column(column = "t5_2.column1", property = "t5_2column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t5_2.column2", property = "t5_2column2"),
})
@IDataAccessObject.Table(table = "user6.table7 t5_3", group = "t5", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_3.column1", property = "t5_3column1", linkColumn = "t1.column1"),
        @IDataAccessObject.Column(column = "t5_3.column2", property = "t5_3column2", type = IDataAccessObject.ColumnType.PrimaryKeu),
})
@IDataAccessObject.Table(table = "user7.table8 t5_2_1", group = "t5_2", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_2_1.column1", property = "t5_2_1column1", linkColumn = "t5_2.column1"),
        @IDataAccessObject.Column(column = "t5_2_1.column2", property = "t5_2_1column2", type = IDataAccessObject.ColumnType.PrimaryKeu),
})
@IDataAccessObject.Table(table = "user7.table9 t5_2_2", group = "t5_2", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t5_2_2.column1", property = "t5_2_2column1", linkColumn = "t5_2_1.column1"),
        @IDataAccessObject.Column(column = "t5_2_2.column2", property = "t5_2_2column2"),
})
@IDataAccessObject.Table(table = "user8.table10 t6_1", group = "t6", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t6_1.column1", property = "t6_1column1", linkColumn = "t1.column1"),
        @IDataAccessObject.Column(column = "t6_1.column2", property = "t6_1column2"),
})
@IDataAccessObject.Table(table = "user8.table11 t6_2", group = "t6", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t6_2.column1", property = "t6_2column1", linkColumn = "t6_1.column1"),
        @IDataAccessObject.Column(column = "t6_2.column2", property = "t6_2column2"),
})
@IDataAccessObject.Table(table = "user8.table12 t7", group = "t7", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t7.column1", property = "t7_2column1", linkColumn = "t5_1.column1"),
        @IDataAccessObject.Column(column = "t7.column2", property = "t7_2column2", linkColumn = "t5_2_1.column1"),
})
public interface IDemoTypeBEntity extends IDemoTypeAEntity {
}
