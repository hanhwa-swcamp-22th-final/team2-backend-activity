// EmailLogRepositoryTest: EmailLog 엔티티의 Repository 계층 테스트
package com.team2.activity.command.domain.repository;

// EmailLog 엔티티 import
import com.team2.activity.command.domain.entity.EmailLog;
// EmailLogAttachment 엔티티 import
import com.team2.activity.command.domain.entity.EmailLogAttachment;
// EmailLogType 엔티티 import
import com.team2.activity.command.domain.entity.EmailLogType;
// DocumentType 열거형 import
import com.team2.activity.command.domain.entity.enums.DocumentType;
// MailStatus 열거형 import
import com.team2.activity.command.domain.entity.enums.MailStatus;
// 테스트 메서드 표시 import
import org.junit.jupiter.api.DisplayName;
// 테스트 메서드 import
import org.junit.jupiter.api.Test;
// 의존성 주입 import
import org.springframework.beans.factory.annotation.Autowired;
// JPA 테스트 설정 import
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// 페이징 결과 import
import org.springframework.data.domain.Page;
// 페이징 요청 import
import org.springframework.data.domain.PageRequest;
// 동적 쿼리 필터링 import
import org.springframework.data.jpa.domain.Specification;
// 테스트 프로파일 활성화 import
import org.springframework.test.context.ActiveProfiles;

// 날짜-시간 타입 import
import java.time.LocalDateTime;
// 리스트 타입 import
import java.util.List;

// AssertJ assertion import
import static org.assertj.core.api.Assertions.assertThat;

// JPA 테스트 설정 어노테이션
@DataJpaTest
// 테스트 프로파일 설정
@ActiveProfiles("test")
// 테스트 클래스 표시명
@DisplayName("EmailLogRepository 테스트")
// EmailLogRepository 테스트 클래스
class EmailLogRepositoryTest {

    // 테스트 대상 Repository 주입
    @Autowired
    private EmailLogRepository emailLogRepository;

    // ── 공통 픽스처 (테스트용 객체 생성 헬퍼 메서드) ─────────────────────────────────────────────
    // 기본 EmailLog 객체 생성 헬퍼 메서드
    private EmailLog buildEmailLog(Long clientId, Long senderId, MailStatus status) {
        // EmailLog 빌더 패턴으로 객체 생성
        return EmailLog.builder()
                // 거래처 ID 설정
                .clientId(clientId)
                // 이메일 발송자 ID 설정
                .emailSenderId(senderId)
                // 이메일 제목 설정
                .emailTitle("[PI] 발송 테스트")
                // 이메일 수신자 이메일 설정
                .emailRecipientEmail("buyer@example.com")
                // 이메일 상태 설정
                .emailStatus(status)
                // 이메일 발송 일시 설정 (2025-04-10 09:00)
                .emailSentAt(LocalDateTime.of(2025, 4, 10, 9, 0))
                // 빌드 완료
                .build();
    }

    // ── 테스트 1: 저장 및 ID 조회 ───────────────────────────────
    // 이메일 로그 저장 후 ID로 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("이메일 로그 저장 후 ID로 조회")
    // 테스트 메서드
    void saveAndFindById() {
        // 기본 이메일 로그 생성 (거래처ID=1, 발송자ID=10, 상태=SENT)
        EmailLog emailLog = buildEmailLog(1L, 10L, MailStatus.SENT);

        // 이메일 로그 저장
        EmailLog saved = emailLogRepository.save(emailLog);

        // PK 자동 생성 확인
        assertThat(saved.getEmailLogId()).isNotNull();
        // 저장된 ID로 조회하여 제목이 일치하는지 확인
        assertThat(emailLogRepository.findById(saved.getEmailLogId()))
                // Optional이 존재하는지 확인
                .isPresent()
                // Optional에서 값 추출
                .get()
                // 이메일 제목 추출
                .extracting(EmailLog::getEmailTitle)
                // 제목이 "[PI] 발송 테스트"와 일치하는지 확인
                .isEqualTo("[PI] 발송 테스트");
    }

    // ── 테스트 2: 문서 유형 + 첨부파일과 함께 저장 ──────────────
    // 문서 유형과 첨부파일을 포함하여 이메일 로그를 저장하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("문서 유형, 첨부파일 포함하여 저장 및 조회")
    // 테스트 메서드
    void saveWithDocTypesAndAttachments() {
        // 문서 유형과 첨부파일을 포함한 이메일 로그 생성
        EmailLog emailLog = EmailLog.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 이메일 발송자 ID 설정
                .emailSenderId(10L)
                // 이메일 제목 설정
                .emailTitle("[PI/CI] 발송")
                // 이메일 수신자 이메일 설정
                .emailRecipientEmail("buyer@example.com")
                // 이메일 상태 설정 (SENT)
                .emailStatus(MailStatus.SENT)
                // 이메일 발송 일시 설정 (현재 시간)
                .emailSentAt(LocalDateTime.now())
                // 문서 유형 리스트 설정
                .docTypes(List.of(
                        // PI 문서 유형 추가
                        EmailLogType.of(DocumentType.PI),
                        // CI 문서 유형 추가
                        EmailLogType.of(DocumentType.CI)
                ))
                // 첨부파일 리스트 설정
                .attachments(List.of(
                        // PI001.pdf 파일 추가
                        EmailLogAttachment.of("PI001.pdf"),
                        // CI001.pdf 파일 추가
                        EmailLogAttachment.of("CI001.pdf")
                ))
                // 빌드 완료
                .build();

        // 이메일 로그 저장
        EmailLog saved = emailLogRepository.save(emailLog);

        // 저장된 ID로 조회하여 이메일 로그 반환
        EmailLog found = emailLogRepository.findById(saved.getEmailLogId()).orElseThrow();
        // 저장된 문서 유형이 2개인지 확인
        assertThat(found.getDocTypes()).hasSize(2)
                // 문서 유형 추출
                .extracting(EmailLogType::getEmailDocType)
                // PI와 CI 모두 포함되어 있는지 확인 (순서 무관)
                .containsExactlyInAnyOrder(DocumentType.PI, DocumentType.CI);
        // 저장된 첨부파일이 2개인지 확인
        assertThat(found.getAttachments()).hasSize(2)
                // 첨부파일 이름 추출
                .extracting(EmailLogAttachment::getEmailAttachmentFilename)
                // PI001.pdf와 CI001.pdf 모두 포함되어 있는지 확인 (순서 무관)
                .containsExactlyInAnyOrder("PI001.pdf", "CI001.pdf");
    }

    // ── 테스트 3: status 기본값 FAILED 확인 ───────────────────────
    @Test
    @DisplayName("status 미설정 시 기본값 FAILED 저장")
    // 테스트 메서드
    void save_defaultStatusSent() {
        // 상태를 미설정한 이메일 로그 생성
        EmailLog emailLog = EmailLog.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 이메일 발송자 ID 설정
                .emailSenderId(10L)
                // 이메일 제목 설정
                .emailTitle("기본 상태 테스트")
                // 이메일 수신자 이메일 설정
                .emailRecipientEmail("test@example.com")
                // 이메일 발송 일시 설정 (현재 시간)
                .emailSentAt(LocalDateTime.now())
                // 이메일 상태 미설정 (기본값 적용 예상)
                // emailStatus 미설정
                .build();

        // 이메일 로그 저장
        EmailLog saved = emailLogRepository.save(emailLog);

        // 저장된 상태가 FAILED(기본값)인지 확인
        assertThat(saved.getEmailStatus()).isEqualTo(MailStatus.FAILED);
    }

    // ── 테스트 4: clientId 필터 페이징 조회 ─────────────────────
    // 거래처 ID로 이메일 로그를 페이징하여 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("clientId로 이메일 로그 페이징 조회")
    // 테스트 메서드
    void findAll_filterByClientId() {
        // 거래처 ID=1, 상태=SENT인 이메일 로그 저장
        emailLogRepository.save(buildEmailLog(1L, 10L, MailStatus.SENT));
        // 거래처 ID=1, 상태=SENT인 이메일 로그 저장
        emailLogRepository.save(buildEmailLog(1L, 10L, MailStatus.SENT));
        // 거래처 ID=2, 상태=SENT인 이메일 로그 저장 (다른 거래처)
        emailLogRepository.save(buildEmailLog(2L, 10L, MailStatus.SENT));

        // 거래처 ID=1 필터 스펙 생성
        Specification<EmailLog> spec = EmailLogSpecification.withClientId(1L);
        // 필터 스펙과 페이징 정보(0번째 페이지, 10개)로 조회
        Page<EmailLog> result = emailLogRepository.findAll(spec, PageRequest.of(0, 10));

        // 거래처 ID=1인 로그가 2개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ── 테스트 5: emailStatus 필터 조회 ─────────────────────────
    // 이메일 상태로 이메일 로그를 조회하는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("emailStatus로 이메일 로그 조회")
    // 테스트 메서드
    void findAll_filterByStatus() {
        // 상태=SENT인 이메일 로그 저장
        emailLogRepository.save(buildEmailLog(1L, 10L, MailStatus.SENT));
        // 상태=SENT인 이메일 로그 저장
        emailLogRepository.save(buildEmailLog(1L, 10L, MailStatus.SENT));
        // 상태=FAILED인 이메일 로그 저장
        emailLogRepository.save(buildEmailLog(1L, 10L, MailStatus.FAILED));

        // 상태=FAILED 필터 스펙 생성
        Specification<EmailLog> spec = EmailLogSpecification.withEmailStatus(MailStatus.FAILED);
        // 필터 스펙과 페이징 정보로 조회
        Page<EmailLog> result = emailLogRepository.findAll(spec, PageRequest.of(0, 10));

        // FAILED 상태인 로그가 1개인지 확인
        assertThat(result.getTotalElements()).isEqualTo(1);
        // 조회된 로그의 상태가 FAILED인지 확인
        assertThat(result.getContent()
                // 첫 번째 로그 추출
                .get(0)
                // 이메일 상태 추출
                .getEmailStatus())
                // 상태가 FAILED와 일치하는지 확인
                .isEqualTo(MailStatus.FAILED);
    }

    // ── 테스트 6: emailLog 삭제 시 docTypes, attachments cascade 삭제 ─
    // 이메일 로그 삭제 시 문서 유형과 첨부파일도 함께 삭제되는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("이메일 로그 삭제 시 문서 유형, 첨부파일 cascade 삭제")
    // 테스트 메서드
    void delete_cascadeDocTypesAndAttachments() {
        // 문서 유형과 첨부파일을 포함한 이메일 로그 생성
        EmailLog emailLog = EmailLog.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 이메일 발송자 ID 설정
                .emailSenderId(10L)
                // 이메일 제목 설정
                .emailTitle("삭제 테스트")
                // 이메일 수신자 이메일 설정
                .emailRecipientEmail("test@example.com")
                // 이메일 상태 설정
                .emailStatus(MailStatus.SENT)
                // 이메일 발송 일시 설정 (현재 시간)
                .emailSentAt(LocalDateTime.now())
                // 문서 유형 리스트 설정 (PI만 포함)
                .docTypes(List.of(EmailLogType.of(DocumentType.PI)))
                // 첨부파일 리스트 설정 (PI001.pdf만 포함)
                .attachments(List.of(EmailLogAttachment.of("PI001.pdf")))
                // 빌드 완료
                .build();
        // 이메일 로그 저장
        EmailLog saved = emailLogRepository.save(emailLog);

        // 저장된 이메일 로그 ID로 삭제
        emailLogRepository.deleteById(saved.getEmailLogId());

        // 삭제된 이메일 로그가 조회되지 않는지 확인
        assertThat(emailLogRepository.findById(saved.getEmailLogId()))
                // Optional이 비어있는지 확인 (cascade 삭제 완료)
                .isEmpty();
    }

    // ── 테스트 7: sentAt null 허용 (발송 실패 시) ─────────────────
    // 발송 실패 시 발송 일시가 null로 저장되는 테스트
    @Test
    // 테스트 표시명
    @DisplayName("발송 실패 시 sentAt null 저장 허용")
    // 테스트 메서드
    void save_failedStatus_sentAtNull() {
        // 발송 일시를 미설정한 이메일 로그 생성
        EmailLog emailLog = EmailLog.builder()
                // 거래처 ID 설정
                .clientId(1L)
                // 이메일 발송자 ID 설정
                .emailSenderId(10L)
                // 이메일 제목 설정
                .emailTitle("실패 메일")
                // 이메일 수신자 이메일 설정
                .emailRecipientEmail("test@example.com")
                // 이메일 상태 설정 (FAILED)
                .emailStatus(MailStatus.FAILED)
                // 이메일 발송 일시 미설정 (null로 저장 예상)
                // emailSentAt 미설정 → null
                .build();

        // 이메일 로그 저장
        EmailLog saved = emailLogRepository.save(emailLog);

        // 저장된 발송 일시가 null인지 확인
        assertThat(saved.getEmailSentAt()).isNull();
    }
// EmailLogRepositoryTest 클래스 종료
}
