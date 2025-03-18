package org.welisdoon.metadata.prototype.dao;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.welisdoon.metadata.prototype.consts.IMetaType;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.consts.MetaUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MetaLinkTypeHandler implements TypeHandler<String> {

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
        if (iMetaType instanceof LinkMetaType) {
            LinkMetaType type = ((LinkMetaType) iMetaType);
            if (this.matched(type, LinkMetaType.Value, 0)) {
                type = LinkMetaType.Value;
            } else if (this.matched(type, LinkMetaType.Values, 0)) {
                type = LinkMetaType.Values;
            } else if (this.matched(type, LinkMetaType.SqlToSelect, 0)) {
                type = LinkMetaType.SqlToSelect;
            } else if (this.matched(type, LinkMetaType.SqlToJoinOfMultiDataRel, 0)) {
                type = LinkMetaType.SqlToJoinOfMultiDataRel;
            } else if (this.matched(type, LinkMetaType.SqlOperator, 1)) {
                type = LinkMetaType.SqlOperator;
            } else if (this.matched(type, LinkMetaType.SqlToJoin, 1)) {
                type = LinkMetaType.SqlToJoin;
            }
            return type.name();
        }
        throw new IllegalStateException("不知持的type");
    }

    boolean matched(final LinkMetaType type, final LinkMetaType target, final int deep) {
        LinkMetaType type1 = type;
        for (int i = deep; i >= 0; i--) {
            if (type1 == target) {
                return true;
            }
            if (type1 == null) {
                break;
            }
            type1 = type1.getParent();
        }
        return false;
    }
}
