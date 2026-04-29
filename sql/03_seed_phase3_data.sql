-- 第三阶段演示数据：电子处方、药师审核、库存与相互作用规则
-- 执行前请先执行：
-- 1. sql/01_create_schema.sql
-- 2. sql/02_seed_phase1_data.sql

USE `medical_care`;

-- 菜单权限基础项（当前后端菜单为代码生成，这里保留数据库权限数据，方便后续扩展）
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `sort_no`, `status`) VALUES
('patient:prescriptions', '我的处方', 'MENU', 0, '/patient/prescriptions', 40, 1),
('doctor:prescriptions', '我的处方', 'MENU', 0, '/doctor/prescriptions', 40, 1),
('pharmacist:audits', '处方审核', 'MENU', 0, '/pharmacist/audits', 15, 1);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'patient:prescriptions'
WHERE r.role_code = 'PATIENT';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'doctor:prescriptions'
WHERE r.role_code = 'DOCTOR';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code = 'pharmacist:audits'
WHERE r.role_code = 'PHARMACIST';

-- 模拟药物相互作用规则
INSERT IGNORE INTO `drug_interaction_rule` (`drug_a_id`, `drug_b_id`, `risk_level`, `description`, `suggestion`, `status`)
SELECT a.id, b.id, 'LOW', '模拟规则：两种药物同用时可能增加胃肠道不适概率。', '建议药师提醒患者饭后服用并观察不适反应。', 1
FROM `drug` a JOIN `drug` b ON a.drug_code = 'DRUG-AMX-001' AND b.drug_code = 'DRUG-IBU-001';

INSERT IGNORE INTO `drug_interaction_rule` (`drug_a_id`, `drug_b_id`, `risk_level`, `description`, `suggestion`, `status`)
SELECT a.id, b.id, 'MEDIUM', '模拟规则：慢病用药组合需关注血压、血糖变化和患者基础疾病。', '建议药师复核患者近期血压血糖记录，并提示规律监测。', 1
FROM `drug` a JOIN `drug` b ON a.drug_code = 'DRUG-NFD-001' AND b.drug_code = 'DRUG-MET-001';

-- 为演示开方和审核准备一条医生服务卡、问诊和待审处方
INSERT INTO `doctor_card_plan` (
  `doctor_id`, `card_type`, `plan_name`, `price`, `valid_days`, `consultation_times`, `total_minutes`,
  `single_minutes`, `gift_limit_minutes`, `description`, `status`
)
SELECT d.id, 'MONTH', '第三阶段演示月卡', 99.00, 30, 5, 150, 30, 30, '用于第三阶段处方审核演示', 1
FROM `doctor_profile` d JOIN `sys_user` u ON u.id = d.user_id
WHERE u.username = 'doctor_zhang'
  AND NOT EXISTS (
    SELECT 1 FROM `doctor_card_plan` p WHERE p.doctor_id = d.id AND p.plan_name = '第三阶段演示月卡' AND p.deleted = 0
  );

INSERT INTO `doctor_card_order` (
  `order_no`, `patient_id`, `doctor_id`, `plan_id`, `card_type`, `plan_name`, `total_amount`, `pay_amount`,
  `pay_type`, `status`, `pay_time`
)
SELECT 'CARD-PHASE3-DEMO-001', pp.id, dp.id, plan.id, plan.card_type, plan.plan_name,
       plan.price, plan.price, 'MOCK', 'PAID', NOW()
FROM `patient_profile` pp
JOIN `sys_user` pu ON pu.id = pp.user_id AND pu.username = 'patient_li'
CROSS JOIN `doctor_profile` dp
JOIN `sys_user` du ON du.id = dp.user_id AND du.username = 'doctor_zhang'
JOIN `doctor_card_plan` plan ON plan.doctor_id = dp.id AND plan.plan_name = '第三阶段演示月卡'
WHERE NOT EXISTS (
  SELECT 1 FROM `doctor_card_order` o WHERE o.order_no = 'CARD-PHASE3-DEMO-001'
);

INSERT INTO `private_doctor_card` (
  `patient_id`, `doctor_id`, `plan_id`, `order_id`, `card_type`, `start_time`, `expire_time`,
  `total_times`, `remaining_times`, `total_minutes`, `remaining_minutes`, `gifted_minutes`, `status`
)
SELECT o.patient_id, o.doctor_id, o.plan_id, o.id, o.card_type, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY),
       5, 4, 150, 120, 0, 'ACTIVE'
FROM `doctor_card_order` o
WHERE o.order_no = 'CARD-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `private_doctor_card` c WHERE c.order_id = o.id
  );

INSERT INTO `consultation_session` (
  `session_no`, `patient_id`, `doctor_id`, `card_id`, `chief_complaint`, `disease_desc`,
  `status`, `start_time`, `allowed_minutes`, `used_minutes`, `summary`
)
SELECT 'CONSULT-PHASE3-DEMO-001', c.patient_id, c.doctor_id, c.id,
       '咳嗽、咽痛三天', '无明显发热，既往无明确药物过敏史。',
       'IN_PROGRESS', NOW(), 30, 6, '第三阶段演示问诊摘要'
FROM `private_doctor_card` c
JOIN `doctor_card_order` o ON o.id = c.order_id AND o.order_no = 'CARD-PHASE3-DEMO-001'
WHERE NOT EXISTS (
  SELECT 1 FROM `consultation_session` s WHERE s.session_no = 'CONSULT-PHASE3-DEMO-001'
);

INSERT INTO `prescription` (
  `prescription_no`, `session_id`, `patient_id`, `doctor_id`, `diagnosis`, `status`,
  `valid_until`, `doctor_note`, `patient_instruction`, `submit_time`
)
SELECT 'RX-PHASE3-DEMO-001', s.id, s.patient_id, s.doctor_id, '急性上呼吸道感染', 'PENDING_AUDIT',
       DATE_ADD(NOW(), INTERVAL 3 DAY), '请药师重点复核药物组合和库存。', '按医嘱服用，如出现不适请及时复诊。', NOW()
FROM `consultation_session` s
WHERE s.session_no = 'CONSULT-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `prescription` p WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  );

INSERT INTO `prescription_item` (
  `prescription_id`, `drug_id`, `drug_name`, `specification`, `quantity`, `unit_price`,
  `dosage`, `frequency`, `duration_days`, `usage_method`, `medication_time`, `remark`
)
SELECT p.id, d.id, d.drug_name, d.specification, 1, d.price,
       '1粒', '每日3次', 3, '口服', '饭后', '演示处方明细'
FROM `prescription` p
JOIN `drug` d ON d.drug_code = 'DRUG-AMX-001'
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `prescription_item` i WHERE i.prescription_id = p.id AND i.drug_id = d.id
  );

INSERT INTO `prescription_item` (
  `prescription_id`, `drug_id`, `drug_name`, `specification`, `quantity`, `unit_price`,
  `dosage`, `frequency`, `duration_days`, `usage_method`, `medication_time`, `remark`
)
SELECT p.id, d.id, d.drug_name, d.specification, 1, d.price,
       '1粒', '必要时服用', 3, '口服', '饭后', '演示相互作用检查'
FROM `prescription` p
JOIN `drug` d ON d.drug_code = 'DRUG-IBU-001'
WHERE p.prescription_no = 'RX-PHASE3-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM `prescription_item` i WHERE i.prescription_id = p.id AND i.drug_id = d.id
  );
