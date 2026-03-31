package com.team2.activity.mapper;

import com.team2.activity.entity.Contact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContactQueryMapper {

    Contact findById(Long contactId);

    List<Contact> findAll();

    List<Contact> findAllByClientId(Long clientId);
}
