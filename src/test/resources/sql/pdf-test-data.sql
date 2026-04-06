-- PDF 보고서 테스트용 픽스처 데이터
-- 사용법: @Sql("classpath:sql/pdf-test-data.sql")
-- 포함 내용: 활동 3개(MEETING·SCHEDULE·MEMO) + 패키지 1개 + 패키지-활동 연결 + 열람자 1명

-- ── 활동 데이터 ─────────────────────────────────────────────────
INSERT INTO activities (
    activity_id, client_id, po_id, activity_author_id,
    activity_date, activity_type, activity_title, activity_content,
    activity_priority, activity_schedule_from, activity_schedule_to,
    created_at, updated_at
) VALUES
-- MEETING: 일정 없음, 우선순위 HIGH
(1001, 10, 'PO-PDF-001', 7,
 '2025-03-01', 'MEETING', '1분기 영업 전략 미팅', '거래처 방문 및 계약 조건 협의',
 'HIGH', NULL, NULL,
 '2025-03-01 09:00:00', '2025-03-01 09:00:00'),

-- SCHEDULE: 시작일·종료일 있음, 우선순위 MEDIUM
(1002, 10, 'PO-PDF-001', 7,
 '2025-03-10', 'SCHEDULE', '제품 납기 일정 관리', '생산 라인 점검 및 선적 일정 확인',
 'MEDIUM', '2025-03-10', '2025-03-20',
 '2025-03-10 10:00:00', '2025-03-10 10:00:00'),

-- MEMO: 우선순위 없음
(1003, 10, NULL, 7,
 '2025-03-15', 'MEMO', '클레임 대응 메모', '불량률 이슈 관련 내부 검토 내용 기록',
 NULL, NULL, NULL,
 '2025-03-15 14:00:00', '2025-03-15 14:00:00');

-- ── 활동 패키지 ──────────────────────────────────────────────────
INSERT INTO activity_packages (
    package_id, package_title, package_description,
    po_id, creator_id, date_from, date_to,
    created_at, updated_at
) VALUES (
    2001, '2025년 3월 영업활동 보고서', '3월 주요 영업 활동 묶음',
    'PO-PDF-001', 7, '2025-03-01', '2025-03-31',
    '2025-03-31 18:00:00', '2025-03-31 18:00:00'
);

-- ── 패키지-활동 연결 ──────────────────────────────────────────────
INSERT INTO activity_package_items (package_item_id, package_id, activity_id)
VALUES
    (3001, 2001, 1001),
    (3002, 2001, 1002),
    (3003, 2001, 1003);

-- ── 패키지 열람 권한 ──────────────────────────────────────────────
INSERT INTO activity_package_viewers (package_viewer_id, package_id, user_id)
VALUES (4001, 2001, 8);
