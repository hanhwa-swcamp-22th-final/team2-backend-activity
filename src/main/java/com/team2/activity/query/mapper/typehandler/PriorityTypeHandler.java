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

    // enum 값을 PreparedStatement에 문자열로 세팅한다.
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Priority parameter, JdbcType jdbcType)
            throws SQLException {
        // enum의 displayName을 JDBC 문자열 파라미터로 넣는다.
        ps.setString(i, parameter.getDisplayName());
    }

    // 컬럼명 기준 조회 결과를 enum 값으로 변환한다.
    @Override
    public Priority getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 지정한 컬럼명에서 원본 문자열 값을 읽는다.
        String value = rs.getString(columnName);
        // 값이 있으면 enum으로 변환하고 없으면 null을 반환한다.
        return value == null ? null : Priority.from(value);
    }

    // 컬럼 인덱스 기준 조회 결과를 enum 값으로 변환한다.
    @Override
    public Priority getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 지정한 컬럼 인덱스에서 원본 문자열 값을 읽는다.
        String value = rs.getString(columnIndex);
        // 값이 있으면 enum으로 변환하고 없으면 null을 반환한다.
        return value == null ? null : Priority.from(value);
    }

    // 프로시저 호출 결과를 enum 값으로 변환한다.
    @Override
    public Priority getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 프로시저 결과의 지정 인덱스에서 원본 문자열 값을 읽는다.
        String value = cs.getString(columnIndex);
        // 값이 있으면 enum으로 변환하고 없으면 null을 반환한다.
        return value == null ? null : Priority.from(value);
    }
}
