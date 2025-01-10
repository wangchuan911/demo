package org.welisdoon.metadata.prototype.dao;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.welisdoon.metadata.prototype.consts.IMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MetaTypeHandler implements TypeHandler<String> {

    @Override
    public void setParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {

    }

    @Override
    public String getResult(ResultSet rs, String columnName) throws SQLException {
        return getName(MetaUtils.getInstance().getMetaType(rs.getLong(columnName)));
    }

    @Override
    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
        return getName(MetaUtils.getInstance().getMetaType(rs.getLong(columnIndex)));
    }

    @Override
    public String getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }

    public String getName(IMetaType iMetaType) {
        if (iMetaType instanceof Enum) {
            return ((Enum<?>) iMetaType).name();
        }
        return iMetaType.getClass().getName();
    }
}
