package com.team2.activity.mapper.typehandler;

import com.team2.activity.entity.enums.MailStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB에 한글 displayName으로 저장된 email_status 컬럼을 MailStatus enum으로 변환하는 TypeHandler.
 */
@MappedTypes(MailStatus.class)
public class MailStatusTypeHandler extends BaseTypeHandler<MailStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, MailStatus parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getDisplayName());
    }

    @Override
    public MailStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : MailStatus.from(value);
    }

    @Override
    public MailStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : MailStatus.from(value);
    }

    @Override
    public MailStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : MailStatus.from(value);
    }
}
