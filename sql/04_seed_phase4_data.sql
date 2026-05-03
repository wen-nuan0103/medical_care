-- 第四阶段演示数据：医保卡、医保目录、可购药处方
-- 执行前请先执行：
-- 1. sql/01_create_schema.sql
-- 2. sql/02_seed_phase1_data.sql
-- 3. sql/03_seed_phase3_data.sql

USE `medical_care`;

-- 患者演示医保卡
INSERT INTO `insurance_card` (
  `patient_id`, `card_no`, `holder_name`, `holder_id_card`, `balance`,
  `reimbursement_rate`, `status`, `bind_time`, `remark`
)
SELECT p.id, 'MC-PHASE4-0001', u.real_name, p.id_card_no, 500.00,
       0.70, 'ACTIVE', NOW(), '第四阶段医保购药演示卡'
FROM `patient_profile` p
JOIN `sys_user` u ON u.id = p.user_id AND u.username = 'patient_li'
WHERE NOT EXISTS (
  SELECT 1 FROM `insurance_card` c WHERE c.card_no = 'MC-PHASE4-0001'
);

-- 医保目录模拟数据
INSERT INTO `insurance_drug_catalog` (`drug_id`, `catalog_type`, `reimbursement_rate`, `max_reimbursement_amount`, `status`)
SELECT d.id, 'A', 0.80, 200.00, 1
FROM `drug` d
WHERE d.drug_code = 'DRUG-AMX-001'
ON DUPLICATE KEY UPDATE
`catalog_type` = VALUES(`catalog_type`),
`reimbursement_rate` = VALUES(`reimbursement_rate`),
`max_reimbursement_amount` = VALUES(`max_reimbursement_amount`),
`status` = VALUES(`status`);

INSERT INTO `insurance_drug_catalog` (`drug_id`, `catalog_type`, `reimbursement_rate`, `max_reimbursement_amount`, `status`)
SELECT d.id, 'B', 0.60, 120.00, 1
FROM `drug` d
WHERE d.drug_code IN ('DRUG-IBU-001', 'DRUG-NFD-001', 'DRUG-MET-001')
ON DUPLICATE KEY UPDATE
`catalog_type` = VALUES(`catalog_type`),
`reimbursement_rate` = VALUES(`reimbursement_rate`),
`max_reimbursement_amount` = VALUES(`max_reimbursement_amount`),
`status` = VALUES(`status`);

-- 将第三阶段演示处方推进为已审核通过，供第四阶段直接购药
INSERT INTO `prescription_audit` (
  `prescription_id`, `pharmacist_id`, `audit_result`, `risk_level`, `interaction_result`,
  `stock_result`, `dosage_result`, `advice`, `audit_time`
)
SELECT p.id, ph.id, 'APPROVED', 'LOW',
       '演示处方存在低风险药物组合，已由药师确认可按医嘱使用。',
       '演示药品库存充足。',
       '剂量、频次和疗程用于课程演示，请以医生处方为准。',
       '审核通过，可进入医保卡购药流程。',
       NOW()
FROM `prescription` p
CROSS JOIN `pharmacist_profile` ph
JOIN `sys_user` u ON u.id = ph.user_id AND u.username = 'pharmacist_chen'
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `prescription_audit` a
    WHERE a.prescription_id = p.id AND a.audit_result = 'APPROVED'
  );

UPDATE `prescription`
SET `status` = 'APPROVED',
    `approve_time` = COALESCE(`approve_time`, NOW()),
    `valid_until` = DATE_ADD(NOW(), INTERVAL 3 DAY)
WHERE `prescription_no` = 'RX-PHASE3-DEMO-001'
  AND `status` IN ('PENDING_AUDIT', 'NEED_MODIFY', 'REJECTED');
