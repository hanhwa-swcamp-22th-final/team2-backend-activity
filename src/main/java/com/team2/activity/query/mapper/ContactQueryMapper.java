package com.team2.activity.query.mapper;

import com.team2.activity.command.domain.entity.Contact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 연락처 조회용 SQL을 연결하는 MyBatis mapper다.
@Mapper
public interface ContactQueryMapper {

    // 연락처 ID로 단건을 조회한다.
    Contact findContactById(Long contactId);

    // 전체 연락처 목록을 조회한다.
    List<Contact> findAllContacts();

    // 거래처 ID로 연락처 목록을 조회한다.
    List<Contact> findAllContactsByClientId(Long clientId);

    // 필터 조건으로 연락처를 페이징 조회한다.
    List<Contact> findContactsWithFilters(@Param("clientId") Long clientId,
                                          @Param("keyword") String keyword,
                                          @Param("limit") int limit,
                                          @Param("offset") int offset);

    // 필터 조건에 해당하는 연락처 수를 반환한다.
    long countContactsWithFilters(@Param("clientId") Long clientId,
                                  @Param("keyword") String keyword);
}
