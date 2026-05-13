-- Phase 5 demo data: medication reminders, health tracking, follow-up plans and AI templates.
-- Execute after:
-- 1. sql/01_create_schema.sql
-- 2. sql/02_seed_phase1_data.sql
-- 3. sql/03_seed_phase3_data.sql
-- 4. sql/04_seed_phase4_data.sql

USE `medical_care`;

INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `sort_no`, `status`) VALUES
('patient:medication-reminders', 'Medication Reminders', 'MENU', 0, '/patient/medication-reminders', 55, 1),
('patient:health-tracks', 'Health Tracking', 'MENU', 0, '/patient/health-tracks', 56, 1),
('doctor:patients', 'My Patients', 'MENU', 0, '/doctor/patients', 45, 1),
('system:notifications', 'Notifications', 'MENU', 0, '/notifications', 90, 1);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'patient:medication-reminders'
WHERE r.role_code = 'PATIENT';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'patient:health-tracks'
WHERE r.role_code = 'PATIENT';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'doctor:patients'
WHERE r.role_code = 'DOCTOR';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'system:notifications'
WHERE r.role_code IN ('PATIENT', 'DOCTOR', 'PHARMACIST', 'ADMIN');

INSERT INTO `medicine_order` (
  `order_no`, `patient_id`, `prescription_id`, `insurance_card_id`,
  `total_amount`, `insurance_amount`, `self_amount`, `status`, `pay_time`, `remark`
)
SELECT 'MO-PHASE5-DEMO-001', p.patient_id, p.id, c.id,
       COALESCE(SUM(i.unit_price * i.quantity), 0),
       ROUND(COALESCE(SUM(i.unit_price * i.quantity), 0) * 0.70, 2),
       ROUND(COALESCE(SUM(i.unit_price * i.quantity), 0) * 0.30, 2),
       'PAID', NOW(), 'Phase 5 demo paid medicine order'
FROM `prescription` p
JOIN `prescription_item` i ON i.prescription_id = p.id AND i.deleted = 0
JOIN `insurance_card` c ON c.patient_id = p.patient_id AND c.status = 'ACTIVE' AND c.deleted = 0
  AND c.id = (
    SELECT MIN(c2.id)
    FROM `insurance_card` c2
    WHERE c2.patient_id = p.patient_id AND c2.status = 'ACTIVE' AND c2.deleted = 0
  )
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  AND NOT EXISTS (SELECT 1 FROM `medicine_order` o WHERE o.order_no = 'MO-PHASE5-DEMO-001')
GROUP BY p.id, p.patient_id, c.id;

INSERT INTO `medicine_order_item` (
  `order_id`, `drug_id`, `drug_name`, `specification`, `quantity`, `unit_price`,
  `amount`, `insurance_covered`, `insurance_amount`, `remark`
)
SELECT o.id, i.drug_id, i.drug_name, i.specification, i.quantity, i.unit_price,
       ROUND(i.unit_price * i.quantity, 2), d.insurance_covered,
       ROUND(i.unit_price * i.quantity * 0.70, 2), 'Phase 5 demo order item'
FROM `medicine_order` o
JOIN `prescription` p ON p.id = o.prescription_id
JOIN `prescription_item` i ON i.prescription_id = p.id AND i.deleted = 0
JOIN `drug` d ON d.id = i.drug_id
WHERE o.order_no = 'MO-PHASE5-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `medicine_order_item` oi
    WHERE oi.order_id = o.id AND oi.drug_id = i.drug_id AND oi.deleted = 0
  );

INSERT INTO `insurance_payment_record` (
  `payment_no`, `order_id`, `patient_id`, `insurance_card_id`,
  `before_balance`, `paid_amount`, `after_balance`, `status`, `pay_time`, `remark`
)
SELECT 'PAY-PHASE5-DEMO-001', o.id, o.patient_id, o.insurance_card_id,
       c.balance, o.total_amount, GREATEST(c.balance - o.total_amount, 0),
       'SUCCESS', NOW(), 'Phase 5 demo insurance payment'
FROM `medicine_order` o
JOIN `insurance_card` c ON c.id = o.insurance_card_id
WHERE o.order_no = 'MO-PHASE5-DEMO-001'
  AND NOT EXISTS (SELECT 1 FROM `insurance_payment_record` pr WHERE pr.payment_no = 'PAY-PHASE5-DEMO-001');

UPDATE `prescription` p
JOIN `medicine_order` o ON o.prescription_id = p.id AND o.order_no = 'MO-PHASE5-DEMO-001'
SET p.status = 'PAID'
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001';

INSERT INTO `medication_plan` (
  `patient_id`, `prescription_id`, `prescription_item_id`, `drug_id`, `drug_name`,
  `start_date`, `end_date`, `times_per_day`, `reminder_times`, `dosage`, `usage_method`,
  `ai_reminder_text`, `status`, `remark`
)
SELECT p.patient_id, p.id, i.id, i.drug_id, i.drug_name,
       CURDATE(), DATE_ADD(CURDATE(), INTERVAL (GREATEST(COALESCE(i.duration_days, 1), 1) - 1) DAY),
       CASE
         WHEN i.frequency LIKE '%3%' THEN 3
         WHEN i.frequency LIKE '%2%' THEN 2
         ELSE 1
       END,
       CASE
         WHEN i.frequency LIKE '%3%' THEN '08:00,13:00,20:00'
         WHEN i.frequency LIKE '%2%' THEN '08:00,20:00'
         ELSE '08:00'
       END,
       i.dosage, i.usage_method,
       CONCAT('Please take ', i.drug_name, ' as prescribed. This reminder is an auxiliary reference.'),
       'ACTIVE', 'Phase 5 demo medication plan'
FROM `prescription` p
JOIN `prescription_item` i ON i.prescription_id = p.id AND i.deleted = 0
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `medication_plan` mp WHERE mp.prescription_item_id = i.id AND mp.deleted = 0
  );

INSERT INTO `medication_reminder` (`plan_id`, `patient_id`, `drug_id`, `remind_time`, `status`, `remark`)
SELECT mp.id, mp.patient_id, mp.drug_id, CONCAT(CURDATE(), ' 08:00:00'), 'WAITING', 'Phase 5 demo morning reminder'
FROM `medication_plan` mp
WHERE mp.remark = 'Phase 5 demo medication plan'
  AND NOT EXISTS (
    SELECT 1 FROM `medication_reminder` mr
    WHERE mr.plan_id = mp.id AND mr.remind_time = CONCAT(CURDATE(), ' 08:00:00') AND mr.deleted = 0
  );

INSERT INTO `medication_reminder` (`plan_id`, `patient_id`, `drug_id`, `remind_time`, `status`, `remark`)
SELECT mp.id, mp.patient_id, mp.drug_id, CONCAT(CURDATE(), ' 20:00:00'), 'WAITING', 'Phase 5 demo evening reminder'
FROM `medication_plan` mp
WHERE mp.remark = 'Phase 5 demo medication plan' AND mp.times_per_day >= 2
  AND NOT EXISTS (
    SELECT 1 FROM `medication_reminder` mr
    WHERE mr.plan_id = mp.id AND mr.remind_time = CONCAT(CURDATE(), ' 20:00:00') AND mr.deleted = 0
  );

INSERT INTO `health_track_record` (
  `patient_id`, `doctor_id`, `record_date`, `symptom`, `temperature`,
  `systolic_pressure`, `diastolic_pressure`, `heart_rate`, `blood_glucose`,
  `medication_feedback`, `abnormal_flag`, `remark`
)
SELECT pp.id, dp.id, CURDATE(), 'Sore throat improved, mild cough remains.', 37.1,
       122, 78, 82, 5.60, 'Taking medicine on time, no obvious adverse reaction.', 0,
       'Phase 5 demo health track'
FROM `patient_profile` pp
JOIN `sys_user` pu ON pu.id = pp.user_id AND pu.username = 'patient_li'
CROSS JOIN `doctor_profile` dp
JOIN `sys_user` du ON du.id = dp.user_id AND du.username = 'doctor_zhang'
WHERE NOT EXISTS (
  SELECT 1 FROM `health_track_record` h
  WHERE h.patient_id = pp.id AND h.record_date = CURDATE() AND h.remark = 'Phase 5 demo health track'
);

INSERT INTO `follow_up_plan` (`patient_id`, `doctor_id`, `session_id`, `plan_time`, `content`, `status`, `remark`)
SELECT s.patient_id, s.doctor_id, s.id, DATE_ADD(NOW(), INTERVAL 2 DAY),
       'Review cough and sore throat changes, then confirm medication adherence.',
       'PENDING', 'Phase 5 demo follow-up'
FROM `consultation_session` s
WHERE s.session_no = 'CONSULT-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `follow_up_plan` f
    WHERE f.session_id = s.id AND f.remark = 'Phase 5 demo follow-up'
  );

INSERT INTO `system_notification` (
  `receiver_id`, `sender_id`, `title`, `content`, `notification_type`, `business_id`, `read_status`, `remark`
)
SELECT pu.id, du.id, 'Medication reminders generated',
       'Medication plans and reminders have been generated from the paid prescription.',
       'MEDICATION_PLAN', o.id, 0, 'Phase 5 demo notification'
FROM `medicine_order` o
JOIN `patient_profile` pp ON pp.id = o.patient_id
JOIN `sys_user` pu ON pu.id = pp.user_id
JOIN `doctor_profile` dp ON dp.id = (SELECT doctor_id FROM `prescription` p WHERE p.id = o.prescription_id)
JOIN `sys_user` du ON du.id = dp.user_id
WHERE o.order_no = 'MO-PHASE5-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `system_notification` n
    WHERE n.business_id = o.id AND n.notification_type = 'MEDICATION_PLAN' AND n.remark = 'Phase 5 demo notification'
  );

INSERT INTO `ai_prompt_template` (
  `scenario`, `template_name`, `system_prompt`, `user_prompt_template`, `status`, `remark`
)
SELECT 'MEDICATION_REMINDER_TEXT', 'Medication reminder text',
       'You are a medication reminder assistant. Keep output concise and cautious.',
       'Generate a medication reminder based on drug, dosage, frequency and usage.',
       1, 'Phase 5 demo AI prompt'
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_prompt_template` t WHERE t.scenario = 'MEDICATION_REMINDER_TEXT' AND t.deleted = 0
);

INSERT INTO `ai_prompt_template` (
  `scenario`, `template_name`, `system_prompt`, `user_prompt_template`, `status`, `remark`
)
SELECT 'HEALTH_TRACK_ANALYSIS', 'Health tracking analysis',
       'You are a health record assistant and cannot replace doctor diagnosis.',
       'Generate an auxiliary analysis based on symptoms, vital signs and medication feedback.',
       1, 'Phase 5 demo AI prompt'
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_prompt_template` t WHERE t.scenario = 'HEALTH_TRACK_ANALYSIS' AND t.deleted = 0
);

INSERT INTO `ai_knowledge_document` (`title`, `doc_type`, `content`, `source`, `version_no`, `status`, `remark`)
SELECT 'Medication reminder demo knowledge', 'PLATFORM_GUIDE',
       'Medication reminders are only auxiliary tools for following prescriptions and cannot replace doctors or pharmacists.',
       'platform-demo', 'v1', 1, 'Phase 5 demo knowledge'
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_knowledge_document` d WHERE d.title = 'Medication reminder demo knowledge' AND d.deleted = 0
);
