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

// Contact 조회 mapper가 DB 레코드를 읽기 모델로 정확히 매핑하는지 검증한다.
@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/sql/query-schema.sql")
@Transactional
@DisplayName("ContactQueryRepository 테스트")
class ContactQueryRepositoryTest {

    // 연락처 읽기 쿼리를 수행하는 실제 mapper다.
    @Autowired
    private ContactQueryMapper contactQueryMapper;

    // 테스트용 연락처 데이터를 직접 저장하기 위한 JDBC 도구다.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 연락처 레코드를 저장하고 mapper 상세 조회 결과를 반환한다.
    private Contact saveContact(Long clientId, String name) {
        // contacts 테이블에 테스트용 연락처를 직접 삽입한다.
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

        // 최근 저장된 연락처 PK를 조회한다.
        Long contactId = jdbcTemplate.queryForObject("SELECT MAX(contact_id) FROM contacts", Long.class);
        // mapper 상세 조회 결과를 재사용할 수 있도록 반환한다.
        return contactQueryMapper.findById(contactId);
    }

    @Test
    @DisplayName("ID로 연락처 조회 시 읽기 모델 필드가 매핑된다")
    void findById_mapsContactFields() {
        // 조회 대상 연락처를 DB에 저장한다.
        Contact saved = saveContact(1L, "kim");

        // mapper 단건 조회 결과를 가져온다.
        Contact found = contactQueryMapper.findById(saved.getContactId());

        // 기본 연락처 필드가 정확히 매핑되는지 확인한다.
        assertThat(found).isNotNull();
        assertThat(found.getContactId()).isEqualTo(saved.getContactId());
        assertThat(found.getClientId()).isEqualTo(1L);
        assertThat(found.getContactName()).isEqualTo("kim");
        assertThat(found.getContactEmail()).isEqualTo("kim@example.com");
    }

    @Test
    @DisplayName("거래처 ID로 연락처 목록을 조회한다")
    void findAllByClientId_returnsOnlyMatchedContacts() {
        // 여러 거래처의 연락처를 함께 저장한다.
        saveContact(1L, "kim");
        saveContact(1L, "lee");
        saveContact(2L, "park");

        // 특정 거래처의 연락처 목록을 조회한다.
        List<Contact> result = contactQueryMapper.findAllByClientId(1L);

        // 요청한 거래처의 연락처만 반환되는지 확인한다.
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Contact::getClientId)
                .containsOnly(1L);
    }
}
