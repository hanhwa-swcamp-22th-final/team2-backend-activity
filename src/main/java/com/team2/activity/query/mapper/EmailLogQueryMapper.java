package com.team2.activity.query.mapper;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// 이메일 로그 조회용 SQL을 연결하는 MyBatis mapper다.
@Mapper
public interface EmailLogQueryMapper {

    // 이메일 로그 ID로 단건을 조회한다.
    EmailLog findById(Long emailLogId);

    // 전체 이메일 로그 목록을 조회한다.
    List<EmailLog> findAll();

    // 거래처 ID로 이메일 로그 목록을 조회한다.
    List<EmailLog> findByClientId(Long clientId);

    // 상태 값으로 이메일 로그 목록을 조회한다.
    List<EmailLog> findByEmailStatus(MailStatus emailStatus);
}
