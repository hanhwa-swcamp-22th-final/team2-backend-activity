package com.team2.activity.query.mapper.typehandler;

import com.team2.activity.entity.enums.DocumentType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DB에 displayName으로 저장된 email_doc_type 컬럼을 DocumentType enum으로 변환하는 TypeHandler.
 */
@MappedTypes(DocumentType.class)
public class DocumentTypeHandler extends BaseTypeHandler<DocumentType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, DocumentType parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.getDisplayName());
    }

    @Override
    public DocumentType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : DocumentType.from(value);
    }

    @Override
    public DocumentType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : DocumentType.from(value);
    }

    @Override
    public DocumentType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : DocumentType.from(value);
    }
}
