package org.welisdoon.common.object.wrapper.demo;

import org.welisdoon.common.object.wrapper.IDataAccessObject;

/**
 * @Classname DemoTypeAEntity
 * @Description TODO
 * @Author Septem
 * @Date 10:58
 */
@IDataAccessObject.DataFuture(name = "TypeBA", filter = {})
@IDataAccessObject.Table(table = "user1.table1 t1", datasource = "db1", columns = {
        @IDataAccessObject.Column(column = "t1.column1", property = "t1column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t1.column2", property = "t1column2"),
        @IDataAccessObject.Column(column = "t1.column3", property = "t1column3"),
        @IDataAccessObject.Column(column = "t1.column4", property = "t1column4"),
        @IDataAccessObject.Column(column = "t1.column5", property = "t1column5"),
        @IDataAccessObject.Column(column = "t1.column6", property = "t1column6")
})
@IDataAccessObject.Table(table = "user2.table2 t2", datasource = "db1", rel = IDataAccessObject.TableRel.Weak, columns = {
        @IDataAccessObject.Column(column = "t2.column1", property = "t2column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t2.column2", property = "t2column2"),
        @IDataAccessObject.Column(column = "t2.column3", property = "t2column3"),
        @IDataAccessObject.Column(column = "t2.column4", property = "t2column4"),
        @IDataAccessObject.Column(column = "t2.column5", property = "t2column5"),
        @IDataAccessObject.Column(column = "t2.column6", property = "t2column6")
})
@IDataAccessObject.Table(table = "user3.table3 t3", datasource = "db1", rel = IDataAccessObject.TableRel.Strong, columns = {
        @IDataAccessObject.Column(column = "t3.column1", property = "t3column1", linkColumn = "t1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t3.column2", property = "t3column2"),
        @IDataAccessObject.Column(column = "t3.column3", property = "t3column3"),
        @IDataAccessObject.Column(column = "t3.column4", property = "t3column4"),
        @IDataAccessObject.Column(column = "t3.column5", property = "t3column5"),
        @IDataAccessObject.Column(column = "t3.column6", property = "t3column6")
})
@IDataAccessObject.Table(table = "user4.table4 t4", group = "t4", datasource = "db1", rel = IDataAccessObject.TableRel.Multi, columns = {
        @IDataAccessObject.Column(column = "t4.column1", property = "node1.t4column1", linkColumn = "t3.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t4.column2", property = "node1.t4column2"),
        @IDataAccessObject.Column(column = "t4.column3", property = "node1.t4column3"),
        @IDataAccessObject.Column(column = "t4.column4", property = "node1.t4column4"),
        @IDataAccessObject.Column(column = "t4.column5", property = "node1.t4column5"),
        @IDataAccessObject.Column(column = "t4.column6", property = "node1.t4column6")
})
@IDataAccessObject.Table(table = "user4.table41 t4_1", group = "t4_1", datasource = "db1", rel = IDataAccessObject.TableRel.Multi, columns = {
        @IDataAccessObject.Column(column = "t4_1.column1", property = "node1.node2.t4_1column1", linkColumn = "t4.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t4_1.column2", property = "node1.node2.t4_1column2"),
        @IDataAccessObject.Column(column = "t4_1.column3", property = "node1.node2.t4_1column3"),
        @IDataAccessObject.Column(column = "t4_1.column4", property = "node1.node2.t4_1column4"),
        @IDataAccessObject.Column(column = "t4_1.column5", property = "node1.node2.t4_1column5"),
        @IDataAccessObject.Column(column = "t4_1.column6", property = "node1.node2.t4_1column6")
})
@IDataAccessObject.Table(table = "user4.table44 t4_2", group = "t4_1", datasource = "db1", rel = IDataAccessObject.TableRel.Strong, columns = {
        @IDataAccessObject.Column(column = "t4_2.column1", property = "node1.node2.t4_2column1", linkColumn = "t4_1.column1", type = IDataAccessObject.ColumnType.PrimaryKeu),
        @IDataAccessObject.Column(column = "t4_2.column2", property = "node1.node2.t4_2column2"),
        @IDataAccessObject.Column(column = "t4_2.column3", property = "node1.node2.t4_2column3"),
        @IDataAccessObject.Column(column = "t4_2.column4", property = "node1.node2.t4_2column4"),
        @IDataAccessObject.Column(column = "t4_2.column5", property = "node1.node2.t4_2column5"),
        @IDataAccessObject.Column(column = "t4_2.column6", property = "node1.node2.t4_2column6")
})


public interface IDemoTypeAEntity extends IDataAccessObject {

    @MixColumn(columns = @ColMapper(column = "t4_2.column1", target = "t7.column1"), single = true, dataType = String.class, source = IDemoTypeBEntity.class)
    String getValueA();
}
