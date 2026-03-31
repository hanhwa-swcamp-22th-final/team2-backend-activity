package com.team2.activity.query.repository;

import com.team2.activity.entity.Contact;
import com.team2.activity.query.mapper.ContactQueryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("ContactQueryRepository 테스트")
class ContactQueryRepositoryTest {

    @Autowired
    private ContactQueryMapper contactQueryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Contact saveContact(Long clientId, String name) {
        jdbcTemplate.update(
                """
                INSERT INTO contacts (
                    client_id, writer_id, contact_name, contact_position,
                    contact_email, contact_tel, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                clientId,
                10L,
                name,
                "과장",
                name + "@example.com",
                "010-0000-0000",
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0)),
                Timestamp.valueOf(LocalDateTime.of(2025, 4, 1, 9, 0))
        );

        Long contactId = jdbcTemplate.queryForObject("SELECT MAX(contact_id) FROM contacts", Long.class);
        return contactQueryMapper.findById(contactId);
    }

    @Test
    @DisplayName("ID로 연락처 조회 시 읽기 모델 필드가 매핑된다")
    void findById_mapsContactFields() {
        Contact saved = saveContact(1L, "kim");

        Contact found = contactQueryMapper.findById(saved.getContactId());

        assertThat(found).isNotNull();
        assertThat(found.getContactId()).isEqualTo(saved.getContactId());
        assertThat(found.getClientId()).isEqualTo(1L);
        assertThat(found.getContactName()).isEqualTo("kim");
        assertThat(found.getContactEmail()).isEqualTo("kim@example.com");
    }

    @Test
    @DisplayName("거래처 ID로 연락처 목록을 조회한다")
    void findAllByClientId_returnsOnlyMatchedContacts() {
        saveContact(1L, "kim");
        saveContact(1L, "lee");
        saveContact(2L, "park");

        List<Contact> result = contactQueryMapper.findAllByClientId(1L);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Contact::getClientId)
                .containsOnly(1L);
    }
}
