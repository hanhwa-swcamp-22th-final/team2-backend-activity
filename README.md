# team2-backend-activity

> SalesBoost Activity Service `:8013`
> 영업 활동기록 · 연락처(개인 주소록) · 이메일 이력 · 활동 패키지(PDF)

CQRS — `command/` (JPA + Spring Data) / `query/` (MyBatis XML).

---

## 도메인

| 도메인 | 주요 기능 |
|---|---|
| **활동기록** (`activities`) | 미팅/이슈/일정 등록·수정·삭제, 우선순위, PO·거래처·컨택 연결 |
| **컨택** (`contacts`) | 영업담당자 개인 주소록 — **거래처 무관**, 작성자(writer_id) 기준 조회 |
| **이메일 이력** (`email_logs`) | Documents 발송 결과 수신 (X-Internal-Token), 실패 건 1-click 재전송 |
| **활동 패키지** (`activity_packages`) | 활동기록 묶음 PDF 보고서, 열람 권한 (팀 단위 멀티선택) |

---

## 핵심 정책

### Contact = 개인 인맥 자산
- `client_id` 컬럼 없음 — 거래처 사람일 필요 없는 자유 컨택
- 작성자(`writer_id`) 기준 조회 (ADMIN 만 전체)
- 같은 팀 buyer 등록 시 sync 가 팀원 각각에게 별도 row 생성 → 팀이 자동 공유
- admin 외 다른 사람의 컨택 못 봄 (개인 주소록)

### Email Log = Activity 가 유일한 Write Owner
- Documents 가 메일 발송 → `POST /api/email-logs/internal/...` (X-Internal-Token) 으로 Activity 에 INSERT 위임
- 재전송 시 Activity 가 `email_logs.status = SENDING` 으로 update 후 Documents 호출, 결과 수신 후 SENT/FAILED update
- Documents 는 read 도 안 함 — 데이터 소유 단일화

### 활동 패키지 열람 권한
- 작성자가 사용자 다중선택 (팀 단위 그룹핑)
- ActivityPackageViewer 테이블이 N:M 매핑

---

## 외부 호출 (Feign)

| 대상 | 용도 |
|---|---|
| `auth-service` (`/api/users/internal/by-role`) | 메일 자동 수신처 resolver (sales/production/shipping 팀원 조회) |
| `master-service` (`/api/buyers/internal/{clientId}`) | PI 자동 발송 시 거래처 buyer 이메일 조회 |
| `documents-service` (`/api/emails/internal/send-no-log`) | 메일 재전송 (no-log 모드) |

`X-Internal-Token` 헤더 자동 주입 (`InternalTokenFeignInterceptor`).

---

## API 엔드포인트 (주요)

```
[활동기록]
GET    /api/activities
POST   /api/activities
PUT    /api/activities/{id}
DELETE /api/activities/{id}

[컨택]
GET    /api/contacts                  ← writerId 기준 자동 필터 (ADMIN 전체)
POST   /api/contacts                  ← 자유 컨택 등록 (clientId 없음)
PUT    /api/contacts/{id}
DELETE /api/contacts/{id}

[이메일 이력]
GET    /api/email-logs                ← 페이징 + 필터
PUT    /api/email-logs/{id}/resend    ← 재전송 (Documents 호출)
POST   /api/email-logs/internal/...   ← Documents → Activity (X-Internal-Token)

[활동 패키지]
GET    /api/activity-packages         ← @PreAuthorize ADMIN/SALES
POST   /api/activity-packages
PUT    /api/activity-packages/{id}
DELETE /api/activity-packages/{id}
GET    /api/activity-packages/{id}/pdf

[내부 sync (X-Internal-Token)]
POST   /api/contacts/internal         ← Master Buyer 등록 → Contact sync
```

---

## 스키마 (`team2_activity`)

```
activities                 # 활동기록
activity_packages          # 패키지
activity_package_items     # 패키지-활동 매핑
activity_package_viewers   # 패키지 열람 권한 (사용자 N:M)
contacts                   # 개인 주소록 (client_id 없음)
email_logs                 # 메일 이력
email_log_attachments      # 첨부
email_log_types            # 메일 타입 ENUM 매핑
```

자세한 DDL: `ddl/activity_service.sql`

---

## 환경 변수

| 변수 | 기본값 | 설명 |
|---|---|---|
| `SERVER_PORT` | `8013` | 서비스 포트 |
| `DB_URL` | `jdbc:mariadb://team2-mariadb:3306/team2_activity` | DB 접속 |
| `DB_USERNAME` / `DB_PASSWORD` | k8s Secret | DB 인증 |
| `INTERNAL_API_TOKEN` | k8s Secret | 시스템 호출 토큰 (prod profile 미설정 시 fail-fast) |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://backend-auth:8011/.well-known/jwks.json` | JWT 검증용 JWKS |
| `AUTH_SERVICE_URL` / `MASTER_SERVICE_URL` / `DOCUMENTS_SERVICE_URL` | `http://backend-{name}:80xx` | Feign upstream |

---

## 실행

```bash
./gradlew bootRun                # 로컬
./gradlew build -x test          # CLAUDE.md pre-push 체크
./gradlew assemble               # test 컴파일도 스킵 (test scenario 깨진 항목 우회)
docker build -t team2-backend-activity .
```
