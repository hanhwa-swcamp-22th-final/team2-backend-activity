package com.team2.activity.entity; // 테스트 대상 클래스와 같은 패키지

import com.team2.activity.entity.enums.DocumentType; // 문서 유형 열거형 (PI, CI, PL, 생산지시서, 출하지시서)
import com.team2.activity.entity.enums.MailStatus;   // 메일 발송 상태 열거형 (SENT, FAILED)
import org.junit.jupiter.api.DisplayName;             // 테스트 이름 표시 어노테이션
import org.junit.jupiter.api.Test;                    // 개별 테스트 메서드 표시 어노테이션

import java.time.LocalDateTime; // 날짜+시간 타입 - 발송일시 저장에 사용
import java.util.List;          // 문서 유형, 첨부파일 목록 저장에 사용

import static org.assertj.core.api.Assertions.assertThat;           // AssertJ 검증 메서드 정적 import
import static org.assertj.core.api.Assertions.assertThatThrownBy; // 예외 검증 메서드 정적 import

@DisplayName("MailHistory 엔티티 테스트") // 테스트 클래스 전체의 표시 이름
class MailHistoryTest {

    // ── 공통 픽스처 ────────────────────────────────────────────
    // 여러 테스트에서 재사용할 기본 MailHistory 객체 생성 헬퍼
    private MailHistory buildBasicMailHistory() {
        return MailHistory.builder()
                .clientId(1L)                                           // 거래처 ID (마스터 서비스의 client PK 참조)
                .title("[PI] PI-2025-001 발송")                         // 메일 제목
                .recipientEmail("buyer@example.com")                    // 수신자 이메일 주소
                .recipientName("John Doe")                              // 수신자 이름 (선택)
                .senderId(10L)                                          // 발송자 사용자 ID (auth 서비스의 user PK)
                .status(MailStatus.SENT)                                // 발송 상태 = 발송 완료
                .sentAt(LocalDateTime.of(2025, 4, 10, 9, 0))           // 발송 일시
                .poId("PO-001")                                         // 연결된 수주건 ID (선택)
                .documentTypes(List.of(DocumentType.PI, DocumentType.CI)) // 포함된 문서 유형 목록
                .attachmentFileNames(List.of("PI001.pdf", "CI001.pdf")) // 첨부 파일명 목록
                .build();
    }

    // ── 테스트 1: 기본 생성 ────────────────────────────────────
    @Test
    @DisplayName("기본 MailHistory 생성 성공")
    void createMailHistory_basic() {
        MailHistory mail = buildBasicMailHistory(); // 헬퍼로 기본 MailHistory 생성

        assertThat(mail.getClientId()).isEqualTo(1L);                                              // 거래처 ID 확인
        assertThat(mail.getTitle()).isEqualTo("[PI] PI-2025-001 발송");                            // 제목 확인
        assertThat(mail.getRecipientEmail()).isEqualTo("buyer@example.com");                       // 수신자 이메일 확인
        assertThat(mail.getRecipientName()).isEqualTo("John Doe");                                 // 수신자 이름 확인
        assertThat(mail.getSenderId()).isEqualTo(10L);                                             // 발송자 ID 확인
        assertThat(mail.getStatus()).isEqualTo(MailStatus.SENT);                                   // 상태 확인
        assertThat(mail.getSentAt()).isEqualTo(LocalDateTime.of(2025, 4, 10, 9, 0));              // 발송 일시 확인
        assertThat(mail.getPoId()).isEqualTo("PO-001");                                            // PO ID 확인
        assertThat(mail.getDocumentTypes()).containsExactly(DocumentType.PI, DocumentType.CI);     // 문서 유형 목록 확인
        assertThat(mail.getAttachmentFileNames()).containsExactly("PI001.pdf", "CI001.pdf");       // 첨부파일 목록 확인
    }

    // ── 테스트 2: status 기본값 ────────────────────────────────
    @Test
    @DisplayName("status null 전달 시 기본값 SENT 적용")
    void createMailHistory_defaultStatus_whenNull() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("테스트 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .sentAt(LocalDateTime.now())           // 현재 일시
                // status 미설정 → 엔티티 생성자에서 null이면 SENT 로 초기화해야 함
                .build();

        assertThat(mail.getStatus()).isEqualTo(MailStatus.SENT); // 기본값 SENT 확인
    }

    // ── 테스트 3: FAILED 상태 명시 생성 ───────────────────────
    @Test
    @DisplayName("FAILED 상태로 MailHistory 생성")
    void createMailHistory_failedStatus() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("실패 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .status(MailStatus.FAILED)             // 발송 실패 상태로 명시 생성
                .sentAt(LocalDateTime.now())           // 현재 일시
                .build();

        assertThat(mail.getStatus()).isEqualTo(MailStatus.FAILED); // FAILED 상태 확인
    }

    // ── 테스트 4: FAILED 상태 + sentAt null ────────────────────
    @Test
    @DisplayName("FAILED 상태 시 sentAt null 허용")
    void createMailHistory_failedStatus_sentAtNull() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("발송 실패 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .status(MailStatus.FAILED)             // 발송 실패 상태
                // sentAt 미설정 → 발송 실패 시 발송 시각 없음 (DDL: email_sent_at TIMESTAMP NULL)
                .build();

        assertThat(mail.getStatus()).isEqualTo(MailStatus.FAILED); // FAILED 상태 확인
        assertThat(mail.getSentAt()).isNull();                      // 발송 시각 null 확인
    }

    // ── 테스트 5: updateStatus() - SENT → FAILED ───────────────
    @Test
    @DisplayName("updateStatus - SENT에서 FAILED로 변경")
    void updateStatus_sentToFailed() {
        MailHistory mail = buildBasicMailHistory(); // 기본 상태 SENT

        mail.updateStatus(MailStatus.FAILED); // 상태를 FAILED로 변경

        assertThat(mail.getStatus()).isEqualTo(MailStatus.FAILED); // FAILED로 변경됐는지 확인
    }

    // ── 테스트 6: updateStatus() - FAILED → SENT (재전송) ──────
    @Test
    @DisplayName("updateStatus - FAILED에서 SENT로 변경")
    void updateStatus_failedToSent() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("재발송 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .status(MailStatus.FAILED)             // 재발송 전 실패 상태
                .sentAt(LocalDateTime.now())           // 현재 일시
                .build();

        mail.updateStatus(MailStatus.SENT); // 재발송 성공 → SENT로 상태 변경

        assertThat(mail.getStatus()).isEqualTo(MailStatus.SENT); // SENT로 변경됐는지 확인
    }

    // ── 테스트 7: null 리스트 → 빈 리스트 초기화 ──────────────
    @Test
    @DisplayName("documentTypes null 전달 시 빈 리스트로 초기화")
    void createMailHistory_nullDocumentTypes() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("테스트")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .sentAt(LocalDateTime.now())           // 현재 일시
                .documentTypes(null)       // null로 전달 → NPE 방지를 위해 빈 리스트로 초기화해야 함
                .attachmentFileNames(null) // null로 전달 → 마찬가지로 빈 리스트로 초기화해야 함
                .build();

        assertThat(mail.getDocumentTypes()).isNotNull().isEmpty();      // null이 아닌 빈 리스트 확인
        assertThat(mail.getAttachmentFileNames()).isNotNull().isEmpty(); // null이 아닌 빈 리스트 확인
    }

    // ── 테스트 8: 복수 문서 유형 저장 ─────────────────────────
    @Test
    @DisplayName("복수 문서 유형 및 첨부파일 저장")
    void createMailHistory_multipleDocumentTypes() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("복합 문서 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .sentAt(LocalDateTime.now())           // 현재 일시
                .documentTypes(List.of(                                     // 5가지 문서 유형 모두 포함 가능
                        DocumentType.PI,              // PI (선적전검사의뢰서)
                        DocumentType.CI,              // CI (상업송장)
                        DocumentType.PL))             // PL (포장명세서)
                .attachmentFileNames(List.of("PI001.pdf", "CI001.pdf", "PL001.pdf")) // 3개 첨부파일
                .build();

        assertThat(mail.getDocumentTypes()).hasSize(3)
                .contains(DocumentType.PI, DocumentType.CI, DocumentType.PL); // 3개 유형 저장 확인
        assertThat(mail.getAttachmentFileNames()).hasSize(3);                   // 3개 파일명 저장 확인
    }

    // ── 테스트 9: 한글 문서 유형 저장 ─────────────────────────
    @Test
    @DisplayName("한글 문서 유형(생산지시서, 출하지시서) 저장")
    void createMailHistory_koreanDocumentTypes() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("내부 지시서 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .sentAt(LocalDateTime.now())           // 현재 일시
                .documentTypes(List.of(
                        DocumentType.PRODUCTION_ORDER, // 생산지시서
                        DocumentType.SHIPPING_ORDER))  // 출하지시서
                .build();

        assertThat(mail.getDocumentTypes())
                .contains(DocumentType.PRODUCTION_ORDER, DocumentType.SHIPPING_ORDER); // 한글 유형 저장 확인
    }

    // ── 테스트 10: MailStatus 열거값 목록 확인 ─────────────────
    @Test
    @DisplayName("MailStatus 열거값 확인")
    void mailStatus_values() {
        // 시스템에서 사용하는 2가지 메일 상태가 모두 정의되어 있는지 검증
        assertThat(MailStatus.values())
                .containsExactlyInAnyOrder(
                        MailStatus.SENT,   // 발송 완료
                        MailStatus.FAILED  // 발송 실패
                );
    }

    // ── 테스트 11: MailStatus JSON 직렬화 ──────────────────────
    @Test
    @DisplayName("MailStatus JSON 직렬화 - 한글 displayName 반환")
    void mailStatus_displayName() {
        // @JsonValue 가 붙은 getDisplayName()이 JSON 응답에 한글 문자열을 반환하는지 확인
        assertThat(MailStatus.SENT.getDisplayName()).isEqualTo("발송");    // 발송 완료 표시값
        assertThat(MailStatus.FAILED.getDisplayName()).isEqualTo("실패"); // 발송 실패 표시값
    }

    // ── 테스트 12: MailStatus JSON 역직렬화 ────────────────────
    @Test
    @DisplayName("MailStatus JSON 역직렬화 - 한글 문자열로 생성")
    void mailStatus_fromDisplayName() {
        // 프론트에서 "발송"/"실패" 문자열로 전송 시 @JsonCreator 로 enum 상수로 변환
        assertThat(MailStatus.from("발송")).isEqualTo(MailStatus.SENT);    // "발송" → SENT
        assertThat(MailStatus.from("실패")).isEqualTo(MailStatus.FAILED);  // "실패" → FAILED
    }

    // ── 테스트 13: DocumentType 열거값 목록 확인 ───────────────
    @Test
    @DisplayName("DocumentType 열거값 확인")
    void documentType_values() {
        // DDL: ENUM('PI','CI','PL','생산지시서','출하지시서') - 5가지 문서 유형 모두 정의되어야 함
        assertThat(DocumentType.values())
                .containsExactlyInAnyOrder(
                        DocumentType.PI,               // 선적전검사의뢰서
                        DocumentType.CI,               // 상업송장
                        DocumentType.PL,               // 포장명세서
                        DocumentType.PRODUCTION_ORDER, // 생산지시서
                        DocumentType.SHIPPING_ORDER    // 출하지시서
                );
    }

    // ── 테스트 14: DocumentType JSON 직렬화 ────────────────────
    @Test
    @DisplayName("DocumentType JSON 직렬화 - displayName 반환")
    void documentType_displayName() {
        // @JsonValue 가 붙은 getDisplayName()이 JSON 응답에 올바른 값을 반환하는지 확인
        assertThat(DocumentType.PI.getDisplayName()).isEqualTo("PI");                 // 영문 그대로
        assertThat(DocumentType.CI.getDisplayName()).isEqualTo("CI");                 // 영문 그대로
        assertThat(DocumentType.PL.getDisplayName()).isEqualTo("PL");                 // 영문 그대로
        assertThat(DocumentType.PRODUCTION_ORDER.getDisplayName()).isEqualTo("생산지시서"); // 한글
        assertThat(DocumentType.SHIPPING_ORDER.getDisplayName()).isEqualTo("출하지시서");   // 한글
    }

    // ── 테스트 15: DocumentType JSON 역직렬화 ──────────────────
    @Test
    @DisplayName("DocumentType JSON 역직렬화 - 문자열로 생성")
    void documentType_fromDisplayName() {
        // 프론트에서 문자열로 전송한 값을 @JsonCreator 로 enum 상수로 변환하는지 확인
        assertThat(DocumentType.from("PI")).isEqualTo(DocumentType.PI);
        assertThat(DocumentType.from("CI")).isEqualTo(DocumentType.CI);
        assertThat(DocumentType.from("PL")).isEqualTo(DocumentType.PL);
        assertThat(DocumentType.from("생산지시서")).isEqualTo(DocumentType.PRODUCTION_ORDER);
        assertThat(DocumentType.from("출하지시서")).isEqualTo(DocumentType.SHIPPING_ORDER);
    }

    // ── 테스트 16: PO 없는 메일 이력 ──────────────────────────
    @Test
    @DisplayName("PO 없는 MailHistory 생성")
    void createMailHistory_withoutPoId() {
        MailHistory mail = MailHistory.builder()
                .clientId(1L)                          // 거래처 ID
                .title("PO 없는 메일")
                .recipientEmail("test@example.com")
                .senderId(10L)                         // 발송자 사용자 ID
                .sentAt(LocalDateTime.now())           // 현재 일시
                // poId 미설정 → 특정 수주건과 무관한 메일
                .build();

        assertThat(mail.getPoId()).isNull(); // PO ID 없음 확인
    }

    // ── 테스트 17: MailStatus.from() 잘못된 값 예외 ──────────────
    @Test
    @DisplayName("MailStatus.from() - 잘못된 값 전달 시 예외 발생")
    void mailStatus_fromInvalidValue_throwsException() {
        // from() 내부 for 루프의 "일치하는 값 없음" 브랜치 커버 → JaCoCo 브랜치 100% 달성
        assertThatThrownBy(() -> MailStatus.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── 테스트 18: DocumentType.from() 잘못된 값 예외 ────────────
    @Test
    @DisplayName("DocumentType.from() - 잘못된 값 전달 시 예외 발생")
    void documentType_fromInvalidValue_throwsException() {
        // from() 내부 for 루프의 "일치하는 값 없음" 브랜치 커버 → JaCoCo 브랜치 100% 달성
        assertThatThrownBy(() -> DocumentType.from("잘못된값"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
