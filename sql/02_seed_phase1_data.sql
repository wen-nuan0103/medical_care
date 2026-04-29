-- 悦医健康第一阶段初始化数据
-- 执行前请先执行 sql/01_create_schema.sql
-- 默认密码均为：123456

USE `medical_care`;

-- 角色
INSERT IGNORE INTO `sys_role` (`role_code`, `role_name`, `status`, `remark`) VALUES
('PATIENT', '患者', 1, '患者端角色'),
('DOCTOR', '医生', 1, '医生端角色'),
('PHARMACIST', '药剂师', 1, '药剂师端角色'),
('ADMIN', '管理员', 1, '平台管理员角色');

-- 权限菜单
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `path`, `sort_no`, `status`) VALUES
('patient:home', '患者首页', 'MENU', 0, '/patient/home', 10, 1),
('patient:doctors', '找医生', 'MENU', 0, '/patient/doctors', 20, 1),
('patient:drugs', '药品商城', 'MENU', 0, '/patient/drugs', 30, 1),
('doctor:workbench', '医生工作台', 'MENU', 0, '/doctor/workbench', 10, 1),
('pharmacist:workbench', '药师工作台', 'MENU', 0, '/pharmacist/workbench', 10, 1),
('pharmacist:drugs', '药品管理', 'MENU', 0, '/pharmacist/drugs', 20, 1),
('admin:dashboard', '管理看板', 'MENU', 0, '/admin/dashboard', 10, 1),
('admin:users', '用户管理', 'MENU', 0, '/admin/users', 20, 1),
('admin:doctors', '医生管理', 'MENU', 0, '/admin/doctors', 30, 1),
('admin:drugs', '药品管理', 'MENU', 0, '/admin/drugs', 40, 1);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code IN ('patient:home', 'patient:doctors', 'patient:drugs')
WHERE r.role_code = 'PATIENT';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code IN ('doctor:workbench')
WHERE r.role_code = 'DOCTOR';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code IN ('pharmacist:workbench', 'pharmacist:drugs')
WHERE r.role_code = 'PHARMACIST';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `sys_role` r JOIN `sys_permission` p ON p.permission_code IN ('admin:dashboard', 'admin:users', 'admin:doctors', 'admin:drugs')
WHERE r.role_code = 'ADMIN';

-- 用户，演示密码使用 {noop} 明文标记，后续新建用户由后端 PBKDF2 加密。
INSERT IGNORE INTO `sys_user` (`username`, `password_hash`, `real_name`, `phone`, `email`, `gender`, `status`, `remark`) VALUES
('admin', '{noop}123456', '平台管理员', '18800000001', 'admin@medcare.local', 'UNKNOWN', 1, '第一阶段管理员账号'),
('patient_li', '{noop}123456', '李明', '18800000002', 'patient@medcare.local', 'MALE', 1, '演示患者账号'),
('doctor_zhang', '{noop}123456', '张文博', '18800000003', 'doctor1@medcare.local', 'MALE', 1, '呼吸内科医生'),
('doctor_wang', '{noop}123456', '王静', '18800000004', 'doctor2@medcare.local', 'FEMALE', 1, '心内科医生'),
('pharmacist_chen', '{noop}123456', '陈敏', '18800000005', 'pharmacist@medcare.local', 'FEMALE', 1, '演示药剂师账号');

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u JOIN `sys_role` r ON r.role_code = 'ADMIN' WHERE u.username = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u JOIN `sys_role` r ON r.role_code = 'PATIENT' WHERE u.username = 'patient_li';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u JOIN `sys_role` r ON r.role_code = 'DOCTOR' WHERE u.username IN ('doctor_zhang', 'doctor_wang');

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `sys_user` u JOIN `sys_role` r ON r.role_code = 'PHARMACIST' WHERE u.username = 'pharmacist_chen';

-- 角色资料
INSERT IGNORE INTO `patient_profile` (`user_id`, `id_card_no`, `birthday`, `height_cm`, `weight_kg`, `allergy_history`, `medical_history`)
SELECT id, '350100199001010011', '1990-01-01', 172.00, 68.00, '无明确药物过敏史', '偶发过敏性鼻炎'
FROM `sys_user` WHERE username = 'patient_li';

INSERT IGNORE INTO `doctor_profile` (`user_id`, `hospital`, `department`, `title`, `license_no`, `specialty`, `introduction`, `consultation_count`, `score`, `service_status`, `audit_status`)
SELECT id, '厦门大学附属第一医院', '呼吸内科', '主任医师', 'DOC-XM-1001', '感冒、咳嗽、哮喘、肺炎康复随访', '长期从事呼吸系统常见病与慢病管理。', 1260, 4.90, 1, 'APPROVED'
FROM `sys_user` WHERE username = 'doctor_zhang';

INSERT IGNORE INTO `doctor_profile` (`user_id`, `hospital`, `department`, `title`, `license_no`, `specialty`, `introduction`, `consultation_count`, `score`, `service_status`, `audit_status`)
SELECT id, '厦门市中医院', '心内科', '副主任医师', 'DOC-XM-1002', '高血压、冠心病、心律失常、慢病随访', '擅长心血管慢病在线随访与用药指导。', 980, 4.80, 1, 'APPROVED'
FROM `sys_user` WHERE username = 'doctor_wang';

INSERT IGNORE INTO `pharmacist_profile` (`user_id`, `certificate_no`, `hospital_or_org`, `working_years`, `audit_status`, `status`)
SELECT id, 'PHA-XM-2001', '悦医健康互联网药房', 8, 'APPROVED', 1
FROM `sys_user` WHERE username = 'pharmacist_chen';

-- 字典
INSERT IGNORE INTO `sys_dict_type` (`dict_code`, `dict_name`, `status`) VALUES
('department', '科室', 1),
('doctor_title', '医生职称', 1),
('drug_dosage_form', '药品剂型', 1);

INSERT IGNORE INTO `sys_dict_item` (`dict_type_id`, `item_label`, `item_value`, `sort_no`, `status`)
SELECT id, '呼吸内科', '呼吸内科', 10, 1 FROM `sys_dict_type` WHERE dict_code = 'department';
INSERT IGNORE INTO `sys_dict_item` (`dict_type_id`, `item_label`, `item_value`, `sort_no`, `status`)
SELECT id, '心内科', '心内科', 20, 1 FROM `sys_dict_type` WHERE dict_code = 'department';
INSERT IGNORE INTO `sys_dict_item` (`dict_type_id`, `item_label`, `item_value`, `sort_no`, `status`)
SELECT id, '内分泌科', '内分泌科', 30, 1 FROM `sys_dict_type` WHERE dict_code = 'department';
INSERT IGNORE INTO `sys_dict_item` (`dict_type_id`, `item_label`, `item_value`, `sort_no`, `status`)
SELECT id, '主任医师', '主任医师', 10, 1 FROM `sys_dict_type` WHERE dict_code = 'doctor_title';
INSERT IGNORE INTO `sys_dict_item` (`dict_type_id`, `item_label`, `item_value`, `sort_no`, `status`)
SELECT id, '副主任医师', '副主任医师', 20, 1 FROM `sys_dict_type` WHERE dict_code = 'doctor_title';

-- 药品分类
INSERT INTO `drug_category` (`id`, `parent_id`, `category_name`, `sort_no`, `status`) VALUES
(1, 0, '呼吸系统用药', 10, 1),
(2, 0, '心血管系统用药', 20, 1),
(3, 0, '内分泌系统用药', 30, 1),
(4, 0, '解热镇痛用药', 40, 1)
ON DUPLICATE KEY UPDATE
`category_name` = VALUES(`category_name`),
`sort_no` = VALUES(`sort_no`),
`status` = VALUES(`status`);

-- 药品
INSERT INTO `drug` (
  `drug_code`, `drug_name`, `generic_name`, `category_id`, `specification`, `dosage_form`, `manufacturer`, `approval_no`,
  `price`, `stock_quantity`, `warning_threshold`, `prescription_required`, `insurance_covered`, `usage_instruction`,
  `contraindication`, `adverse_reaction`, `status`
) VALUES
('DRUG-AMX-001', '阿莫西林胶囊', '阿莫西林', 1, '0.25g*24粒', '胶囊剂', '模拟制药厂A', '国药准字H000001', 18.80, 120, 20, 1, 1, '遵医嘱口服，建议饭后服用。', '青霉素过敏者禁用。', '可能出现胃肠不适、皮疹等。', 1),
('DRUG-IBU-001', '布洛芬缓释胶囊', '布洛芬', 4, '0.3g*20粒', '胶囊剂', '模拟制药厂B', '国药准字H000002', 22.50, 85, 20, 0, 1, '按说明或医嘱服用。', '严重肝肾功能不全者慎用。', '可能出现胃部不适。', 1),
('DRUG-NFD-001', '硝苯地平控释片', '硝苯地平', 2, '30mg*7片', '片剂', '模拟制药厂C', '国药准字H000003', 35.00, 60, 15, 1, 1, '遵医嘱每日固定时间服用。', '低血压患者慎用。', '可能出现头痛、面部潮红。', 1),
('DRUG-MET-001', '盐酸二甲双胍片', '二甲双胍', 3, '0.5g*48片', '片剂', '模拟制药厂D', '国药准字H000004', 28.60, 90, 15, 1, 1, '遵医嘱随餐或餐后服用。', '严重肾功能不全者禁用。', '可能出现恶心、腹泻等。', 1),
('DRUG-VC-001', '维生素C片', '维生素C', 4, '0.1g*100片', '片剂', '模拟制药厂E', '国药准字H000005', 12.00, 200, 30, 0, 0, '按说明服用。', '对本品过敏者禁用。', '偶见胃部不适。', 1)
ON DUPLICATE KEY UPDATE
`drug_name` = VALUES(`drug_name`),
`generic_name` = VALUES(`generic_name`),
`category_id` = VALUES(`category_id`),
`specification` = VALUES(`specification`),
`price` = VALUES(`price`),
`stock_quantity` = VALUES(`stock_quantity`),
`warning_threshold` = VALUES(`warning_threshold`),
`prescription_required` = VALUES(`prescription_required`),
`insurance_covered` = VALUES(`insurance_covered`),
`status` = VALUES(`status`);

-- 模拟联网医疗数据
INSERT INTO `mock_hospital` (`id`, `hospital_name`, `hospital_level`, `province`, `city`, `address`, `status`) VALUES
(1, '厦门大学附属第一医院', '三级甲等', '福建省', '厦门市', '思明区镇海路模拟地址', 1),
(2, '厦门市中医院', '三级甲等', '福建省', '厦门市', '湖里区仙岳路模拟地址', 1)
ON DUPLICATE KEY UPDATE
`hospital_name` = VALUES(`hospital_name`),
`hospital_level` = VALUES(`hospital_level`),
`status` = VALUES(`status`);

INSERT INTO `mock_disease` (`disease_code`, `disease_name`, `department`, `description`, `common_symptoms`, `status`) VALUES
('DIS-COLD-001', '急性上呼吸道感染', '呼吸内科', '常见呼吸道感染模拟疾病数据。', '咳嗽、鼻塞、流涕、咽痛', 1),
('DIS-HBP-001', '高血压', '心内科', '常见慢病模拟疾病数据。', '头晕、血压升高、心悸', 1),
('DIS-DM-001', '2型糖尿病', '内分泌科', '常见内分泌慢病模拟疾病数据。', '口渴、多尿、血糖升高', 1)
ON DUPLICATE KEY UPDATE
`disease_name` = VALUES(`disease_name`),
`department` = VALUES(`department`),
`status` = VALUES(`status`);

