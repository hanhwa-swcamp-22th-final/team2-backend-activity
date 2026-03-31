package com.team2.activity.mapper;

import com.team2.activity.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmailLogQueryMapper {

    EmailLog findById(Long emailLogId);

    List<EmailLog> findAll();

    List<EmailLog> findByClientId(Long clientId);

    List<EmailLog> findByEmailStatus(String emailStatus);
}
