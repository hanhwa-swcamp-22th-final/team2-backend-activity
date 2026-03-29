package com.team2.activity.entity;

import com.team2.activity.entity.enums.DocumentType; // 문서 유형 열거형 (PI, CI, PL, 생산지시서, 출하지시서)
import com.team2.activity.entity.enums.MailStatus;   // 메일 상태 열거형 (SENT, FAILED)
import lombok.Builder; // 빌더 패턴 자동 생성 어노테이션
import lombok.Getter;  // 모든 필드의 getter 메서드 자동 생성 어노테이션

import java.time.LocalDateTime; // 날짜+시간 타입 - 발송 일시에 사용
import java.util.ArrayList;     // 빈 리스트 초기화에 사용
import java.util.List;          // 문서 유형 목록, 첨부파일 목록 타입

// 메일 발송 이력 도메인 객체 (DB 테이블: email_logs + email_log_types + email_log_attachments)
@Getter // 모든 필드의 getter 자동 생성 (getClientId(), getStatus() 등)
public class MailHistory {

    private final Long clientId;                   // 거래처 ID (FK→master.clients)
    private final String title;                    // 메일 제목
    private final String recipientEmail;           // 수신자 이메일 주소 (필수)
    private final String recipientName;            // 수신자 이름 (선택)
    private final Long senderId;                   // 발송자 사용자 ID (FK→auth.users, 로그인 사용자 자동 설정)
    private MailStatus status;                     // 발송 상태 - 유일하게 변경 가능한 필드 (updateStatus() 사용)
    private final LocalDateTime sentAt;            // 발송 일시 (발송 실패 시 null 허용)
    private final String poId;                     // 연결된 수주건 ID (선택, FK→document.purchase_orders)
    private final List<DocumentType> documentTypes;    // 포함된 문서 유형 목록 (PI, CI, PL 등)
    private final List<String> attachmentFileNames;    // 첨부파일 이름 목록

    @Builder // 이 생성자를 기반으로 빌더 클래스 자동 생성
    private MailHistory(Long clientId, String title, String recipientEmail,
                        String recipientName, Long senderId, MailStatus status,
                        LocalDateTime sentAt, String poId,
                        List<DocumentType> documentTypes, List<String> attachmentFileNames) {
        this.clientId = clientId;                                                          // 거래처 ID 초기화
        this.title = title;                                                                // 제목 초기화
        this.recipientEmail = recipientEmail;                                              // 수신자 이메일 초기화
        this.recipientName = recipientName;                                                // 수신자 이름 초기화
        this.senderId = senderId;                                                          // 발송자 ID 초기화
        this.status = status != null ? status : MailStatus.SENT;                          // status 미설정 시 기본값 SENT 적용
        this.sentAt = sentAt;                                                              // 발송 일시 초기화 (실패 시 null)
        this.poId = poId;                                                                  // PO ID 초기화
        this.documentTypes = documentTypes != null ? documentTypes : new ArrayList<>();    // null 방어 - 빈 리스트로 초기화
        this.attachmentFileNames = attachmentFileNames != null ? attachmentFileNames : new ArrayList<>(); // null 방어 - 빈 리스트로 초기화
    }

    // 발송 상태 변경 메서드 (발송 성공/실패 업데이트 또는 재발송 시 사용)
    public void updateStatus(MailStatus status) {
        this.status = status; // 발송 상태 변경 (SENT ↔ FAILED)
    }
}
