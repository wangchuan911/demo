package org.welisdoon.model.data.entity.database;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.util.TypeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.ibatis.type.JdbcType;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;

@Model(DataModelType.Column)
public class ColumnEntity extends AbstractDataEntity implements IForeignAssign, IColumnValue {
    String length;
    @JsonIgnore
    @JSONField(serialize = false)
    TableEntity table;
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
        IForeignAssign iForeignAssign = getForeign().getAssign();
        if (iForeignAssign instanceof ColumnEntity) {
            if (((ColumnEntity) iForeignAssign).getValue() == null) {
                ((ColumnEntity) iForeignAssign).setValue(TableResultUtils.queryForObject(this.getTable().getPrimary().getValue(), Object.class, (ColumnEntity) iForeignAssign));
            }
            Object typeId = ((ColumnEntity) iForeignAssign).getValue();

            if ((iForeignAssign = ((ColumnEntity) iForeignAssign).getForeign().getAssign()) instanceof ColumnEntity) {
                return ((ColumnEntity) iForeignAssign).setValue(TableResultUtils.queryForObject(typeId, Object.class, (ColumnEntity) iForeignAssign)).foreignTable();
            }
            if (iForeignAssign instanceof DataObjectEntity) {
                iForeignAssign = ApplicationContextProvider.getBean(DataObjectDao.class).get(TypeUtils.castToLong(typeId)).getTable();
            }
            if (iForeignAssign instanceof TableEntity) {
                return (TableEntity) iForeignAssign;
            }
        }
        return iForeignAssign.foreignTable();
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


}
