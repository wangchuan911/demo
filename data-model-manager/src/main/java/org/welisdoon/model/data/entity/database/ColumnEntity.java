package org.welisdoon.model.data.entity.database;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.TypeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.service.DataBaseService;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

@Model(DataModelType.Column)
public class ColumnEntity extends AbstractDataEntity implements IForeignTarget, IColumnValue {
    String length;
    @JsonIgnore
    @JSONField(serialize = false)
    TableEntity table;
    Long tableId;
    JdbcType type = JdbcType.UNDEFINED;
    ForeignEntity foreign;
    Object value, formatValue;

    public TableEntity getTable() {
        return table;
    }

    public ColumnEntity setTable(TableEntity table) {
        this.table = table;
        return this;
    }

    public ColumnEntity setType(JdbcType type) {
        this.type = type;
        return this;
    }

    public JdbcType getType() {
        return type;
    }


    public void setLength(String length) {
        this.length = length;
    }

    public String getLength() {
        return length;
    }


    public ForeignEntity getForeign() {
        return foreign;
    }

    public void setForeign(ForeignEntity foreign) {
        this.foreign = foreign;
    }

    public boolean isForeign() {
        return this.foreign != null;
    }

    @Override
    public TableEntity foreignTable() {
        IForeignTarget iForeignTarget = getForeign().getTarget();
        if (iForeignTarget instanceof ColumnEntity) {
            if (((ColumnEntity) iForeignTarget).getValue() == null) {
                ((ColumnEntity) iForeignTarget).setValue(TableResultUtils.queryForObject(this.getTable().getPrimary().getValue(), Object.class, (ColumnEntity) iForeignTarget));
            }
            Object typeId = ((ColumnEntity) iForeignTarget).getValue();

            if ((iForeignTarget = ((ColumnEntity) iForeignTarget).getForeign().getTarget()) instanceof ColumnEntity) {
                return ((ColumnEntity) iForeignTarget).setValue(TableResultUtils.queryForObject(typeId, Object.class, (ColumnEntity) iForeignTarget)).foreignTable();
            }
            if (iForeignTarget instanceof DataObjectEntity) {
                iForeignTarget = ApplicationContextProvider.getBean(DataBaseService.class).getDataObject(TypeUtils.castToLong(typeId)).getTable();
            }
            if (iForeignTarget instanceof TableEntity) {
                return (TableEntity) iForeignTarget;
            }
        }
        if (iForeignTarget == null)
            return null;
        return iForeignTarget.foreignTable();
    }

    public Object getValue() {
        return value;
    }

    public ColumnEntity setValue(Object value) {
        this.value = value;
        return this;
    }

    public Object getFormatValue() {
        return formatValue;
    }

    public ColumnEntity setFormatValue(Object formatValue) {
        this.formatValue = formatValue;
        return this;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }
}
