package com.team2.activity.query.mapper;

import com.team2.activity.command.domain.entity.EmailLog;
import com.team2.activity.command.domain.entity.enums.MailStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EmailLogQueryMapper {

    EmailLog findById(Long emailLogId);

    List<EmailLog> findAll();

    List<EmailLog> findByClientId(Long clientId);

    List<EmailLog> findByEmailStatus(MailStatus emailStatus);

    List<EmailLog> findWithFilters(@Param("clientId") Long clientId,
                                    @Param("poId") String poId,
                                    @Param("emailStatus") MailStatus emailStatus,
                                    @Param("emailSenderId") Long emailSenderId,
                                    @Param("keyword") String keyword,
                                    @Param("dateFrom") LocalDateTime dateFrom,
                                    @Param("dateTo") LocalDateTime dateTo,
                                    @Param("limit") int limit,
                                    @Param("offset") int offset);

    long countWithFilters(@Param("clientId") Long clientId,
                           @Param("poId") String poId,
                           @Param("emailStatus") MailStatus emailStatus,
                           @Param("emailSenderId") Long emailSenderId,
                           @Param("keyword") String keyword,
                           @Param("dateFrom") LocalDateTime dateFrom,
                           @Param("dateTo") LocalDateTime dateTo);
}
