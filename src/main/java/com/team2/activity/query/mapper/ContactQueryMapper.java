package com.team2.activity.query.mapper;

import com.team2.activity.entity.Contact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// 연락처 조회용 SQL을 연결하는 MyBatis mapper다.
@Mapper
public interface ContactQueryMapper {

    // 연락처 ID로 단건을 조회한다.
    Contact findById(Long contactId);

    // 전체 연락처 목록을 조회한다.
    List<Contact> findAll();

    // 거래처 ID로 연락처 목록을 조회한다.
    List<Contact> findAllByClientId(Long clientId);
}
