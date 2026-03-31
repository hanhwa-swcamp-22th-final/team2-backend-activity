package com.team2.activity.query.mapper.typehandler;

import com.team2.activity.entity.enums.Priority;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB에 한글 displayName으로 저장된 activity_priority 컬럼을 Priority enum으로 변환하는 TypeHandler.
 */
@MappedTypes(Priority.class)
public class PriorityTypeHandler extends BaseTypeHandler<Priority> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Priority parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getDisplayName());
    }

    @Override
    public Priority getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : Priority.from(value);
    }

    @Override
    public Priority getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : Priority.from(value);
    }

    @Override
    public Priority getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : Priority.from(value);
    }
}
