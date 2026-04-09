-- activities (10건)
INSERT INTO activities (client_id, po_id, activity_author_id, activity_date, activity_type, activity_title, activity_content, activity_priority, activity_schedule_from, activity_schedule_to, created_at, updated_at) VALUES
(1, 'PO20260001', 1, '2026-03-01', 'meeting', '글로벌 스틸 초기 미팅', '거래처 담당자와 첫 미팅 진행. 제품 소개 및 가격 협의.', NULL, NULL, NULL, NOW(), NOW()),
(1, 'PO20260001', 1, '2026-03-05', 'memo', '글로벌 스틸 미팅 후기', '미팅 결과 정리. 샘플 요청 확인.', NULL, NULL, NULL, NOW(), NOW()),
(2, 'PO20260002', 2, '2026-03-10', 'meeting', '아시아 무역 견적 협의', '견적서 기반 가격 협의. 물류비 별도 논의 필요.', NULL, NULL, NULL, NOW(), NOW()),
(2, 'PO20260002', 2, '2026-03-12', 'issue', '아시아 무역 납기 지연', '원자재 수급 문제로 납기 2주 지연 예상.', 'high', NULL, NULL, NOW(), NOW()),
(3, 'PO20260003', 1, '2026-03-15', 'meeting', '유럽 트레이딩 계약 논의', '연간 계약 조건 논의. MOQ 및 결제 조건 확인.', NULL, NULL, NULL, NOW(), NOW()),
(3, 'PO20260003', 3, '2026-03-18', 'memo', '유럽 트레이딩 계약서 검토', '법무팀 계약서 검토 요청 완료.', NULL, NULL, NULL, NOW(), NOW()),
(1, 'PO20260001', 2, '2026-03-20', 'issue', '글로벌 스틸 품질 이슈', '납품 제품 일부 규격 미달. 교체 배송 진행.', 'normal', NULL, NULL, NOW(), NOW()),
(4, 'PO20260004', 3, '2026-03-22', 'schedule', '동남아 물산 공장 방문', '베트남 공장 실사 일정.', NULL, '2026-04-01', '2026-04-03', NOW(), NOW()),
(4, 'PO20260004', 3, '2026-03-25', 'meeting', '동남아 물산 가격 재협상', '환율 변동으로 인한 가격 재협상 요청.', NULL, NULL, NULL, NOW(), NOW()),
(2, 'PO20260002', 1, '2026-03-28', 'schedule', '아시아 무역 분기 리뷰', '1분기 거래 실적 리뷰 미팅.', NULL, '2026-04-05', '2026-04-05', NOW(), NOW());

-- contacts (10건)
INSERT INTO contacts (client_id, writer_id, contact_name, contact_position, contact_email, contact_tel, created_at, updated_at) VALUES
(1, 1, '김철수', '부장', 'chulsoo.kim@globalsteel.com', '010-1111-1001', NOW(), NOW()),
(1, 1, '이영희', '과장', 'younghee.lee@globalsteel.com', '010-1111-1002', NOW(), NOW()),
(2, 2, '박민수', '대리', 'minsoo.park@asiatrade.com', '010-2222-2001', NOW(), NOW()),
(2, 2, '최지은', '차장', 'jieun.choi@asiatrade.com', '010-2222-2002', NOW(), NOW()),
(3, 1, '정하늘', '팀장', 'haneul.jung@eurotrading.com', '010-3333-3001', NOW(), NOW()),
(3, 3, '한서연', '사원', 'seoyeon.han@eurotrading.com', '010-3333-3002', NOW(), NOW()),
(4, 3, '강동원', '부장', 'dongwon.kang@seasia.com', '010-4444-4001', NOW(), NOW()),
(4, 3, '윤서준', '과장', 'seojun.yoon@seasia.com', '010-4444-4002', NOW(), NOW()),
(1, 2, '조은비', '대리', 'eunbi.jo@globalsteel.com', '010-1111-1003', NOW(), NOW()),
(2, 1, '임재현', '팀장', 'jaehyun.lim@asiatrade.com', '010-2222-2003', NOW(), NOW());

-- email_logs (10건)
INSERT INTO email_logs (client_id, po_id, email_title, email_recipient_name, email_recipient_email, email_sender_id, email_status, email_sent_at, created_at) VALUES
(1, 'PO20260001', '글로벌 스틸 견적서 발송', '김철수', 'chulsoo.kim@globalsteel.com', 1, 'sent', NOW(), NOW()),
(1, 'PO20260001', '글로벌 스틸 PI 발송', '이영희', 'younghee.lee@globalsteel.com', 1, 'sent', NOW(), NOW()),
(2, 'PO20260002', '아시아 무역 CI 발송', '박민수', 'minsoo.park@asiatrade.com', 2, 'sent', NOW(), NOW()),
(2, 'PO20260002', '아시아 무역 PL 발송', '최지은', 'jieun.choi@asiatrade.com', 2, 'failed', NULL, NOW()),
(3, 'PO20260003', '유럽 트레이딩 계약서 발송', '정하늘', 'haneul.jung@eurotrading.com', 1, 'sent', NOW(), NOW()),
(3, 'PO20260003', '유럽 트레이딩 PI 발송', '한서연', 'seoyeon.han@eurotrading.com', 3, 'sent', NOW(), NOW()),
(4, 'PO20260004', '동남아 물산 생산지시서 발송', '강동원', 'dongwon.kang@seasia.com', 3, 'failed', NULL, NOW()),
(4, 'PO20260004', '동남아 물산 선적지시서 발송', '윤서준', 'seojun.yoon@seasia.com', 3, 'sent', NOW(), NOW()),
(1, 'PO20260001', '글로벌 스틸 수정 견적서', '김철수', 'chulsoo.kim@globalsteel.com', 2, 'sent', NOW(), NOW()),
(2, 'PO20260002', '아시아 무역 납기 안내', '임재현', 'jaehyun.lim@asiatrade.com', 1, 'sent', NOW(), NOW());

-- email_log_types (email_logs 1~10에 대한 문서 유형)
INSERT INTO email_log_types (email_log_id, email_doc_type) VALUES
(1, 'PI'), (1, 'CI'),
(2, 'PI'),
(3, 'CI'), (3, 'PL'),
(4, 'PL'),
(5, 'PI'), (5, 'CI'),
(6, 'PI'),
(7, 'production_order'),
(8, 'shipment_order'),
(9, 'PI'),
(10, 'CI');

-- email_log_attachments (email_logs 1~10에 대한 첨부파일)
INSERT INTO email_log_attachments (email_log_id, email_attachment_filename, email_attachment_file_path) VALUES
(1, 'PI_globalsteel_001.pdf', '/files/2026/03/PI_globalsteel_001.pdf'),
(1, 'CI_globalsteel_001.pdf', '/files/2026/03/CI_globalsteel_001.pdf'),
(2, 'PI_globalsteel_002.pdf', '/files/2026/03/PI_globalsteel_002.pdf'),
(3, 'CI_asiatrade_001.pdf', '/files/2026/03/CI_asiatrade_001.pdf'),
(5, 'contract_eurotrading.pdf', '/files/2026/03/contract_eurotrading.pdf'),
(6, 'PI_eurotrading_001.pdf', '/files/2026/03/PI_eurotrading_001.pdf'),
(7, 'production_order_seasia.pdf', '/files/2026/03/production_order_seasia.pdf'),
(8, 'shipping_order_seasia.pdf', '/files/2026/03/shipping_order_seasia.pdf'),
(9, 'PI_globalsteel_revised.pdf', '/files/2026/03/PI_globalsteel_revised.pdf'),
(10, 'CI_asiatrade_delivery.pdf', '/files/2026/03/CI_asiatrade_delivery.pdf');

-- activity_packages (10건)
INSERT INTO activity_packages (package_title, package_description, po_id, creator_id, date_from, date_to, created_at, updated_at) VALUES
('글로벌 스틸 3월 활동 보고서', '3월 글로벌 스틸 관련 영업 활동 종합', 'PO20260001', 1, '2026-03-01', '2026-03-31', NOW(), NOW()),
('아시아 무역 3월 활동 보고서', '3월 아시아 무역 관련 영업 활동 종합', 'PO20260002', 2, '2026-03-01', '2026-03-31', NOW(), NOW()),
('유럽 트레이딩 계약 패키지', '유럽 트레이딩 계약 관련 활동 모음', 'PO20260003', 1, '2026-03-15', '2026-03-31', NOW(), NOW()),
('동남아 물산 4월 일정', '동남아 물산 공장 방문 및 협상 일정', 'PO20260004', 3, '2026-04-01', '2026-04-30', NOW(), NOW()),
('1분기 이슈 모음', '1분기 발생한 주요 이슈 정리', NULL, 1, '2026-01-01', '2026-03-31', NOW(), NOW()),
('글로벌 스틸 품질 이슈 패키지', '품질 관련 이슈 및 대응 기록', 'PO20260001', 2, '2026-03-01', '2026-03-31', NOW(), NOW()),
('주간 미팅 기록 (3월 1주)', '3월 1주차 미팅 기록 모음', NULL, 1, '2026-03-01', '2026-03-07', NOW(), NOW()),
('주간 미팅 기록 (3월 2주)', '3월 2주차 미팅 기록 모음', NULL, 2, '2026-03-08', '2026-03-14', NOW(), NOW()),
('아시아 무역 납기 관리', '납기 지연 이슈 및 후속 조치 기록', 'PO20260002', 1, '2026-03-01', '2026-03-31', NOW(), NOW()),
('전체 거래처 3월 요약', '3월 전체 거래처 활동 요약 패키지', NULL, 3, '2026-03-01', '2026-03-31', NOW(), NOW());

-- activity_package_viewers (패키지별 열람자)
INSERT INTO activity_package_viewers (package_id, user_id) VALUES
(1, 2), (1, 3),
(2, 1), (2, 3),
(3, 2),
(4, 1), (4, 2),
(5, 2), (5, 3),
(6, 1),
(7, 2),
(8, 1),
(9, 2), (9, 3),
(10, 1), (10, 2);

-- activity_package_items (패키지별 포함 활동)
INSERT INTO activity_package_items (package_id, activity_id) VALUES
(1, 1), (1, 2), (1, 7),
(2, 3), (2, 4), (2, 10),
(3, 5), (3, 6),
(4, 8), (4, 9),
(5, 4), (5, 7),
(6, 7),
(7, 1),
(8, 3),
(9, 4), (9, 10),
(10, 1), (10, 3), (10, 5), (10, 8);
