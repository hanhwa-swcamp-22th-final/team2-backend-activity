package com.team2.activity.query.mapper;

import com.team2.activity.entity.EmailLog;
import com.team2.activity.entity.enums.MailStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmailLogQueryMapper {

    EmailLog findById(Long emailLogId);

    List<EmailLog> findAll();

    List<EmailLog> findByClientId(Long clientId);

    List<EmailLog> findByEmailStatus(MailStatus emailStatus);
}
