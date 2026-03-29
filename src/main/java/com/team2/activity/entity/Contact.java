package com.team2.activity.entity;

import lombok.Builder; // 빌더 패턴 자동 생성 어노테이션
import lombok.Getter;  // 모든 필드의 getter 메서드 자동 생성 어노테이션

// 거래처 담당자 연락처 도메인 객체 (DB 테이블: contacts)
// 연락처는 등록한 영업담당자 개인 자산으로 관리됨
@Getter // 모든 필드의 getter 자동 생성 (getClientId(), getName() 등)
public class Contact {

    private final Long clientId; // 거래처 ID (생성 후 변경 불가 - final, FK→master.clients)
    private String name;         // 담당자 이름 (필수)
    private String position;     // 담당자 직위 (선택, 예: "Team Leader", "Team Member")
    private String email;        // 담당자 이메일 (선택, 이메일 발송 시 수신자로 활용)
    private String tel;          // 담당자 전화번호 (선택)

    @Builder // 이 생성자를 기반으로 빌더 클래스 자동 생성 → Contact.builder().clientId(1L)...build() 가능
    private Contact(Long clientId, String name, String position, String email, String tel) {
        this.clientId = clientId;   // 거래처 ID 초기화
        this.name = name;           // 담당자 이름 초기화
        this.position = position;   // 직위 초기화
        this.email = email;         // 이메일 초기화
        this.tel = tel;             // 전화번호 초기화
    }

    // 연락처 수정 메서드 - clientId는 변경 불가이므로 파라미터에서 제외
    public void update(String name, String position, String email, String tel) {
        this.name = name;         // 담당자 이름 변경
        this.position = position; // 직위 변경 (null 전달 시 제거)
        this.email = email;       // 이메일 변경
        this.tel = tel;           // 전화번호 변경 (null 전달 시 제거)
    }
}
