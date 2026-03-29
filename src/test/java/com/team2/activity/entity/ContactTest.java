package com.team2.activity.entity; // 테스트 대상 클래스와 같은 패키지

import org.junit.jupiter.api.DisplayName; // 테스트 이름 표시 어노테이션
import org.junit.jupiter.api.Test;         // 개별 테스트 메서드 표시 어노테이션

import static org.assertj.core.api.Assertions.assertThat; // AssertJ 검증 메서드 정적 import

@DisplayName("Contact 엔티티 테스트") // 테스트 클래스 전체의 표시 이름
class ContactTest {

    // ── 공통 픽스처 ────────────────────────────────────────────
    // 여러 테스트에서 재사용할 기본 Contact 객체 생성 헬퍼
    private Contact buildBasicContact() {
        return Contact.builder()
                .clientId(1L)               // 거래처 ID (마스터 서비스의 client PK 참조, 필수)
                .name("김철수")              // 연락처 담당자 이름 (필수)
                .position("Team Leader")    // 직위 (선택)
                .email("kim@example.com")   // 이메일 주소 (선택, DDL NULL 허용)
                .tel("010-1234-5678")        // 전화번호 (선택)
                .build();
    }

    // ── 테스트 1: 기본 생성 ────────────────────────────────────
    @Test
    @DisplayName("기본 Contact 생성 성공")
    void createContact_basic() {
        Contact contact = buildBasicContact(); // 헬퍼로 기본 Contact 생성

        assertThat(contact.getClientId()).isEqualTo(1L);               // 거래처 ID 확인
        assertThat(contact.getName()).isEqualTo("김철수");              // 이름 확인
        assertThat(contact.getPosition()).isEqualTo("Team Leader");    // 직위 확인
        assertThat(contact.getEmail()).isEqualTo("kim@example.com");   // 이메일 확인
        assertThat(contact.getTel()).isEqualTo("010-1234-5678");        // 전화번호 확인
    }

    // ── 테스트 2: 선택 필드 없이 생성 ─────────────────────────
    @Test
    @DisplayName("선택 필드 없이 Contact 생성")
    void createContact_withoutOptionalFields() {
        Contact contact = Contact.builder()
                .clientId(2L)
                .name("이영수")              // 이름만 필수 - position, email, tel 모두 선택 필드
                .build();

        assertThat(contact.getPosition()).isNull(); // 직위 미입력 → null
        assertThat(contact.getEmail()).isNull();    // 이메일 미입력 → null (DDL: NULL 허용)
        assertThat(contact.getTel()).isNull();      // 전화번호 미입력 → null
    }

    // ── 테스트 3: update() - 전체 수정 가능 필드 변경 ─────────
    @Test
    @DisplayName("Contact 수정 - 모든 수정 가능 필드 변경")
    void updateContact_allFields() {
        Contact contact = buildBasicContact(); // 기존 연락처 (김철수)

        // update() 메서드 호출: 이름, 직위, 이메일, 전화번호 변경
        contact.update(
                "박영희",                          // 이름 변경
                "Team Member",                     // 직위 변경
                "park@example.com",               // 이메일 변경
                "010-9999-8888"                    // 전화번호 변경
        );

        assertThat(contact.getName()).isEqualTo("박영희");                         // 이름 변경 확인
        assertThat(contact.getPosition()).isEqualTo("Team Member");               // 직위 변경 확인
        assertThat(contact.getEmail()).isEqualTo("park@example.com");             // 이메일 변경 확인
        assertThat(contact.getTel()).isEqualTo("010-9999-8888");                   // 전화번호 변경 확인
    }

    // ── 테스트 4: 불변 필드 검증 ──────────────────────────────
    @Test
    @DisplayName("clientId는 수정 불가")
    void clientId_isImmutable() {
        Contact contact = buildBasicContact(); // clientId=1

        // update()에 clientId 파라미터가 없으므로 변경 불가
        contact.update("다른이름", null, "other@example.com", null);

        assertThat(contact.getClientId()).isEqualTo(1L); // 거래처 ID 불변 확인
    }

    // ── 테스트 5: update() - 기존 값을 null로 클리어 ────────────
    @Test
    @DisplayName("Contact 수정 - 선택 필드 제거 (null로 클리어)")
    void updateContact_clearOptionalFields() {
        Contact contact = buildBasicContact(); // position="Team Leader", tel="010-1234-5678"

        // update()로 선택 필드를 null로 전달하여 기존 값 제거
        contact.update(
                "김철수",          // 이름 유지
                null,              // 직위 제거
                "kim@example.com", // 이메일 유지
                null               // 전화번호 제거
        );

        assertThat(contact.getPosition()).isNull(); // 직위 null로 클리어 확인
        assertThat(contact.getTel()).isNull();       // 전화번호 null로 클리어 확인
    }

    // ── 테스트 6: Team Member 직위 ─────────────────────────────
    @Test
    @DisplayName("Team Member 직위 설정")
    void createContact_teamMember() {
        Contact contact = Contact.builder()
                .clientId(1L)
                .name("신입직원")
                .position("Team Member") // 직위 = 팀원 (Team Leader와 함께 프론트에서 사용하는 두 값 중 하나)
                .build();

        assertThat(contact.getPosition()).isEqualTo("Team Member"); // 직위 저장 확인
    }
}
